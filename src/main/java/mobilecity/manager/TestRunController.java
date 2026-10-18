package mobilecity.manager;

import mobilecity.manager.RunHistoryStore.RunRecord;
import mobilecity.manager.RunResultService.RunSummary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TestRunController implements AutoCloseable {
    public record RunProgress(
            int completed,
            int passed,
            int failed,
            int blocked,
            int skipped,
            String activeTestSelector) {
    }

    public record RunExecution(
            String id,
            Instant startedAt,
            long durationMillis,
            int exitCode,
            boolean cancelled,
            boolean cancellationFailed,
            RunSummary summary,
            Path reportDirectory,
            Path logFile,
            String persistenceError) {
    }

    public interface Listener {
        void onOutput(String line);

        default void onProgress(RunProgress progress) {
        }

        void onCancellationRequested();

        void onCompleted(RunExecution execution);
    }

    private static final DateTimeFormatter RUN_ID_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withZone(ZoneId.systemDefault());

    private final Path projectRoot;
    private final RunResultService resultService;
    private final RunHistoryStore historyStore;
    private final ExecutorService runExecutor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "mobilecity-test-runner");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean stopRequested = new AtomicBoolean();
    private volatile Process activeProcess;
    private volatile Path activeCancellationFile;

    public TestRunController(Path projectRoot, RunResultService resultService,
                             RunHistoryStore historyStore) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.resultService = resultService;
        this.historyStore = historyStore;
    }

    public boolean isRunning() {
        return running.get();
    }

    public String start(List<TestCaseDefinition> selected, TestSettings settings, Listener listener)
            throws IOException {
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Hãy chọn ít nhất một test case.");
        }
        settings.validate();
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Một lần chạy kiểm thử đang hoạt động.");
        }
        stopRequested.set(false);
        String runId = "GUI-" + RUN_ID_TIME.format(Instant.now());
        activeCancellationFile = Path.of(System.getProperty("user.home"),
                ".mobilecity-test-manager", "cancellation", runId + ".requested");
        try {
            runExecutor.execute(() -> execute(runId, List.copyOf(selected), settings, listener));
        } catch (RuntimeException e) {
            running.set(false);
            activeCancellationFile = null;
            throw e;
        }
        return runId;
    }

    public void cancel(Listener listener) {
        Process process = activeProcess;
        if (process == null || !process.isAlive()) {
            listener.onOutput("Chưa thể xác nhận tiến trình Maven đang hoạt động để dừng.");
            return;
        }
        if (stopRequested.get()) {
            listener.onOutput("Yêu cầu dừng đã được gửi; chờ test hiện tại kết thúc.");
            return;
        }
        Path cancellationFile = activeCancellationFile;
        if (cancellationFile == null) {
            listener.onOutput("Không có tệp tín hiệu hủy cho lần chạy; chưa gửi yêu cầu dừng.");
            return;
        }
        try {
            Files.createDirectories(cancellationFile.getParent());
            Files.writeString(cancellationFile,
                    "Finish the current JUnit test, then skip tests that have not started.",
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            listener.onOutput("Không ghi được tín hiệu dừng an toàn: " + e.getMessage());
            return;
        }
        stopRequested.set(true);
        listener.onCancellationRequested();
        listener.onOutput("Đã gửi tín hiệu dừng an toàn. Test đang chạy sẽ hoàn tất trước khi bỏ qua các test kế tiếp.");
    }

    private void execute(String runId, List<TestCaseDefinition> selected, TestSettings settings,
                        Listener listener) {
        Instant startedAt = Instant.now();
        long startNanos = System.nanoTime();
        Path reportDirectory = projectRoot.resolve("target")
                .resolve("surefire-reports").resolve(runId);
        Path runLog = Path.of(System.getProperty("user.home"), ".mobilecity-test-manager",
                "logs", runId + ".log");
        Path progressFile = reportDirectory.resolve("test-progress.log");
        int exitCode = -1;
        boolean cancelled = false;
        boolean cancellationFailed = false;
        String state;
        String persistenceError = "";
        ProgressTracker progressTracker = new ProgressTracker(progressFile, listener);
        java.util.concurrent.ScheduledExecutorService progressMonitor = null;

        try {
            Files.createDirectories(reportDirectory);
            Files.createDirectories(runLog.getParent());
            Files.deleteIfExists(progressFile);
            if (stopRequested.get()) {
                cancelled = true;
                state = "Đã hủy trước khi khởi chạy";
            } else {
                Path cancellationFile = activeCancellationFile;
                List<String> command = command(reportDirectory, selected, settings, cancellationFile);
                listener.onOutput("Lệnh chạy: " + String.join(" ", command));
                Process process = new ProcessBuilder(command)
                        .directory(projectRoot.toFile())
                        .redirectErrorStream(true)
                        .start();
                activeProcess = process;
                progressMonitor = Executors.newSingleThreadScheduledExecutor(task -> {
                    Thread thread = new Thread(task, "mobilecity-test-progress");
                    thread.setDaemon(true);
                    return thread;
                });
                progressMonitor.scheduleAtFixedRate(
                        progressTracker::pollSafely, 0, 250, TimeUnit.MILLISECONDS);
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                     BufferedWriter logWriter = Files.newBufferedWriter(runLog,
                             StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String safeLine = redact(line);
                        listener.onOutput(safeLine);
                        logWriter.write(safeLine);
                        logWriter.newLine();
                    }
                }
                exitCode = process.waitFor();
                boolean aliveDescendants = process.toHandle().descendants()
                        .anyMatch(ProcessHandle::isAlive);
                cancelled = stopRequested.get() && !process.isAlive() && !aliveDescendants
                        && activeCancellationFile != null && Files.exists(activeCancellationFile);
                cancellationFailed = stopRequested.get() && !cancelled;
                state = cancelled ? "Đã hủy"
                        : cancellationFailed ? "Không xác nhận được dừng an toàn"
                        : exitCode == 0 ? "Hoàn tất Maven" : "Maven có lỗi";
            }
        } catch (IOException e) {
            state = stopRequested.get() ? "Không xác nhận được hủy" : "Không thể khởi chạy Maven";
            listener.onOutput("Lỗi chạy kiểm thử: " + e.getMessage());
            cancellationFailed = stopRequested.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            state = "Tiến trình chạy bị gián đoạn";
            cancellationFailed = stopRequested.get();
            listener.onOutput("Luồng theo dõi Maven bị gián đoạn; cần kiểm tra trạng thái tiến trình.");
        } finally {
            if (progressMonitor != null) {
                progressMonitor.shutdownNow();
                progressTracker.pollSafely();
            }
            activeProcess = null;
        }

        RunSummary summary;
        try {
            summary = resultService.summarize(reportDirectory, selected);
        } catch (IOException e) {
            summary = new RunSummary(selected.size(), 0, 0, 0, selected.size(), List.of());
            persistenceError = "Không đọc được báo cáo Surefire: " + e.getMessage();
        }
        if (!cancelled && !stopRequested.get()
                && ("Hoàn tất Maven".equals(state) || "Maven có lỗi".equals(state))
                && summary.results().stream().noneMatch(result -> !result.status().equals("NOT EXECUTED"))) {
            state = exitCode == 0 ? "Không có kết quả JUnit" : "Maven thất bại, chưa có kết quả JUnit";
        }
        if (persistenceError.startsWith("Không đọc được báo cáo Surefire")) {
            state = "Không đọc được báo cáo Surefire";
        }

        long durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        Path testLog = Path.of(settings.reportDirectory()).resolve("test-execution.log");
        String selectors = selected.stream().map(TestCaseDefinition::selector)
                .reduce((first, second) -> first + "," + second).orElse("");
        try {
            historyStore.save(new RunRecord(runId, startedAt, durationMillis, state,
                    selected.size(), summary.passed(), summary.failed(), summary.blocked(),
                    summary.notExecuted(), exitCode, selectors, reportDirectory, testLog));
        } catch (IOException e) {
            persistenceError = persistenceError.isBlank()
                    ? "Không lưu được lịch sử chạy: " + e.getMessage()
                    : persistenceError + " | Không lưu được lịch sử chạy: " + e.getMessage();
        }
        Path cancellationFile = activeCancellationFile;
        if (cancellationFile != null) {
            try {
                Files.deleteIfExists(cancellationFile);
            } catch (IOException e) {
                persistenceError = persistenceError.isBlank()
                        ? "Không dọn được tệp tín hiệu dừng: " + e.getMessage()
                        : persistenceError + " | Không dọn được tệp tín hiệu dừng: " + e.getMessage();
            }
        }
        activeCancellationFile = null;
        running.set(false);
        listener.onCompleted(new RunExecution(runId, startedAt, durationMillis, exitCode,
                cancelled, cancellationFailed, summary, reportDirectory, runLog, persistenceError));
    }

    private List<String> command(Path reportDirectory, List<TestCaseDefinition> selected,
                                 TestSettings settings, Path cancellationFile) {
        List<String> arguments = new ArrayList<>();
        arguments.add("-B");
        arguments.add("--no-transfer-progress");
        arguments.add("-Dmobilecity.surefireReportsDirectory=" + reportDirectory);
        arguments.add("-Dmobilecity.baseUrl=" + settings.baseUrl().trim());
        arguments.add("-Dmobilecity.headless=" + settings.headless());
        arguments.add("-Dmobilecity.waitSeconds=" + settings.waitSeconds());
        arguments.add("-Dmobilecity.screenshotDir=" + settings.screenshotDirectory().trim());
        arguments.add("-Dmobilecity.reportDir=" + settings.reportDirectory().trim());
        arguments.add("-Dmobilecity.cancelFile=" + cancellationFile);
        arguments.add("-Dmobilecity.progressFile=" + reportDirectory.resolve("test-progress.log"));
        if (selected.size() != TestCaseCatalog.all().size()) {
            Map<String, List<String>> methodsByClass = new LinkedHashMap<>();
            for (TestCaseDefinition testCase : selected) {
                methodsByClass.computeIfAbsent(testCase.className(), ignored -> new ArrayList<>())
                        .add(testCase.methodName());
            }
            String selector = methodsByClass.entrySet().stream()
                    .map(entry -> entry.getKey() + "#" + String.join("+", entry.getValue()))
                    .reduce((first, next) -> first + "," + next).orElseThrow();
            arguments.add("-Dtest=" + selector);
        }
        arguments.add("test");

        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")) {
            String mavenCommand = "mvn " + arguments.stream()
                    .map(TestRunController::quoteWindowsArgument)
                    .reduce((first, next) -> first + " " + next).orElse("");
            return List.of("cmd.exe", "/d", "/s", "/c", "\"" + mavenCommand + "\"");
        }
        List<String> command = new ArrayList<>();
        command.add("mvn");
        command.addAll(arguments);
        return List.copyOf(command);
    }

    private static final class ProgressTracker {
        private final Path progressFile;
        private final Listener listener;
        private final AtomicBoolean readErrorReported = new AtomicBoolean();
        private int processedLines;
        private int passed;
        private int failed;
        private int blocked;
        private int skipped;
        private String activeTestSelector;

        private ProgressTracker(Path progressFile, Listener listener) {
            this.progressFile = progressFile;
            this.listener = listener;
        }

        private synchronized void pollSafely() {
            if (!Files.isRegularFile(progressFile)) {
                return;
            }
            try {
                String content = Files.readString(progressFile, StandardCharsets.UTF_8);
                String[] lines = content.split("\\R", -1);
                int completeLines = lines.length - 1;
                while (processedLines < completeLines) {
                    apply(lines[processedLines]);
                    processedLines++;
                }
                readErrorReported.set(false);
            } catch (IOException e) {
                if (readErrorReported.compareAndSet(false, true)) {
                    listener.onOutput("Không đọc được tiến độ JUnit: " + e.getMessage());
                }
            }
        }

        private void apply(String line) {
            String[] fields = line.split("\\t", 2);
            if (fields.length != 2) {
                listener.onOutput("Bỏ qua dòng tiến độ JUnit không hợp lệ.");
                return;
            }
            String selector = fields[1];
            switch (fields[0]) {
                case "START" -> activeTestSelector = selector;
                case "PASS" -> {
                    passed++;
                    activeTestSelector = null;
                }
                case "FAIL" -> {
                    failed++;
                    activeTestSelector = null;
                }
                case "BLOCKED" -> {
                    blocked++;
                    activeTestSelector = null;
                }
                case "SKIPPED" -> {
                    skipped++;
                    activeTestSelector = null;
                }
                default -> {
                    listener.onOutput("Bỏ qua trạng thái tiến độ JUnit không hỗ trợ: "
                            + fields[0]);
                    return;
                }
            }
            listener.onProgress(new RunProgress(
                    passed + failed + blocked, passed, failed, blocked, skipped,
                    activeTestSelector));
        }
    }

    private static String quoteWindowsArgument(String argument) {
        if (argument.chars().anyMatch(Character::isWhitespace) || argument.contains("\"")) {
            return "\"" + argument.replace("\"", "\\\"") + "\"";
        }
        return argument;
    }

    private static String redact(String line) {
        String result = line;
        for (String name : List.of("MOBILECITY_USERNAME", "MOBILECITY_PASSWORD")) {
            String secret = System.getenv(name);
            if (secret != null && !secret.isEmpty()) {
                result = result.replace(secret, "[REDACTED]");
            }
        }
        return result.replaceAll(
                "(?i)(password|passwd|username|token|secret)\\s*[:=]\\s*[^\\s,;]+",
                "$1=[REDACTED]");
    }

    @Override
    public void close() {
        runExecutor.shutdown();
    }
}
