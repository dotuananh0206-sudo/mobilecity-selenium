package mobilecity.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public final class ProductPage extends BasePage {
    private static final By PRODUCT_LINKS = By.cssSelector("p.name a[href$='.html']");
    private static final By PRODUCT_HEADING = By.cssSelector("h1");
    private static final By LISTED_PRODUCT_PRICE = By.cssSelector("div.product-list-item .price");
    private static final By PRODUCT_PRICE = By.cssSelector(".product-price-box .price");
    private static final By PRIMARY_PRODUCT_IMAGE = By.cssSelector("ul.product_image li.active img");
    private static final By STORAGE_OPTIONS = By.cssSelector(".storage-list .storage-item");
    private static final By COLOR_OPTIONS = By.cssSelector(".color-list .color-item");
    private static final By SELECTED_COLOR = By.cssSelector(".color-list .color-name-selected");
    private static final By WARRANTY_OPTIONS = By.cssSelector(".warranty-list .attribute-item");
    private static final By PURCHASE_ACTIONS = By.cssSelector(".buy-box a.pre-order");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public String firstListedProductName() {
        String text = visible(PRODUCT_LINKS).getText().trim();
        if (text.isBlank()) {
            throw new IllegalStateException("The first listed product link has no visible name");
        }
        return text;
    }

    public String firstListedProductUrl() {
        String href = visible(PRODUCT_LINKS).getAttribute("href");
        if (href == null || href.isBlank()) {
            throw new IllegalStateException("The first listed product link has no destination");
        }
        return href;
    }

    public String firstListedProductPrice() {
        return visible(LISTED_PRODUCT_PRICE).getText().trim();
    }

    public void openFirstListedProduct() {
        clickable(PRODUCT_LINKS).click();
    }

    public boolean hasProductHeading() {
        return isVisible(PRODUCT_HEADING);
    }

    public String productHeading() {
        return visible(PRODUCT_HEADING).getText().trim();
    }

    public String productPrice() {
        return visible(PRODUCT_PRICE).getText().trim();
    }

    public String primaryImageSource() {
        return visible(PRIMARY_PRODUCT_IMAGE).getAttribute("src");
    }

    public boolean hasWarrantyOption() {
        return driver.findElements(WARRANTY_OPTIONS).stream().anyMatch(WebElement::isDisplayed);
    }

    public boolean hasPurchaseAction() {
        return driver.findElements(PURCHASE_ACTIONS).stream().anyMatch(WebElement::isDisplayed);
    }

    public boolean hasStorageOption(String version) {
        return findOption(STORAGE_OPTIONS, version) != null;
    }

    public void selectStorageOption(String version) {
        WebElement option = wait.until(currentDriver -> findOption(STORAGE_OPTIONS, version));
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    public boolean isStorageOptionSelected(String version) {
        WebElement option = findOption(STORAGE_OPTIONS, version);
        return option != null && option.getAttribute("class").contains("active");
    }

    public int colorOptionCount() {
        return (int) driver.findElements(COLOR_OPTIONS).stream()
                .filter(WebElement::isDisplayed)
                .count();
    }

    public String colorOptionName(int index) {
        return colorOptions().get(index).getAttribute("data-title");
    }

    public void selectColorAt(int index) {
        WebElement option = colorOptions().get(index);
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    public boolean isColorOptionSelected(String colorName) {
        return colorOptions().stream()
                .anyMatch(option -> colorName.equals(option.getAttribute("data-title"))
                        && option.getAttribute("class").contains("active"));
    }

    public String selectedColorName() {
        return driver.findElements(SELECTED_COLOR).stream()
                .filter(WebElement::isDisplayed)
                .map(element -> element.getText().trim())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No visible selected product color"));
    }

    public boolean hasLoadedPrimaryImage() {
        return wait.until(currentDriver -> {
            WebElement image = currentDriver.findElement(PRIMARY_PRODUCT_IMAGE);
            return Boolean.TRUE.equals(((JavascriptExecutor) currentDriver)
                    .executeScript("return arguments[0].complete && arguments[0].naturalWidth > 0", image));
        });
    }

    public boolean hasProductDetails() {
        return hasProductHeading() && !productHeading().isBlank();
    }

    public boolean hasSpecificationSection() {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo(0, document.body.scrollHeight)");
        return wait.until(currentDriver -> currentDriver.findElement(By.tagName("body"))
                .getText()
                .contains("Thông Số Kỹ Thuật"));
    }

    private WebElement findOption(By locator, String optionText) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(option -> option.getText().trim().equals(optionText))
                .findFirst()
                .orElse(null);
    }

    private List<WebElement> colorOptions() {
        return driver.findElements(COLOR_OPTIONS).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }
}
