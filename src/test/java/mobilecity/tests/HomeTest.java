package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.HomePage;
import mobilecity.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeTest extends BaseTest {
    @Test
    @DisplayName("TC-HOME-001 - Trang chủ tải và hiển thị tiêu đề")
    void homepageLoadsWithTitle() {
        HomePage homePage = new HomePage(driver).open();

        assertTrue(homePage.hasTitle(), "Trang chủ cần có tiêu đề trang");
        assertTrue(homePage.hasPhoneCategoryLink(), "Trang chủ cần hiển thị liên kết danh mục Điện thoại");
    }
}
