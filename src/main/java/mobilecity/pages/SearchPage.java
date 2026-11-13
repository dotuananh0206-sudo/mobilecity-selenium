package mobilecity.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.regex.Pattern;

public final class SearchPage extends BasePage {
    private static final By SEARCH_INPUT = By.id("keyword");
    private static final By SEARCH_BUTTON = By.cssSelector("form#form_submit button[type='submit']");
    private static final By RESULTS_HEADING = By.xpath("//h1[normalize-space()='Kết quả tìm kiếm']");
    private static final By RESULT_LINKS = By.cssSelector("p.name a[href$='.html']");
    private static final By EMPTY_RESULTS = By.xpath("//*[normalize-space()='Không thấy kết quả phù hợp']");
    private static final By SUGGESTION_BOX = By.cssSelector(".suggest-search");
    private static final By SUGGESTION_ITEMS = By.cssSelector(".suggest-search-list li");
    private static final Pattern SUGGESTION_PRICE =
            Pattern.compile("(?iu)(?:\\d{1,3}(?:[.,]\\d{3})+\\s*(?:đ|₫|vnđ|vnd)|(?:đ|₫|vnđ|vnd)\\s*\\d+)");

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    public boolean isSearchInputVisible() {
        return isVisible(SEARCH_INPUT);
    }

    public String placeholder() {
        return visible(SEARCH_INPUT).getDomAttribute("placeholder");
    }

    public boolean isEmptyInputRequired() {
        WebElement input = visible(SEARCH_INPUT);
        return input.getDomAttribute("required") != null
                && Boolean.FALSE.equals(((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("return arguments[0].checkValidity()", input));
    }

    public void enterTermWithoutSubmitting(String term) {
        WebElement input = visible(SEARCH_INPUT);
        input.clear();
        input.sendKeys(term);
    }

    public SearchPage submitTerm(String term) {
        enterTermWithoutSubmitting(term);
        clickable(SEARCH_BUTTON).click();
        wait.until(ExpectedConditions.urlContains("/tim-kiem"));
        return this;
    }

    public SearchPage submitTermWithEnter(String term) {
        visible(SEARCH_INPUT).sendKeys(term, Keys.ENTER);
        wait.until(ExpectedConditions.urlContains("/tim-kiem"));
        return this;
    }

    public void submitEmptyTermWithEnter() {
        visible(SEARCH_INPUT).clear();
        visible(SEARCH_INPUT).sendKeys(Keys.ENTER);
    }

    public void clickSearchButtonWithEmptyTerm() {
        visible(SEARCH_INPUT).clear();
        clickable(SEARCH_BUTTON).click();
    }

    public String enteredTerm() {
        return visible(SEARCH_INPUT).getDomProperty("value");
    }

    public boolean hasResultsHeading() {
        return isVisible(RESULTS_HEADING);
    }

    public boolean hasEmptyResultsMessage() {
        return isVisible(EMPTY_RESULTS);
    }

    public boolean hasNoProductResults() {
        return driver.findElements(RESULT_LINKS).stream().noneMatch(WebElement::isDisplayed);
    }

    public boolean hasSuggestionsWithImageAndPrice() {
        boolean suggestionBoxVisible = driver.findElements(SUGGESTION_BOX).stream()
                .anyMatch(WebElement::isDisplayed);
        List<WebElement> suggestions = driver.findElements(SUGGESTION_ITEMS).stream()
                .filter(WebElement::isDisplayed)
                .toList();
        return suggestionBoxVisible && !suggestions.isEmpty()
                && suggestions.stream().allMatch(this::suggestionHasImageAndPrice);
    }

    public boolean hasProductResultMatching(String term) {
        return driver.findElements(RESULT_LINKS).stream()
                .anyMatch(link -> link.getText().toLowerCase(java.util.Locale.ROOT)
                        .contains(term.toLowerCase(java.util.Locale.ROOT)));
    }

    public String firstResultName() {
        return visible(RESULT_LINKS).getText().trim();
    }

    public void openFirstResult() {
        clickable(RESULT_LINKS).click();
    }

    private boolean suggestionHasImageAndPrice(WebElement suggestion) {
        String text = suggestion.getText();
        boolean hasName = !SUGGESTION_PRICE.matcher(text).replaceAll("").isBlank();
        boolean hasLoadedImage = suggestion.findElements(By.tagName("img")).stream()
                .anyMatch(image -> {
                    String source = image.getAttribute("src");
                    String naturalWidth = image.getDomProperty("naturalWidth");
                    return source != null && !source.isBlank()
                            && naturalWidth != null && !"0".equals(naturalWidth);
                });
        return hasName && hasLoadedImage && SUGGESTION_PRICE.matcher(text).find();
    }
}
