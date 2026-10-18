package mobilecity.base;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public final class RunCancellationExtension implements ExecutionCondition,
        BeforeTestExecutionCallback, TestWatcher {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return RunCancellationSignal.isRequested()
                ? ConditionEvaluationResult.disabled(
                        "NOT EXECUTED: dừng an toàn được yêu cầu; test chưa bắt đầu.")
                : ConditionEvaluationResult.enabled("Không có yêu cầu dừng an toàn.");
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        recordProgress(context, "START");
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        recordProgress(context, "PASS");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        recordProgress(context, "FAIL");
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        String message = cause.getMessage();
        recordProgress(context, message != null && message.toUpperCase(java.util.Locale.ROOT)
                .contains("BLOCKED") ? "BLOCKED" : "SKIPPED");
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        recordProgress(context, "SKIPPED");
    }

    private static void recordProgress(ExtensionContext context, String event) {
        String configuredFile = System.getProperty("mobilecity.progressFile");
        if (configuredFile == null || configuredFile.isBlank()) {
            return;
        }
        String selector = context.getTestMethod()
                .map(method -> method.getDeclaringClass().getName() + "#" + method.getName())
                .orElse(context.getUniqueId());
        try {
            Files.writeString(Path.of(configuredFile), event + "\t" + selector
                            + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Không ghi được tiến độ JUnit: " + e.getMessage());
        }
    }
}
