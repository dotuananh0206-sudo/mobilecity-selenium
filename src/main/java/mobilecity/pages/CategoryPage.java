package mobilecity.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CategoryPage extends BasePage {
    private static final By MAIN_CATEGORY_LINKS =
            By.cssSelector(".v3_header_bottom a.v3_catgory_bottom, a.v3_catgory_bottom");
    private static final By MENU_PANEL = By.cssSelector("div.sub-menu-type-3, .sub-menu-type-3, .menu-dropdown");
    private static final By FILTER_ITEMS = By.cssSelector("li.fillter-item");
    private static final By FILTER_TRIGGERS =
            By.xpath("./a[not(contains(@class, 'option'))] | ./button | .//*[@role='button']");
    private static final By FILTER_OPTIONS =
            By.cssSelector(".dropdown-filter a, .dropdown-filter button, .dropdown-filter label, "
                    + ".dropdown-filter [role='option'], .dropdown-filter [role='checkbox']");
    private static final By PRODUCT_CARDS = By.cssSelector("div.product-list-item");
    private static final By PRODUCT_NAME = By.cssSelector("p.name a[href$='.html']");
    private static final By PRODUCT_PRICE = By.cssSelector(".price");
    private static final Pattern PRICE_NUMBER = Pattern.compile("[0-9][0-9.,]*");

    public CategoryPage(org.openqa.selenium.WebDriver driver) {
        super(driver);
    }

    public void hoverMainCategory(String categoryName) {
        WebElement trigger = mainCategoryLink(categoryName);
        new Actions(driver).moveToElement(trigger).perform();
        WebElement menu = menuFor(trigger);
        if (menu != null) {
            wait.until(ExpectedConditions.visibilityOf(menu));
        }
    }

    public void openMainCategory(String categoryName) {
        wait.until(ExpectedConditions.elementToBeClickable(mainCategoryLink(categoryName))).click();
    }

    public void clickMainCategoryMenu(String categoryName) {
        WebElement trigger = mainCategoryLink(categoryName);
        String originalUrl = driver.getCurrentUrl();
        try {
            trigger.click();
        } catch (RuntimeException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", trigger);
        }
        wait.until(currentDriver -> {
            if (visibleMenuLink("iPhone Chính hãng VN/A") != null) {
                return true;
            }
            String currentUrl = currentDriver.getCurrentUrl();
            return !currentUrl.equals(originalUrl) && currentUrl.toLowerCase().contains("dien-thoai");
        });
    }

    public String mainCategoryDestination(String categoryName) {
        return mainCategoryLink(categoryName).getAttribute("href");
    }

    public boolean openMenuContains(String linkText) {
        return wait.until(currentDriver -> visibleMenuLink(linkText) != null);
    }

    public boolean isMenuLinkVisible(String linkText) {
        return wait.until(currentDriver -> {
            if (visibleMenuLink(linkText) != null) {
                return true;
            }
            return currentDriver.findElements(By.xpath("//a[normalize-space(.)='" + linkText + "']")).stream()
                    .anyMatch(link -> link.getAttribute("href") != null && !link.getAttribute("href").isBlank());
        });
    }

    public String menuTextFor(String categoryName) {
        WebElement menu = menuFor(mainCategoryLink(categoryName));
        return menu != null ? menu.getText() : "";
    }

    public String menuLinkDestination(String linkText) {
        WebElement link = visibleMenuLink(linkText);
        if (link == null) {
            throw new org.openqa.selenium.TimeoutException(
                    "The visible category menu does not contain link: " + linkText);
        }
        return link.getAttribute("href");
    }

    public void openVisibleMenuLink(String linkText) {
        WebElement link = wait.until(currentDriver -> visibleMenuLink(linkText));
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
    }

    public void openMenuLink(String categoryName, String linkText) {
        hoverMainCategory(categoryName);
        WebElement link = wait.until(currentDriver -> visibleMenuLink(linkText));
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
    }

    public void movePointerOutsideMenu(String categoryName) {
        WebElement menu = menuFor(mainCategoryLink(categoryName));
        if (menu != null) {
            new Actions(driver).moveToLocation(0, 0).perform();
            wait.until(ExpectedConditions.invisibilityOf(menu));
        }
    }

    public boolean isMenuClosed(String categoryName) {
        WebElement menu = menuFor(mainCategoryLink(categoryName));
        return menu == null || wait.until(ExpectedConditions.invisibilityOf(menu));
    }

    public boolean categoryPageMentions(String text) {
        String bodyText = visible(By.tagName("body")).getText().toLowerCase();
        return bodyText.contains(text.toLowerCase());
    }

    public String availableFilterTrigger(String... labels) {
        for (String label : labels) {
            if (findFilterTrigger(label) != null) {
                return label;
            }
        }
        return null;
    }

    public boolean hasFilterTrigger(String label) {
        return findFilterTrigger(label) != null;
    }

    public void openFilter(String label) {
        WebElement trigger = wait.until(currentDriver -> findFilterTrigger(label));
        wait.until(ExpectedConditions.elementToBeClickable(trigger)).click();
    }

    public boolean hasFilterOption(String label) {
        return findFilterOption(label) != null;
    }

    public void selectFilterOption(String label) {
        WebElement option = wait.until(currentDriver -> findFilterOption(label));
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    public boolean displayedProductsMatchBrand(String brand) {
        List<WebElement> cards = displayedProductCards();
        return !cards.isEmpty() && cards.stream().allMatch(card -> {
            WebElement name = card.findElement(PRODUCT_NAME);
            return productMatchesBrand(name, brand);
        });
    }

    public boolean displayedPricesAreBelow(long upperBound) {
        List<WebElement> cards = displayedProductCards();
        return !cards.isEmpty() && cards.stream().allMatch(card -> {
            Long price = displayedPrice(card);
            return price != null && price < upperBound;
        });
    }

    public boolean displayedProductsMatchBrandAndPrice(String brand, long lowerBound, long upperBound) {
        List<WebElement> cards = displayedProductCards();
        return !cards.isEmpty() && cards.stream().allMatch(card -> {
            WebElement name = card.findElement(PRODUCT_NAME);
            Long price = displayedPrice(card);
            return productMatchesBrand(name, brand)
                    && price != null
                    && price >= lowerBound
                    && price <= upperBound;
        });
    }

    public boolean headerCategoryIsInsideViewport(String categoryName) {
        List<WebElement> links = driver.findElements(By.xpath(
                "//a[normalize-space(.)='" + categoryName + "']"));
        for (WebElement link : links) {
            if (!link.isDisplayed()) {
                continue;
            }
            var rect = link.getRect();
            long viewportWidth = ((Number) ((JavascriptExecutor) driver)
                    .executeScript("return document.documentElement.clientWidth")).longValue();
            if (rect.getX() >= 0 && rect.getX() + rect.getWidth() <= viewportWidth + 1) {
                return true;
            }
        }
        return false;
    }

    private WebElement mainCategoryLink(String categoryName) {
        return wait.until(currentDriver -> {
            List<WebElement> directMatches = currentDriver.findElements(MAIN_CATEGORY_LINKS).stream()
                    .filter(link -> normalize(link.getText()).equals(normalize(categoryName)))
                    .toList();
            if (!directMatches.isEmpty()) {
                return directMatches.get(0);
            }
            return currentDriver.findElements(By.xpath("//a[normalize-space(.)='" + categoryName + "']")).stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(null);
        });
    }

    private WebElement menuFor(WebElement trigger) {
        List<WebElement> containers = trigger.findElements(By.xpath("./ancestor::*[contains(@class, 'v3-item-menu') or contains(@class, 'menu-item')][1]"));
        if (!containers.isEmpty()) {
            WebElement menuContainer = containers.get(0);
            List<WebElement> menus = menuContainer.findElements(MENU_PANEL);
            if (!menus.isEmpty()) {
                return menus.get(0);
            }
        }
        return driver.findElements(MENU_PANEL).stream().findFirst().orElse(null);
    }

    private WebElement visibleMenuLink(String linkText) {
        List<WebElement> candidates = driver.findElements(By.xpath(
                "//a[normalize-space(.)='" + linkText + "'] | //option[normalize-space(.)='" + linkText + "']"));
        for (WebElement link : candidates) {
            if (!link.isDisplayed()) {
                continue;
            }
            if (isInMenuPanel(link)) {
                return link;
            }
        }
        return null;
    }

    private boolean isInMenuPanel(WebElement link) {
        List<WebElement> ancestors = link.findElements(By.xpath("./ancestor::*[contains(@class, 'sub-menu-type-3') or contains(@class, 'menu-dropdown') or contains(@class, 'menu')][1]"));
        return !ancestors.isEmpty() || link.getAttribute("data-link") != null;
    }

    private WebElement findFilterTrigger(String label) {
        for (WebElement item : driver.findElements(FILTER_ITEMS)) {
            if (!item.isDisplayed()) {
                continue;
            }
            for (WebElement trigger : item.findElements(FILTER_TRIGGERS)) {
                if (trigger.isDisplayed() && normalize(trigger.getText()).equals(normalize(label))) {
                    return trigger;
                }
            }
        }
        return null;
    }

    private WebElement findFilterOption(String label) {
        for (WebElement item : driver.findElements(FILTER_ITEMS)) {
            for (WebElement option : item.findElements(FILTER_OPTIONS)) {
                if (option.isDisplayed() && normalize(option.getText()).equals(normalize(label))) {
                    return option;
                }
            }
        }
        return null;
    }

    private List<WebElement> displayedProductCards() {
        return driver.findElements(PRODUCT_CARDS).stream()
                .filter(WebElement::isDisplayed)
                .filter(card -> !card.findElements(PRODUCT_NAME).isEmpty())
                .toList();
    }

    private Long displayedPrice(WebElement card) {
        List<WebElement> priceElements = card.findElements(PRODUCT_PRICE);
        if (priceElements.isEmpty()) {
            return null;
        }
        Matcher matcher = PRICE_NUMBER.matcher(priceElements.get(0).getText());
        if (!matcher.find()) {
            return null;
        }
        String digits = matcher.group().replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : Long.parseLong(digits);
    }

    private boolean productMatchesBrand(WebElement productName, String brand) {
        String name = normalize(productName.getText()).toLowerCase(Locale.ROOT);
        String href = productName.getAttribute("href");
        String expected = brand.equalsIgnoreCase("Apple") ? "iphone" : brand.toLowerCase(Locale.ROOT);
        return name.contains(expected)
                || (href != null && href.toLowerCase(Locale.ROOT).contains(expected));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
