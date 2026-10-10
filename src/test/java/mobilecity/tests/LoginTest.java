package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.LoginPage;
import mobilecity.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginTest extends BaseTest {
    @Test
    @DisplayName("TC-LOGIN-001 - Form đăng nhập hiển thị các trường bắt buộc")
    void loginFormShowsRequiredFieldsWithoutSubmitting() {
        driver.get(TestData.url("login.url"));
        LoginPage loginPage = new LoginPage(driver);

        assertTrue(loginPage.hasRequiredFields(), "Form cần hiển thị trường định danh và mật khẩu");
        assertTrue(loginPage.fieldsAreRequired(), "Hai trường đăng nhập cần được đánh dấu bắt buộc");
        assertEquals("password", loginPage.passwordInputType(), "Mật khẩu cần được che trong trường nhập");
        assertTrue(loginPage.emptyLoginFieldsAreInvalid(),
                "Các trường trống cần không hợp lệ theo validation phía trình duyệt");
    }
}
