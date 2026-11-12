package mobilecity.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public final class CategoryPage extends BasePage {
    private static final By MAIN_CATEGORY_LINKS =
            By.cssSelector(".v3_header_bottom a.v3_catgory_bottom");
    private static final By MENU_PANEL = By.cssSelector("div.sub-menu-type-3");

    public CategoryPage(org.openqa.selenium.WebDriver driver) {
        super(driver);
    }

    public void hoverMainCategory(String categoryName) {
        WebElement trigger = mainCategoryLink(categoryName);
        new Actions(driver).moveToElement(trigger).perform();
        wait.until(ExpectedConditions.visibilityOf(menuFor(trigger)));
    }

    public void openMainCategory(String categoryName) {
        wait.until(ExpectedConditions.elementToBeClickable(mainCategoryLink(categoryName))).click();
    }

    public void clickMainCategoryMenu(String categoryName) {
        mainCategoryLink(categoryName).click();
    }

    public String mainCategoryDestination(String categoryName) {
        return mainCategoryLink(categoryName).getAttribute("href");
    }

    public boolean openMenuContains(String linkText) {
        return wait.until(currentDriver -> {
            WebElement link = visibleMenuLink(linkText);
            return link != null;
        });
    }

    public boolean isMenuLinkVisible(String linkText) {
        return visibleMenuLink(linkText) != null;
    }

    public String menuTextFor(String categoryName) {
        return menuFor(mainCategoryLink(categoryName)).getText();
    }

    public String menuLinkDestination(String linkText) {
        WebElement link = visibleMenuLink(linkText);
        if (link == null) {
            throw new org.openqa.selenium.TimeoutException(
                    "The visible category menu does not contain link: " + linkText);
        }
        return link.getAttribute("href");
    }

    public void openMenuLink(String categoryName, String linkText) {
        hoverMainCategory(categoryName);
        WebElement link = wait.until(currentDriver -> visibleMenuLink(linkText));
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
    }

    public void movePointerOutsideMenu(String categoryName) {
        WebElement menu = menuFor(mainCategoryLink(categoryName));
        new Actions(driver).moveToLocation(0, 0).perform();
        wait.until(ExpectedConditions.invisibilityOf(menu));
    }

    public boolean isMenuClosed(String categoryName) {
        return wait.until(ExpectedConditions.invisibilityOf(menuFor(mainCategoryLink(categoryName))));
    }

    public boolean categoryPageMentions(String text) {
        String bodyText = visible(By.tagName("body")).getText().toLowerCase();
        return bodyText.contains(text.toLowerCase());
    }

    public boolean headerCategoryIsInsideViewport(String categoryName) {
        WebElement link = mainCategoryLink(categoryName);
        if (!link.isDisplayed()) {
            return false;
        }
        long viewportWidth = ((Number) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.documentElement.clientWidth")).longValue();
        return link.getRect().getX() >= 0
                && link.getRect().getX() + link.getRect().getWidth() <= viewportWidth;
    }

    private WebElement mainCategoryLink(String categoryName) {
        return wait.until(currentDriver -> currentDriver.findElements(MAIN_CATEGORY_LINKS).stream()
                .filter(WebElement::isDisplayed)
                .filter(link -> link.getText().trim().equals(categoryName))
                .findFirst()
                .orElse(null));
    }

    private WebElement menuFor(WebElement trigger) {
        WebElement menuItem = trigger.findElement(
                By.xpath("./ancestor::li[contains(@class, 'v3-item-menu')]"));
        return menuItem.findElement(MENU_PANEL);
    }

    private WebElement visibleMenuLink(String linkText) {
        List<WebElement> links = driver.findElements(By.linkText(linkText));
        for (WebElement link : links) {
            if (link.isDisplayed() && link.findElements(By.xpath(
                    "./ancestor::div[contains(@class, 'sub-menu-type-3')]")).size() > 0) {
                return link;
            }
        }
        return null;
    }
}
