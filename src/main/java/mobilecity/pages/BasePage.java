package mobilecity.pages;

import mobilecity.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Set;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.waitTimeout());
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void clickAndWaitForDestination(WebElement link, String destination) {
        String target = link.getDomAttribute("target");
        Set<String> originalWindows = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        if ("_blank".equals(target)) {
            wait.until(ExpectedConditions.numberOfWindowsToBe(originalWindows.size() + 1));
            String newWindow = driver.getWindowHandles().stream()
                    .filter(handle -> !originalWindows.contains(handle))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "The link opened no new browser tab"));
            driver.switchTo().window(newWindow);
        }
        wait.until(ExpectedConditions.urlToBe(destination));
    }

    protected boolean isVisible(By locator) {
        try {
            return visible(locator).isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }
}
