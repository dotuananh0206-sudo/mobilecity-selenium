package mobilecity.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenceWriterTest {
    @Test
    void writesVietnameseEvidenceAsUtf8(@TempDir Path tempDirectory) throws IOException {
        String previousReportDirectory = System.getProperty("mobilecity.reportDir");
        System.setProperty("mobilecity.reportDir", tempDirectory.toString());
        try {
            EvidenceWriter.record("TC-VIỆT-001", "PASS", "Đã tải ảnh chính");

            Path log = tempDirectory.resolve("test-execution.log");
            String contents = Files.readString(log, StandardCharsets.UTF_8);

            assertTrue(contents.contains("TC-VIỆT-001"));
            assertTrue(contents.contains("Đã tải ảnh chính"));
            assertFalse(contents.contains("\uFFFD"));
        } finally {
            if (previousReportDirectory == null) {
                System.clearProperty("mobilecity.reportDir");
            } else {
                System.setProperty("mobilecity.reportDir", previousReportDirectory);
            }
        }
    }
}
