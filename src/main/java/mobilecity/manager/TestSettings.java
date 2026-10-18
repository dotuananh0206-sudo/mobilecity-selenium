package mobilecity.manager;

import mobilecity.config.TestConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record TestSettings(
        String baseUrl,
        boolean headless,
        int waitSeconds,
        String screenshotDirectory,
        String reportDirectory) {
    private static final Path SETTINGS_FILE = Path.of(
            System.getProperty("user.home"), ".mobilecity-test-manager", "settings.properties");

    public static TestSettings load() throws IOException {
        Properties properties = new Properties();
        if (Files.exists(SETTINGS_FILE)) {
            try (Reader reader = Files.newBufferedReader(SETTINGS_FILE)) {
                properties.load(reader);
            }
        }
        String configuredHeadless = properties.getProperty(
                "headless", Boolean.toString(TestConfig.headless()));
        if (!configuredHeadless.equalsIgnoreCase("true")
                && !configuredHeadless.equalsIgnoreCase("false")) {
            throw new IOException("Giá trị headless trong cấu hình phải là true hoặc false.");
        }
        try {
            TestSettings settings = new TestSettings(
                    properties.getProperty("baseUrl", TestConfig.baseUrl()),
                    Boolean.parseBoolean(configuredHeadless),
                    Integer.parseInt(properties.getProperty(
                            "waitSeconds", Long.toString(TestConfig.waitTimeout().toSeconds()))),
                    properties.getProperty("screenshotDirectory", TestConfig.screenshotDirectory()),
                    properties.getProperty("reportDirectory", TestConfig.reportDirectory()));
            settings.validate();
            return settings;
        } catch (IllegalArgumentException e) {
            throw new IOException("Cấu hình ứng dụng không hợp lệ: " + e.getMessage(), e);
        }
    }

    public void validate() {
        URI uri;
        try {
            uri = URI.create(baseUrl.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("URL website không hợp lệ.", e);
        }
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
            throw new IllegalArgumentException("URL phải là địa chỉ HTTP hoặc HTTPS hợp lệ.");
        }
        if (waitSeconds < 1 || waitSeconds > 120) {
            throw new IllegalArgumentException("Thời gian chờ phải từ 1 đến 120 giây.");
        }
        if (screenshotDirectory.isBlank() || reportDirectory.isBlank()) {
            throw new IllegalArgumentException("Thư mục ảnh và báo cáo không được để trống.");
        }
        if (screenshotDirectory.contains("\"") || reportDirectory.contains("\"")
                || screenshotDirectory.contains("\n") || reportDirectory.contains("\n")) {
            throw new IllegalArgumentException("Đường dẫn thư mục không được chứa dấu ngoặc kép hoặc xuống dòng.");
        }
        Path.of(screenshotDirectory);
        Path.of(reportDirectory);
    }

    public void save() throws IOException {
        validate();
        Files.createDirectories(SETTINGS_FILE.getParent());
        Properties properties = new Properties();
        properties.setProperty("baseUrl", baseUrl.trim());
        properties.setProperty("headless", Boolean.toString(headless));
        properties.setProperty("waitSeconds", Integer.toString(waitSeconds));
        properties.setProperty("screenshotDirectory", screenshotDirectory.trim());
        properties.setProperty("reportDirectory", reportDirectory.trim());
        try (Writer writer = Files.newBufferedWriter(SETTINGS_FILE)) {
            properties.store(writer, "MobileCity Test Manager settings");
        }
    }

    public static Path settingsFile() {
        return SETTINGS_FILE;
    }
}
