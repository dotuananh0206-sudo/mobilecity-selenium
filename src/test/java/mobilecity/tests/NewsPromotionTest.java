package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.HomePage;
import mobilecity.pages.NewsPromotionPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class NewsPromotionTest extends BaseTest {
    // [NEWS & PROMOTION] Kiểm tra liên kết Tin tức trên thanh menu trang chủ.
    @Test
    @DisplayName("TC_NP_01 - Điều hướng vào trang Tin tức và Khuyến mãi")
    void opensNewsAndPromotionFromHomeHeader() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        NewsPromotionPage newsPage = new NewsPromotionPage(driver);
        String destination = newsPage.headerNewsDestination();

        newsPage.openFromHeader();
        assumeNoSiteChallenge();

        assertEquals(destination, driver.getCurrentUrl(),
                "Liên kết Tin tức cần mở đúng trang danh sách");
        assertTrue(newsPage.hasNewsHeading(), "Trang đích cần có tiêu đề danh sách tin tức");
        assertEquals("Tin tức", newsPage.newsHeading(),
                "Trang đích cần nhận diện chức năng Tin tức");
    }

    // [NEWS & PROMOTION] Kiểm tra tiêu đề, ảnh, ngày đăng và mô tả của bài đang hiển thị.
    @Test
    @DisplayName("TC_NP_02 - Hiển thị danh sách bài viết tin tức")
    void visibleNewsPostShowsRequiredSummary() {
        NewsPromotionPage newsPage = new NewsPromotionPage(driver).openIndex();
        assumeNoSiteChallenge();

        assertTrue(newsPage.hasVisiblePosts(), "Danh sách cần có ít nhất một bài viết đang hiển thị");
        assertTrue(newsPage.firstPostHasRequiredSummary(),
                "Bài viết đầu tiên cần có tiêu đề, ảnh tải được, ngày đăng và mô tả");
    }

    // [NEWS & PROMOTION] Kiểm tra mở bài viết đang hiển thị và nội dung chi tiết.
    @Test
    @DisplayName("TC_NP_03 - Xem nội dung chi tiết bài viết")
    void opensVisibleNewsPostDetail() {
        NewsPromotionPage newsPage = new NewsPromotionPage(driver).openIndex();
        assumeNoSiteChallenge();
        String expectedTitle = newsPage.firstPostTitle();
        String expectedDestination = newsPage.firstPostDestination();

        newsPage.openFirstPost();
        assumeNoSiteChallenge();

        assertEquals(expectedDestination, driver.getCurrentUrl(),
                "Tiêu đề bài viết cần mở đúng URL đã hiển thị trong danh sách");
        assertEquals(expectedTitle, newsPage.articleTitle(),
                "Tiêu đề trang chi tiết cần khớp bài viết đã chọn");
        assertTrue(newsPage.hasArticleBody(), "Trang chi tiết cần có nội dung bài viết");
        assertTrue(newsPage.hasLoadedArticleImage(),
                "Trang chi tiết cần có ít nhất một ảnh minh họa đã tải");
        assertTrue(newsPage.hasNoHorizontalOverflow(),
                "Nội dung bài viết không được tràn ngang viewport");
    }

    // [NEWS & PROMOTION] Kiểm tra lọc danh sách theo chuyên mục khuyến mãi đang có trên website.
    @Test
    @DisplayName("TC_NP_04 - Lọc bài viết theo nhóm chuyên mục con")
    void filtersNewsByVisiblePromotionCategory() {
        NewsPromotionPage newsPage = new NewsPromotionPage(driver).openIndex();
        assumeNoSiteChallenge();
        String category = "Tin khuyến mãi";
        assumeTrue(newsPage.hasCategory(category),
                "BLOCKED: website hiện không hiển thị chuyên mục Tin khuyến mãi");
        String destination = newsPage.categoryDestination(category);

        newsPage.openCategory(category);
        assumeNoSiteChallenge();

        assertEquals(destination, driver.getCurrentUrl(),
                "Chuyên mục cần mở đúng đường dẫn đã hiển thị");
        assertTrue(newsPage.newsHeading().contains(category),
                "Tiêu đề danh sách cần nhận diện chuyên mục đã chọn");
        assertTrue(newsPage.hasVisiblePosts(),
                "Danh sách chuyên mục cần được cập nhật và có bài viết");
    }
}
