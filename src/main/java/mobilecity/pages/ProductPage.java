package mobilecity.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class ProductPage extends BasePage {
    private static final By PRODUCT_LINKS = By.cssSelector("p.name a[href$='.html']");
    private static final By PRODUCT_HEADING = By.cssSelector("h1");

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

    public void openFirstListedProduct() {
        clickable(PRODUCT_LINKS).click();
    }

    public boolean hasProductHeading() {
        return isVisible(PRODUCT_HEADING);
    }

    public String productHeading() {
        return visible(PRODUCT_HEADING).getText().trim();
    }

    public boolean hasProductDetails() {
        return hasProductHeading() && !productHeading().isBlank();
    }
}
