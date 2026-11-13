package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.HomePage;
import mobilecity.pages.SearchPage;
import mobilecity.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchTest extends BaseTest {
    // [SEARCH] Tìm kiếm tên sản phẩm có dấu bằng phím Enter.
    @Test
    @DisplayName("TC_SR_01 - Tìm kiếm hợp lệ có dấu")
    void searchFindsAccentedProductUsingEnter() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        assertTrue(homePage.hasSearchInput(), "Trang công khai cần có ô tìm kiếm quan sát được");

        String term = TestData.required("search.accented");
        SearchPage searchPage = homePage.searchPage().submitTermWithEnter(term);
        assumeNoSiteChallenge();

        assertTrue(searchPage.hasResultsHeading(), "Trang tìm kiếm cần hiển thị tiêu đề kết quả");
        assertTrue(searchPage.hasProductResultMatching(term),
                "Kết quả cần chứa sản phẩm khớp từ khóa có dấu");
    }

    // [SEARCH] Tìm kiếm từ khóa không dấu bằng nút kính lúp.
    @Test
    @DisplayName("TC_SR_02 - Tìm kiếm hợp lệ không dấu")
    void searchFindsUnaccentedBrandUsingButton() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        String term = TestData.required("search.unaccented");
        SearchPage searchPage = homePage.searchPage().submitTerm(term);
        assumeNoSiteChallenge();

        assertTrue(searchPage.hasResultsHeading(), "Trang tìm kiếm cần hiển thị tiêu đề kết quả");
        assertTrue(searchPage.hasProductResultMatching(term),
                "Kết quả cần nhận diện và hiển thị sản phẩm thuộc hãng Xiaomi");
    }

    // [SEARCH] Kiểm tra gợi ý tự động có tên, ảnh và giá khi đang nhập.
    @Test
    @DisplayName("TC_SR_03 - Hiển thị gợi ý tìm kiếm nhanh")
    void searchShowsQuickSuggestionsWhileTyping() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        SearchPage searchPage = homePage.searchPage();
        searchPage.enterTermWithoutSubmitting(TestData.required("search.suggestion"));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        boolean suggestionsDisplayed;
        try {
            suggestionsDisplayed = wait.until(
                    ignored -> searchPage.hasSuggestionsWithImageAndPrice());
        } catch (org.openqa.selenium.TimeoutException timeout) {
            suggestionsDisplayed = false;
        }

        assertTrue(suggestionsDisplayed,
                "Gợi ý cần hiện trong 1–2 giây và có tên, ảnh cùng giá sản phẩm");
    }

    // [SEARCH] Kiểm tra kết quả rỗng khi từ khóa không tồn tại.
    @Test
    @DisplayName("TC_SR_04 - Tìm kiếm từ khóa không tồn tại")
    void searchShowsNoResultsForUnknownTerm() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        SearchPage searchPage = homePage.searchPage()
                .submitTermWithEnter(TestData.required("search.noMatch"));
        assumeNoSiteChallenge();

        assertTrue(searchPage.hasResultsHeading(), "Trang tìm kiếm cần hiển thị tiêu đề kết quả");
        assertTrue(searchPage.hasEmptyResultsMessage() || searchPage.hasNoProductResults(),
                "Từ khóa không tồn tại cần hiển thị thông báo hoặc danh sách rỗng");
    }

    // [SEARCH] Kiểm tra gửi từ khóa rỗng không điều hướng khỏi trang hiện tại.
    @Test
    @DisplayName("TC_SR_05 - Tìm kiếm với từ khóa rỗng")
    void emptySearchKeepsCurrentPage() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        SearchPage searchPage = homePage.searchPage();
        String homeUrl = driver.getCurrentUrl();
        assertTrue(searchPage.isEmptyInputRequired(),
                "Ô tìm kiếm cần từ chối giá trị rỗng");

        searchPage.submitEmptyTermWithEnter();
        assertEquals(homeUrl, driver.getCurrentUrl(),
                "Nhấn Enter khi ô tìm kiếm rỗng không được điều hướng");
        searchPage.clickSearchButtonWithEmptyTerm();
        assertEquals(homeUrl, driver.getCurrentUrl(),
                "Nhấp tìm kiếm khi ô tìm kiếm rỗng không được điều hướng");
    }
}
