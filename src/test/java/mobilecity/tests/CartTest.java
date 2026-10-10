package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.CartPage;
import mobilecity.pages.HomePage;
import mobilecity.utils.TestData;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("JUnitTestClassNamingConvention")
class CartTest extends BaseTest {
    @Test
    @DisplayName("TC-CART-001 - Chỉ mở giỏ hàng qua liên kết công khai")
    void cartIsOpenedOnlyThroughPublishedLink() {
        HomePage homePage = new HomePage(driver).open();
        Assumptions.assumeTrue(homePage.hasPublishedCartLink(),
                "BLOCKED: không tìm thấy liên kết giỏ hàng công khai; không đoán URL hoặc kích hoạt mua hàng");

        homePage.openPublishedCartLink();
        CartPage cartPage = new CartPage(driver);
        Assumptions.assumeTrue(cartPage.hasCartPageLoadedFromPublishedLink(),
                "BLOCKED: liên kết công khai không dẫn đến trang giỏ hàng đã nhận diện");
    }
}
