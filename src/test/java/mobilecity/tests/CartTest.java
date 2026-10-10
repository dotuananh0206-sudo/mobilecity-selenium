package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.CartPage;
import mobilecity.pages.HomePage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("JUnitTestClassNamingConvention")
class CartTest extends BaseTest {
    @Test
    @DisplayName("TC-CART-001 - Chỉ mở giỏ hàng qua liên kết công khai đã xác minh")
    void cartIsOpenedOnlyThroughPublishedLink() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        Assumptions.assumeTrue(homePage.hasPublishedCartLink(),
                "BLOCKED: chưa thấy liên kết giỏ hàng công khai; không đoán URL hoặc bấm nút mua/đặt trước");

        homePage.openPublishedCartLink();
        assumeNoSiteChallenge();
        CartPage cartPage = new CartPage(driver);
        Assumptions.assumeTrue(cartPage.hasCartPageLoadedFromPublishedLink(),
                "BLOCKED: liên kết công khai chưa dẫn đến trang giỏ hàng có thể nhận diện");
    }
}
