package mobilecity.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.PageLoadStrategy;

public final class DriverFactory {
    private DriverFactory() {
    }

    public static WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments("--window-size=1440,1000");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        if (TestConfig.headless()) {
            options.addArguments("--headless=new");
        }
        return new ChromeDriver(options);
    }
}
