package mobilecity.base;

import mobilecity.config.DriverFactory;
import mobilecity.config.TestConfig;
import mobilecity.utils.TestRunContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

    protected WebDriver driver;

    @RegisterExtension
    private final FailureEvidenceExtension evidenceExtension =
            new FailureEvidenceExtension(() -> driver);

    @BeforeEach
    void startBrowser() {
        LOGGER.info("Starting run {} with Chrome; headless={}", TestRunContext.runId(), TestConfig.headless());
        driver = DriverFactory.createChromeDriver();
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(TestConfig.waitTimeout().multipliedBy(2));
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (RuntimeException e) {
                LOGGER.error("Unable to close Chrome cleanly", e);
                throw e;
            } finally {
                driver = null;
            }
        }
    }
}
