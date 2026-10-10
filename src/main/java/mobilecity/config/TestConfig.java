package mobilecity.config;

import java.time.Duration;

public final class TestConfig {
    private TestConfig() {
    }

    public static String baseUrl() {
        return value("mobilecity.baseUrl", "MOBILECITY_BASE_URL", "https://mobilecity.vn/");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(value("mobilecity.headless", "MOBILECITY_HEADLESS", "true"));
    }

    public static Duration waitTimeout() {
        String configured = value("mobilecity.waitSeconds", "MOBILECITY_WAIT_SECONDS", "15");
        try {
            long seconds = Long.parseLong(configured);
            if (seconds < 1 || seconds > 120) {
                throw new IllegalArgumentException("mobilecity.waitSeconds must be between 1 and 120");
            }
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("mobilecity.waitSeconds must be an integer", e);
        }
    }

    public static String screenshotDirectory() {
        return value("mobilecity.screenshotDir", "MOBILECITY_SCREENSHOT_DIR", "screenshots");
    }

    public static String reportDirectory() {
        return value("mobilecity.reportDir", "MOBILECITY_REPORT_DIR", "reports");
    }

    private static String value(String property, String environment, String fallback) {
        String systemValue = System.getProperty(property);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }
        String environmentValue = System.getenv(environment);
        return environmentValue == null || environmentValue.isBlank()
                ? fallback
                : environmentValue.trim();
    }
}
