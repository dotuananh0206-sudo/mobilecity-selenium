package mobilecity.pages;

import mobilecity.utils.TestData;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Locale;

public final class WarrantyPolicyPage extends BasePage {
    private static final By FOOTER_POLICY_LINK =
            By.cssSelector("footer a[href$='/page/chinh-sach-bao-hanh.html']");
    private static final By POLICY_HEADING = By.cssSelector("h1.news-content-detail-title");
    private static final By POLICY_CONTENT = By.cssSelector(".news-content");
    private static final By POLICY_TABLES = By.cssSelector(".news-content table");

    public WarrantyPolicyPage(WebDriver driver) {
        super(driver);
    }

    public WarrantyPolicyPage open() {
        driver.get(TestData.url("warranty.url"));
        return this;
    }

    public boolean hasFooterPolicyLink() {
        return driver.findElements(FOOTER_POLICY_LINK).stream().anyMatch(WebElement::isDisplayed);
    }

    public String footerPolicyDestination() {
        String destination = visibleFooterPolicyLink().getAttribute("href");
        if (destination == null || destination.isBlank()) {
            throw new IllegalStateException("The footer warranty policy link has no destination");
        }
        return destination;
    }

    public void openPolicyFromFooter() {
        WebElement link = visibleFooterPolicyLink();
        String destination = link.getAttribute("href");
        clickAndWaitForDestination(link, destination);
        visible(POLICY_HEADING);
    }

    public boolean hasPolicyHeading() {
        return isVisible(POLICY_HEADING)
                && visible(POLICY_HEADING).getText().toLowerCase(Locale.ROOT)
                        .contains("bảo hành");
    }

    public boolean hasPolicyContent() {
        return driver.findElements(POLICY_CONTENT).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(content -> !content.getText().isBlank());
    }

    public boolean hasWarrantyDuration() {
        String text = policyText().toLowerCase(Locale.ROOT);
        return text.contains("thời gian đổi máy") || text.contains("thời gian bh");
    }

    public boolean hasWarrantyCoverage() {
        return policyText().toLowerCase(Locale.ROOT).contains("nội dung được bh");
    }

    public boolean hasExtendedWarrantyPackage() {
        String text = policyText().toLowerCase(Locale.ROOT);
        return text.contains("bh vàng") && text.contains("gói bảo hành mua thêm");
    }

    public boolean hasWarrantyExclusionConditions() {
        String text = policyText().toLowerCase(Locale.ROOT);
        return text.contains("không bảo hành") || text.contains("không áp dụng bảo hành");
    }

    public boolean scrollThroughContentWithoutOverflow() {
        JavascriptExecutor javascript = (JavascriptExecutor) driver;
        javascript.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        return Boolean.TRUE.equals(javascript.executeScript(
                "return document.documentElement.scrollWidth <= document.documentElement.clientWidth "
                        + "&& document.body.scrollHeight > 0"));
    }

    public String contentFontFamily() {
        WebElement content = visible(POLICY_CONTENT);
        return (String) ((JavascriptExecutor) driver).executeScript(
                "return getComputedStyle(arguments[0]).fontFamily", content);
    }

    public boolean hasVisiblePolicyTable() {
        return driver.findElements(POLICY_TABLES).stream().anyMatch(WebElement::isDisplayed);
    }

    public boolean visiblePolicyTablesFitViewport() {
        return driver.findElements(POLICY_TABLES).stream()
                .filter(WebElement::isDisplayed)
                .allMatch(table -> Boolean.TRUE.equals(
                        ((JavascriptExecutor) driver).executeScript("""
                                const bounds = arguments[0].getBoundingClientRect();
                                const viewportWidth = document.documentElement.clientWidth;
                                return arguments[0].scrollWidth <= arguments[0].clientWidth
                                    && bounds.left >= 0
                                    && bounds.right <= viewportWidth + 1;
                                """, table)));
    }

    private String policyText() {
        return visible(POLICY_CONTENT).getText();
    }

    private WebElement visibleFooterPolicyLink() {
        return wait.until(currentDriver -> currentDriver.findElements(FOOTER_POLICY_LINK).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null));
    }
}
