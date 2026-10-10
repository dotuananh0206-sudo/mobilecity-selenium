package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.HomePage;
import mobilecity.pages.SearchPage;
import mobilecity.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchTest extends BaseTest {
    @Test
    @DisplayName("TC-SEARCH-001 - Ô tìm kiếm nhận từ khóa mà không gửi form")
    void searchInputAcceptsTermWithoutSubmission() {
        HomePage homePage = new HomePage(driver).open();
        assertTrue(homePage.hasSearchInput(), "Trang công khai cần có ô tìm kiếm quan sát được");

        SearchPage searchPage = homePage.searchPage();
        String sample = TestData.required("search.sample");
        searchPage.enterTermWithoutSubmitting(sample);

        assertEquals(sample, searchPage.enteredTerm(), "Ô tìm kiếm cần giữ từ khóa đã nhập");
        assertTrue(driver.getCurrentUrl().startsWith(TestData.url("base.url")),
                "Không gửi tìm kiếm hoặc điều hướng khỏi trang trong test an toàn này");
    }
}
