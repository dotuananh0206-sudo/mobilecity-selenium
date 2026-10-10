package mobilecity.pages;

import org.openqa.selenium.WebDriver;

public final class CartPage extends BasePage {
    public CartPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasCartPageLoadedFromPublishedLink() {
        return driver.getCurrentUrl().toLowerCase().contains("cart")
                || driver.getTitle().toLowerCase().contains("giỏ hàng");
    }
}
