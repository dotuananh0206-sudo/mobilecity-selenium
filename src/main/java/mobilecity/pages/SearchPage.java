package mobilecity.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class SearchPage extends BasePage {
    private static final By SEARCH_INPUT = By.id("keyword");

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    public boolean isSearchInputVisible() {
        return isVisible(SEARCH_INPUT);
    }

    public String placeholder() {
        return visible(SEARCH_INPUT).getDomAttribute("placeholder");
    }

    public void enterTermWithoutSubmitting(String term) {
        WebElement input = visible(SEARCH_INPUT);
        input.clear();
        input.sendKeys(term);
    }

    public String enteredTerm() {
        return visible(SEARCH_INPUT).getDomProperty("value");
    }
}
