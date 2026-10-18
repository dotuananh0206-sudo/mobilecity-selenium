package mobilecity.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestSettingsTest {
    @Test
    void acceptsSupportedTestConfigValues() {
        TestSettings settings = new TestSettings(
                "https://mobilecity.vn/", true, 15, "screenshots", "reports");

        assertDoesNotThrow(settings::validate);
    }

    @Test
    void rejectsUnsupportedUrlWaitAndEmptyOutputPaths() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestSettings("file:///tmp", true, 15, "screenshots", "reports").validate());
        assertThrows(IllegalArgumentException.class,
                () -> new TestSettings("https://mobilecity.vn/", true, 121, "screenshots", "reports").validate());
        assertThrows(IllegalArgumentException.class,
                () -> new TestSettings("https://mobilecity.vn/", true, 15, "", "reports").validate());
    }
}
