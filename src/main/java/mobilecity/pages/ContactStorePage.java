package mobilecity.pages;

import mobilecity.utils.TestData;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ContactStorePage extends BasePage {
    private static final By FOOTER_STORE_MAP_LINKS =
            By.cssSelector("footer a[href*='/lien-he?cs=']");
    private static final By CONTACT_HEADING = By.cssSelector("h1");
    private static final By BRANCH_DETAILS = By.cssSelector(".location_info_item");
    private static final By BRANCH_TABS = By.cssSelector(".location_tab li");
    private static final By REGION_FILTER_CONTROLS = By.cssSelector(
            ".location_tab select, .location_tab [role='combobox'], "
                    + ".location_info select, .location_info [role='combobox']");
    private static final By MAP_FRAME =
            By.cssSelector("iframe[src*='google.com/maps/embed']");
    private static final Pattern PHONE_NUMBER = Pattern.compile("\\+?\\d[\\d\\s().-]{6,}\\d");
    private static final Pattern WORKING_HOURS = Pattern.compile(
            "(?iu)(?:giờ\\s*(?:mở cửa|làm việc)|mở cửa|đóng cửa|"
                    + "\\b\\d{1,2}[:h]\\d{2}\\s*(?:-|đến|to)\\s*\\d{1,2}[:h]\\d{2}\\b)");
    private static final List<String> REGION_NAMES =
            List.of("hà nội", "hồ chí minh", "tp. hồ chí minh", "đà nẵng");

    public ContactStorePage(WebDriver driver) {
        super(driver);
    }

    public ContactStorePage open() {
        driver.get(TestData.url("contact.url"));
        return this;
    }

    public String firstStoreMapDestination() {
        String destination = firstVisibleStoreMapLink().getAttribute("href");
        if (destination == null || destination.isBlank()) {
            throw new IllegalStateException("The first store map link has no destination");
        }
        return destination;
    }

    public void openFirstStoreMapLinkFromFooter() {
        WebElement link = firstVisibleStoreMapLink();
        String destination = link.getAttribute("href");
        clickAndWaitForDestination(link, destination);
        visible(CONTACT_HEADING);
    }

    public boolean hasContactHeading() {
        return isVisible(CONTACT_HEADING)
                && visible(CONTACT_HEADING).getText().toLowerCase(Locale.ROOT)
                        .contains("liên hệ");
    }

    public String activeBranchName() {
        return branchParagraphs().stream()
                .findFirst()
                .map(element -> element.getText().trim())
                .orElse("");
    }

    public String activeBranchAddress() {
        List<WebElement> paragraphs = branchParagraphs();
        return paragraphs.size() > 1 ? paragraphs.get(1).getText().trim() : "";
    }

    public boolean activeBranchHasPhoneNumber() {
        List<WebElement> paragraphs = branchParagraphs();
        return paragraphs.stream().skip(2)
                .map(WebElement::getText)
                .anyMatch(text -> PHONE_NUMBER.matcher(text).find());
    }

    public boolean activeBranchHasWorkingHours() {
        return WORKING_HOURS.matcher(activeBranchDetails().getText()).find();
    }

    public boolean hasStoreRegionFilter() {
        boolean regionTabExists = driver.findElements(BRANCH_TABS).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .anyMatch(ContactStorePage::containsRegionName);
        return regionTabExists || driver.findElements(REGION_FILTER_CONTROLS).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(control -> containsRegionName(control.getText()));
    }

    public boolean hasVisibleMapFrame() {
        return driver.findElements(MAP_FRAME).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(frame -> frame.getRect().getWidth() > 0
                        && frame.getRect().getHeight() > 0);
    }

    public boolean hasLoadedMapControls() {
        WebElement mapFrame = driver.findElements(MAP_FRAME).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
        if (mapFrame == null) {
            return false;
        }

        try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(mapFrame));
            return wait.until(currentDriver ->
                    !currentDriver.findElements(By.cssSelector("button")).isEmpty());
        } catch (TimeoutException timeout) {
            return false;
        } finally {
            driver.switchTo().defaultContent();
        }
    }

    private WebElement firstVisibleStoreMapLink() {
        return wait.until(currentDriver -> currentDriver.findElements(FOOTER_STORE_MAP_LINKS).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null));
    }

    private WebElement activeBranchDetails() {
        return wait.until(currentDriver -> currentDriver.findElements(BRANCH_DETAILS).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null));
    }

    private List<WebElement> branchParagraphs() {
        return activeBranchDetails().findElements(By.tagName("p"));
    }

    private static boolean containsRegionName(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        return REGION_NAMES.stream().anyMatch(normalized::contains);
    }
}
