package mobilecity.manager;

import mobilecity.base.RunCancellationSignal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunCancellationSignalTest {
    @Test
    void detectsOnlyAnExistingConfiguredCancellationFile(@TempDir Path directory) throws IOException {
        String previous = System.getProperty("mobilecity.cancelFile");
        Path marker = directory.resolve("cancel.requested");
        try {
            System.setProperty("mobilecity.cancelFile", marker.toString());
            assertFalse(RunCancellationSignal.isRequested());
            Files.createFile(marker);
            assertTrue(RunCancellationSignal.isRequested());
        } finally {
            if (previous == null) {
                System.clearProperty("mobilecity.cancelFile");
            } else {
                System.setProperty("mobilecity.cancelFile", previous);
            }
        }
    }
}
