package mobilecity.manager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public final class RunHistoryStore {
    public record RunRecord(
            String id,
            Instant startedAt,
            long durationMillis,
            String state,
            int selected,
            int passed,
            int failed,
            int blocked,
            int notExecuted,
            int exitCode,
            String selectors,
            Path reportDirectory,
            Path logFile) {
    }

    private final Path historyDirectory;

    public RunHistoryStore() {
        this(Path.of(System.getProperty("user.home"), ".mobilecity-test-manager", "history"));
    }

    public RunHistoryStore(Path historyDirectory) {
        this.historyDirectory = historyDirectory;
    }

    public void save(RunRecord record) throws IOException {
        Files.createDirectories(historyDirectory);
        Properties properties = new Properties();
        properties.setProperty("id", record.id());
        properties.setProperty("startedAt", record.startedAt().toString());
        properties.setProperty("durationMillis", Long.toString(record.durationMillis()));
        properties.setProperty("state", record.state());
        properties.setProperty("selected", Integer.toString(record.selected()));
        properties.setProperty("passed", Integer.toString(record.passed()));
        properties.setProperty("failed", Integer.toString(record.failed()));
        properties.setProperty("blocked", Integer.toString(record.blocked()));
        properties.setProperty("notExecuted", Integer.toString(record.notExecuted()));
        properties.setProperty("exitCode", Integer.toString(record.exitCode()));
        properties.setProperty("selectors", record.selectors());
        properties.setProperty("reportDirectory", record.reportDirectory().toString());
        properties.setProperty("logFile", record.logFile().toString());
        Path destination = historyDirectory.resolve(record.id() + ".properties");
        try (Writer writer = Files.newBufferedWriter(destination)) {
            properties.store(writer, "Actual MobileCity Maven/JUnit run");
        }
    }

    public List<RunRecord> loadAll() throws IOException {
        if (!Files.isDirectory(historyDirectory)) {
            return List.of();
        }
        List<Path> records;
        try (var files = Files.list(historyDirectory)) {
            records = files.filter(path -> path.getFileName().toString().endsWith(".properties"))
                    .toList();
        }
        List<RunRecord> history = new ArrayList<>();
        for (Path file : records) {
            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(file)) {
                properties.load(reader);
            }
            history.add(new RunRecord(
                    required(properties, "id"),
                    Instant.parse(required(properties, "startedAt")),
                    Long.parseLong(required(properties, "durationMillis")),
                    required(properties, "state"),
                    Integer.parseInt(required(properties, "selected")),
                    Integer.parseInt(required(properties, "passed")),
                    Integer.parseInt(required(properties, "failed")),
                    Integer.parseInt(required(properties, "blocked")),
                    Integer.parseInt(required(properties, "notExecuted")),
                    Integer.parseInt(required(properties, "exitCode")),
                    properties.getProperty("selectors", ""),
                    Path.of(required(properties, "reportDirectory")),
                    Path.of(required(properties, "logFile"))));
        }
        history.sort(Comparator.comparing(RunRecord::startedAt).reversed());
        return List.copyOf(history);
    }

    public Path historyDirectory() {
        return historyDirectory;
    }

    private static String required(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IOException("Thiếu trường " + key + " trong lịch sử chạy.");
        }
        return value;
    }
}
