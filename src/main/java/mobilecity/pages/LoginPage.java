package mobilecity.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class LoginPage extends BasePage {
    private static final By LOGIN_INPUT = By.name("login");
    private static final By PASSWORD_INPUT = By.name("password");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasRequiredFields() {
        return isVisible(LOGIN_INPUT) && isVisible(PASSWORD_INPUT);
    }

    public boolean fieldsAreRequired() {
        return visible(LOGIN_INPUT).getDomAttribute("required") != null
                && visible(PASSWORD_INPUT).getDomAttribute("required") != null;
    }

    public boolean emptyLoginFieldsAreInvalid() {
        WebElement login = visible(LOGIN_INPUT);
        WebElement password = visible(PASSWORD_INPUT);
        return !(Boolean) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].checkValidity() && arguments[1].checkValidity();",
                login, password);
    }

    public String passwordInputType() {
        return visible(PASSWORD_INPUT).getAttribute("type");
    }
}
