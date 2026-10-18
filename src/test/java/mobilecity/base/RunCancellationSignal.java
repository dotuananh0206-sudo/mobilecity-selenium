package mobilecity.base;

import java.nio.file.Files;
import java.nio.file.Path;

public final class RunCancellationSignal {
    private RunCancellationSignal() {
    }

    public static boolean isRequested() {
        String configuredFile = System.getProperty("mobilecity.cancelFile");
        return configuredFile != null && !configuredFile.isBlank()
                && Files.isRegularFile(Path.of(configuredFile));
    }
}
