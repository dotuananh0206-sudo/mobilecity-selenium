package mobilecity.pages;

import mobilecity.utils.TestData;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public final class NewsPromotionPage extends BasePage {
    private static final By HEADER_NEWS_LINK = By.cssSelector("a[href$='/tin-tuc']");
    private static final By NEWS_HEADING = By.cssSelector("h1.intro-category-title");
    private static final By NEWS_CARDS = By.cssSelector(".news-content-item");
    private static final By POST_TITLE_LINK = By.cssSelector("h3.name a[href*='/tin-tuc/']");
    private static final By POST_IMAGE = By.cssSelector(".img img");
    private static final By POST_DATE = By.cssSelector(".infor-meta .time");
    private static final By POST_EXCERPT = By.cssSelector(".description");
    private static final By CATEGORY_LINKS = By.cssSelector(".categories-list a[href]");
    private static final By ARTICLE_TITLE = By.cssSelector("h1.news-content-detail-title");
    private static final By ARTICLE_CONTENT = By.cssSelector(".news-content-list.news-content-detail");

    public NewsPromotionPage(WebDriver driver) {
        super(driver);
    }

    public NewsPromotionPage openIndex() {
        driver.get(TestData.url("news.url"));
        return this;
    }

    public String headerNewsDestination() {
        return visible(HEADER_NEWS_LINK).getAttribute("href");
    }

    public void openFromHeader() {
        String destination = headerNewsDestination();
        clickable(HEADER_NEWS_LINK).click();
        wait.until(ExpectedConditions.urlToBe(destination));
    }

    public boolean hasNewsHeading() {
        return isVisible(NEWS_HEADING);
    }

    public String newsHeading() {
        return visible(NEWS_HEADING).getText().trim();
    }

    public boolean hasVisiblePosts() {
        return !visiblePostCards().isEmpty();
    }

    public String firstPostTitle() {
        return firstPostCard().findElement(POST_TITLE_LINK).getText().trim();
    }

    public String firstPostDestination() {
        String destination = firstPostCard().findElement(POST_TITLE_LINK).getAttribute("href");
        if (destination == null || destination.isBlank()) {
            throw new IllegalStateException("The first news post has no destination");
        }
        return destination;
    }

    public boolean firstPostHasRequiredSummary() {
        WebElement card = firstPostCard();
        String title = card.findElement(POST_TITLE_LINK).getText().trim();
        String date = card.findElement(POST_DATE).getText().trim();
        String excerpt = card.findElement(POST_EXCERPT).getText().trim();
        List<WebElement> images = card.findElements(POST_IMAGE);
        if (title.isBlank() || date.isBlank() || excerpt.isBlank() || images.isEmpty()) {
            return false;
        }

        WebElement image = images.get(0);
        String source = image.getAttribute("src");
        String naturalWidth = image.getDomProperty("naturalWidth");
        return source != null && !source.isBlank()
                && naturalWidth != null && !"0".equals(naturalWidth);
    }

    public void openFirstPost() {
        String destination = firstPostDestination();
        wait.until(ExpectedConditions.elementToBeClickable(
                firstPostCard().findElement(POST_TITLE_LINK))).click();
        wait.until(ExpectedConditions.urlToBe(destination));
    }

    public String articleTitle() {
        return visible(ARTICLE_TITLE).getText().trim();
    }

    public boolean hasArticleBody() {
        return driver.findElements(ARTICLE_CONTENT).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(content -> content.getText().trim().length() > articleTitle().length());
    }

    public boolean hasLoadedArticleImage() {
        return wait.until(currentDriver -> currentDriver.findElements(
                        ARTICLE_CONTENT).stream()
                .filter(WebElement::isDisplayed)
                .flatMap(content -> content.findElements(By.tagName("img")).stream())
                .anyMatch(image -> {
                    String naturalWidth = image.getDomProperty("naturalWidth");
                    return naturalWidth != null && !"0".equals(naturalWidth);
                }));
    }

    public boolean hasNoHorizontalOverflow() {
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "return document.documentElement.scrollWidth <= document.documentElement.clientWidth"));
    }

    public boolean hasCategory(String categoryName) {
        return findCategoryLink(categoryName) != null;
    }

    public String categoryDestination(String categoryName) {
        WebElement link = findCategoryLink(categoryName);
        if (link == null) {
            throw new IllegalStateException("The requested news category is not available: "
                    + categoryName);
        }
        String destination = link.getAttribute("href");
        if (destination == null || destination.isBlank()) {
            throw new IllegalStateException("The requested news category has no destination: "
                    + categoryName);
        }
        return destination;
    }

    public void openCategory(String categoryName) {
        String destination = categoryDestination(categoryName);
        wait.until(ExpectedConditions.elementToBeClickable(findCategoryLink(categoryName))).click();
        wait.until(ExpectedConditions.urlToBe(destination));
        visible(NEWS_HEADING);
    }

    private WebElement firstPostCard() {
        return visiblePostCards().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("The news list has no visible posts"));
    }

    private List<WebElement> visiblePostCards() {
        return driver.findElements(NEWS_CARDS).stream()
                .filter(WebElement::isDisplayed)
                .filter(card -> !card.findElements(POST_TITLE_LINK).isEmpty())
                .toList();
    }

    private WebElement findCategoryLink(String categoryName) {
        return driver.findElements(CATEGORY_LINKS).stream()
                .filter(WebElement::isDisplayed)
                .filter(link -> link.getText().trim().equals(categoryName))
                .findFirst()
                .orElse(null);
    }
}
