package mobilecity.pages;

import mobilecity.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HomePage extends BasePage {
    private static final By HOME_LOGO = By.cssSelector("a[href='https://mobilecity.vn'] img[alt*='logo-mobilecity']");
    private static final By CATEGORY_LINK = By.cssSelector("a[href='https://mobilecity.vn/dien-thoai'], a[href='/dien-thoai']");
    private static final By SEARCH_INPUT = By.id("keyword");
    private static final By HEADER_CATEGORY_LINKS =
            By.cssSelector(".v3_header_bottom a.v3_catgory_bottom");
    private static final By BANNER_LINK =
            By.xpath("//a[.//img[@alt='Phụ kiện điện thoại']]");
    private static final By BANNER_IMAGES = By.cssSelector("a img[alt='hình ảnh banner']");
    private static final By CATEGORY_PRICE_SORT_TRIGGER =
            By.xpath("//li[contains(@class, 'fillter-item')]/a[normalize-space()='Sắp xếp theo giá']");
    private static final By YOUTUBE_FRAME =
            By.cssSelector("iframe[src*='youtube.com/embed/']");
    private static final By PRODUCT_LINKS = By.cssSelector("p.name a[href$='.html']");
    private static final By PRODUCT_PRICES = By.cssSelector("div.product-list-item .price");
    private static final By PRICE_SORT = By.id("order_phone");
    private static final Pattern FIRST_PRICE_NUMBER = Pattern.compile("[0-9][0-9.,]*");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        driver.get(TestConfig.baseUrl());
        wait.until(d -> !"loading".equals(((org.openqa.selenium.JavascriptExecutor) d)
                .executeScript("return document.readyState")));
        return this;
    }

    public boolean hasTitle() {
        return driver.getTitle() != null && !driver.getTitle().isBlank();
    }

    public String title() {
        return driver.getTitle();
    }

    public boolean hasLogo() {
        return isVisible(HOME_LOGO);
    }

    public boolean hasPhoneCategoryLink() {
        return isVisible(CATEGORY_LINK);
    }

    public boolean hasMainContent() {
        return wait.until(currentDriver -> hasDisplayedElement(currentDriver, CATEGORY_LINK)
                && hasVisibleBanner(currentDriver)
                && hasDisplayedElement(currentDriver, YOUTUBE_FRAME)
                && hasDisplayedElement(currentDriver, PRODUCT_LINKS));
    }

    public void openPhoneCategory() {
        clickable(CATEGORY_LINK).click();
    }

    public void openHeaderCategory(String categoryName) {
        visibleHeaderLink(categoryName).click();
    }

    public String headerCategoryDestination(String categoryName) {
        return visibleHeaderLink(categoryName).getAttribute("href");
    }

    public String phoneCategoryDestination() {
        return visibleHeaderLink("Điện thoại").getAttribute("href");
    }

    public String headerLinkDestination(String linkText) {
        return visibleLink(By.linkText(linkText)).getAttribute("href");
    }

    public void openHeaderLink(String linkText) {
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(
                visibleLink(By.linkText(linkText)))).click();
    }

    public void openVisibleLink(String linkText) {
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(
                visibleLink(By.linkText(linkText)))).click();
    }

    public String bannerDestination() {
        return visibleLink(BANNER_LINK).getAttribute("href");
    }

    public boolean hasVisibleAccessoryBanner() {
        return driver.findElements(BANNER_LINK).stream().anyMatch(WebElement::isDisplayed);
    }

    public boolean hasPriceSortControl() {
        return findPriceSortSelect(driver) != null;
    }

    public void openAccessoryBanner() {
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(
                visibleLink(BANNER_LINK))).click();
    }

    public boolean hasVisibleYouTubeFrame() {
        WebElement frame = visible(YOUTUBE_FRAME);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", frame);
        return frame.isDisplayed() && frame.getRect().getWidth() > 0
                && frame.getRect().getHeight() > 0;
    }

    public boolean hasVisibleSupportWidget() {
        return isVisible(By.id("sudovn-btn-wrapper"));
    }

    public void sortProductsByPrice(String order) {
        WebElement homeSort = findPriceSortSelect(driver);
        if (homeSort != null) {
            new Select(homeSort).selectByVisibleText(order);
            return;
        }

        String optionValue = switch (order) {
            case "Giá từ thấp đến cao" -> "thap-den-cao";
            case "Giá từ cao đến thấp" -> "cao-den-thap";
            default -> throw new IllegalArgumentException("Unsupported price sort option: " + order);
        };
        new Actions(driver).moveToElement(visible(CATEGORY_PRICE_SORT_TRIGGER)).perform();
        clickable(By.cssSelector(
                "ul.dropdown-filter.sort-price a.option[value='" + optionValue + "']")).click();
    }

    public boolean displayedPricesAreOrdered(boolean ascending) {
        List<Long> prices = displayedPrices();
        if (prices.size() < 2) {
            return false;
        }
        for (int index = 1; index < prices.size(); index++) {
            int comparison = Long.compare(prices.get(index - 1), prices.get(index));
            if (ascending ? comparison > 0 : comparison < 0) {
                return false;
            }
        }
        return true;
    }

    public int displayedPriceCount() {
        return displayedPrices().size();
    }

    public int displayedProductCount() {
        return (int) driver.findElements(PRODUCT_LINKS).stream()
                .filter(WebElement::isDisplayed)
                .count();
    }

    public boolean hasNoHorizontalOverflow() {
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript("""
                const viewportWidth = window.innerWidth || document.documentElement.clientWidth;
                const selectors = [
                    'main', 'section', '.product-box', '.product-list', '.content', '.container',
                    '.v3_banner', '.v3_product', '.product-category', '.newsl', '.video', '.v3_content'
                ];
                const ignored = new Set(['HEADER', 'NAV', 'DIV']);
                for (const selector of selectors) {
                    const elements = document.querySelectorAll(selector);
                    for (const el of elements) {
                        const style = window.getComputedStyle(el);
                        if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') {
                            continue;
                        }
                        const rect = el.getBoundingClientRect();
                        if (!rect.width && !rect.height) {
                            continue;
                        }
                        const className = (el.className || '').toString();
                        const isIgnoredHeader = className.includes('v3_header') || className.includes('header-main') || className.includes('container');
                        if (isIgnoredHeader) {
                            continue;
                        }
                        if (rect.right > viewportWidth + 10 || rect.left < -10) {
                            return false;
                        }
                    }
                }
                return true;
                """));
    }

    public Double largestContentfulPaintMilliseconds() {
        Object value = ((JavascriptExecutor) driver).executeAsyncScript("""
                const done = arguments[arguments.length - 1];
                if (!window.PerformanceObserver
                    || !PerformanceObserver.supportedEntryTypes.includes('largest-contentful-paint')) {
                    done(null);
                    return;
                }
                const entries = [];
                const observer = new PerformanceObserver(list => entries.push(...list.getEntries()));
                try {
                    observer.observe({ type: 'largest-contentful-paint', buffered: true });
                } catch (error) {
                    observer.disconnect();
                    done(null);
                    return;
                }
                setTimeout(() => {
                    observer.disconnect();
                    done(entries.length ? entries[entries.length - 1].startTime : null);
                }, 1000);
                """);
        return value instanceof Number number ? number.doubleValue() : null;
    }

    public boolean hasSearchInput() {
        return isVisible(SEARCH_INPUT);
    }

    public SearchPage searchPage() {
        visible(SEARCH_INPUT);
        return new SearchPage(driver);
    }

    private WebElement visibleHeaderLink(String linkText) {
        return wait.until(currentDriver -> currentDriver.findElements(HEADER_CATEGORY_LINKS).stream()
                .filter(WebElement::isDisplayed)
                .filter(link -> link.getText().trim().equals(linkText))
                .findFirst()
                .orElse(null));
    }

    private WebElement visibleLink(By locator) {
        return wait.until(currentDriver -> currentDriver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null));
    }

    private boolean hasVisibleBanner(WebDriver currentDriver) {
        return currentDriver.findElements(BANNER_IMAGES).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(image -> image.getRect().getWidth() >= 400
                        && image.getRect().getHeight() >= 120);
    }

    private WebElement findPriceSortSelect(WebDriver currentDriver) {
        List<WebElement> preferred = currentDriver.findElements(PRICE_SORT);
        if (!preferred.isEmpty() && preferred.get(0).isDisplayed()) {
            return preferred.get(0);
        }
        for (WebElement selectElement : currentDriver.findElements(By.tagName("select"))) {
            if (!selectElement.isDisplayed()) {
                continue;
            }
            List<String> options = new Select(selectElement).getOptions().stream()
                    .map(WebElement::getText)
                    .map(String::trim)
                    .toList();
            if (options.contains("Giá từ thấp đến cao")
                    && options.contains("Giá từ cao đến thấp")) {
                return selectElement;
            }
        }
        return null;
    }

    private boolean hasDisplayedElement(WebDriver currentDriver, By locator) {
        return currentDriver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    private List<Long> displayedPrices() {
        List<Long> prices = new ArrayList<>();
        for (WebElement price : driver.findElements(PRODUCT_PRICES)) {
            if (!price.isDisplayed()) {
                continue;
            }
            Matcher matcher = FIRST_PRICE_NUMBER.matcher(price.getText());
            if (matcher.find()) {
                String digits = matcher.group().replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    prices.add(Long.parseLong(digits));
                }
            }
        }
        return prices;
    }
}
