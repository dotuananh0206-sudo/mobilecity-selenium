package mobilecity.base;

import mobilecity.utils.EvidenceWriter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestWatcher;
import org.opentest4j.TestAbortedException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

public final class FailureEvidenceExtension implements TestWatcher, TestExecutionExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailureEvidenceExtension.class);
    private final Supplier<WebDriver> driverSupplier;

    public FailureEvidenceExtension(Supplier<WebDriver> driverSupplier) {
        this.driverSupplier = driverSupplier;
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        if (!(throwable instanceof TestAbortedException)) {
            captureFailure(context.getDisplayName(), throwable);
        }
        throw throwable;
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        EvidenceWriter.record(context.getDisplayName(), "PASS", "JUnit test completed successfully");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        EvidenceWriter.record(context.getDisplayName(), "FAIL", cause.toString());
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        EvidenceWriter.record(context.getDisplayName(), "BLOCKED", cause.toString());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        EvidenceWriter.record(context.getDisplayName(), "NOT EXECUTED",
                reason.orElse("Test disabled"));
    }

    private void captureFailure(String testName, Throwable failure) {
        WebDriver driver = driverSupplier.get();
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            LOGGER.warn("Failure screenshot unavailable for {}: active WebDriver does not support screenshots", testName);
            return;
        }
        try {
            Path destination = EvidenceWriter.screenshotPath(testName);
            Files.write(destination, screenshotDriver.getScreenshotAs(OutputType.BYTES));
            LOGGER.error("Failure screenshot saved to {}", destination);
        } catch (IOException | WebDriverException e) {
            LOGGER.error("Unable to capture failure screenshot for {}", testName, e);
        }
    }
}
