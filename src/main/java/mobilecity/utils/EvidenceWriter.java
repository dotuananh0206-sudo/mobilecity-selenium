package mobilecity.utils;

import mobilecity.config.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class EvidenceWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvidenceWriter.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private EvidenceWriter() {
    }

    public static synchronized void record(String testName, String status, String details) {
        Path log = Path.of(TestConfig.reportDirectory(), "test-execution.log");
        String safeDetails = details == null ? "" : details.replaceAll("[\\r\\n]+", " ").trim();
        String line = "%s | %s | %s | %s | %s%n".formatted(
                TestRunContext.runId(),
                LocalDateTime.now().format(TIMESTAMP),
                testName,
                status,
                safeDetails);
        try {
            Files.createDirectories(log.getParent());
            Files.writeString(log, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            LOGGER.info("{} {}: {}", status, testName, safeDetails);
        } catch (IOException e) {
            LOGGER.error("Unable to write test execution log to {}", log, e);
            throw new IllegalStateException("Unable to persist test execution evidence", e);
        }
    }

    public static Path screenshotPath(String testName) throws IOException {
        Path directory = Path.of(TestConfig.screenshotDirectory());
        Files.createDirectories(directory);
        String safeName = testName.replaceAll("[^A-Za-z0-9._-]", "_");
        return directory.resolve(TestRunContext.runId() + "-" + safeName + ".png");
    }
}
