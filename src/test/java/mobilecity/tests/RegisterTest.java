package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.RegisterPage;
import mobilecity.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterTest extends BaseTest {
    @Test
    @DisplayName("TC-REG-001 - Form đăng ký hiển thị mà không gửi dữ liệu")
    void registrationFormIsVisibleWithoutSubmitting() {
        driver.get(TestData.url("register.url"));
        RegisterPage registerPage = new RegisterPage(driver);

        assertTrue(registerPage.hasRegistrationInput(), "Trang đăng ký cần hiển thị trường bắt đầu");
        assertTrue(registerPage.hasContinueButton(), "Trang đăng ký cần hiển thị nút tiếp tục");
        assertTrue(!registerPage.registrationPlaceholder().isBlank(),
                "Trường đăng ký cần có hướng dẫn nhập");
    }
}
