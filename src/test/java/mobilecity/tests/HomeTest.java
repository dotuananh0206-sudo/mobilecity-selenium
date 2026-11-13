package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.config.TestConfig;
import mobilecity.pages.CategoryPage;
import mobilecity.pages.HomePage;
import mobilecity.pages.ProductPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class HomeTest extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeTest.class);

    // [HOME] Kiểm tra trang chủ tải các vùng nội dung cốt lõi.
    @Test
    @DisplayName("TC_01 - Tải giao diện trang chủ mặc định")
    void testTC01_LoadHomePage() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        ProductPage products = new ProductPage(driver);

        assertTrue(homePage.hasTitle(), "Trang chủ cần có tiêu đề");
        assertTrue(homePage.hasLogo(), "Trang chủ cần hiển thị logo MobileCity");
        assertTrue(homePage.hasMainContent(),
                "Trang chủ cần hiển thị banner, video, danh mục và danh sách sản phẩm");
        assertTrue(homePage.hasSearchInput(), "Header cần hiển thị ô tìm kiếm");
        assertTrue(!products.firstListedProductName().isBlank(),
                "Thẻ sản phẩm cần có tên");
        assertTrue(products.firstListedProductPrice().matches("(?s).*\\d.*"),
                "Thẻ sản phẩm cần có giá công khai");
        assertTrue(homePage.hasVisibleSupportWidget(),
                "Trang chủ cần hiển thị tiện ích hỗ trợ khách hàng");
    }

    // [CATEGORY] Kiểm tra nhấp tên danh mục gốc mở trang tổng hợp Điện thoại.
    @Test
    @DisplayName("DM_05 - Nhấp trực tiếp vào tên danh mục gốc trên Header")
    void testDM05_OpenPhoneCategoryRoot() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        String destination = homePage.phoneCategoryDestination();
        homePage.openPhoneCategory();
        assumeNoSiteChallenge();
        ProductPage products = new ProductPage(driver);

        assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                "Nhấp danh mục gốc cần mở đúng trang Điện thoại");
        assertTrue(new CategoryPage(driver).categoryPageMentions("Điện thoại"),
                "Trang đích cần nhận diện danh mục Điện thoại");
        assertTrue(!products.firstListedProductName().isBlank(),
                "Danh mục cần hiển thị tên sản phẩm");
        assertTrue(products.firstListedProductPrice().matches("(?s).*\\d.*"),
                "Thẻ sản phẩm cần hiển thị giá công khai");
    }

    // [HOME] Kiểm tra các liên kết danh mục chính mở đúng trang theo mục được chọn.
    @Test
    @DisplayName("TC_03 - Điều hướng thanh menu danh mục chính")
    void testTC03_NavigateMainCategories() {
        List<String> categories = List.of("Máy tính bảng", "Tai nghe", "Sửa chữa");
        CategoryPage categoryPage = new CategoryPage(driver);

        for (String category : categories) {
            HomePage homePage = new HomePage(driver).open();
            assumeNoSiteChallenge();
            String destination = homePage.headerCategoryDestination(category);
            homePage.openHeaderCategory(category);
            assumeNoSiteChallenge();

            assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                    "Liên kết Header cần mở đúng đích của danh mục " + category);
            assertTrue(categoryPage.categoryPageMentions(category),
                    "Trang đích cần có nội dung nhận diện danh mục " + category);
        }
    }

    // [HOME] Kiểm tra Header cung cấp đúng lối vào Đăng nhập và Đăng ký.
    @Test
    @DisplayName("TC_04 - Liên kết Đăng nhập/Đăng ký từ Header")
    void testTC04_HeaderAuthenticationLinks() {
        for (String linkText : List.of("Đăng nhập", "Đăng ký")) {
            HomePage homePage = new HomePage(driver).open();
            assumeNoSiteChallenge();
            String destination = homePage.headerLinkDestination(linkText);

            assertTrue(URI.create(destination).getPath().matches(".*/(login|register)/?"),
                    "Header cần liên kết đến trang " + linkText);
            homePage.openHeaderLink(linkText);
            assumeNoSiteChallenge();

            assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                    "Liên kết " + linkText + " cần mở đúng đích");
        }
    }

    // [HOME] Kiểm tra banner Phụ kiện đang hiển thị điều hướng đến nội dung Phụ kiện.
    @Test
    @DisplayName("TC_05 - Điều hướng từ banner trang chủ")
    void testTC05_AccessoryBannerNavigation() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        assumeTrue(homePage.hasVisibleAccessoryBanner(),
                "BLOCKED: banner Phụ kiện không hiển thị/không nhận diện được ở lần chạy này");
        String destination = homePage.bannerDestination();

        assertTrue(URI.create(destination).getPath().contains("/phu-kien"),
                "Banner Phụ kiện cần liên kết đến nội dung thuộc Phụ kiện");
        homePage.openAccessoryBanner();
        assumeNoSiteChallenge();

        assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                "Nhấp banner cần mở đúng đích liên kết");
    }

    // [HOME] Kiểm tra iframe YouTube hiện diện và có kích thước hiển thị.
    @Test
    @DisplayName("TC_06 - Hiển thị và tải khung video YouTube")
    void testTC06_EmbeddedYouTubeFrameIsVisible() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();

        assertTrue(homePage.hasVisibleYouTubeFrame(),
                "Iframe YouTube cần tồn tại và có kích thước hiển thị khác 0");
    }

    // [HOME] Kiểm tra lối tắt Samsung Chính hãng mở đúng danh mục thương hiệu.
    @Test
    @DisplayName("TC_07 - Mở danh mục từ lối tắt hãng trên Home")
    void testTC07_OpenSamsungOfficialCategory() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        String destination = homePage.headerLinkDestination("Samsung Chính hãng");
        homePage.openVisibleLink("Samsung Chính hãng");
        assumeNoSiteChallenge();

        assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                "Liên kết Samsung Chính hãng cần mở đúng danh mục");
        assertTrue(new CategoryPage(driver).categoryPageMentions("Samsung"),
                "Trang đích cần nhận diện thương hiệu Samsung");
    }

    // [HOME] Kiểm tra sắp xếp giá tăng dần từ Home và giảm dần trong danh mục Điện thoại.
    @Test
    @DisplayName("TC_08 - Sắp xếp sản phẩm theo giá trên Home")
    void testTC08_SortProductsByPrice() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        WebDriverWait wait = new WebDriverWait(driver, TestConfig.waitTimeout());

        assertTrue(homePage.hasMainContent(),
                "Trang chủ cần tải danh sách sản phẩm trước khi sắp xếp");
        assumeTrue(homePage.hasPriceSortControl(),
                "BLOCKED: không tìm thấy bộ sắp xếp giá trên nội dung trang chủ hiện tại");
        String categoryDestination = homePage.phoneCategoryDestination();
        homePage.sortProductsByPrice("Giá từ thấp đến cao");
        wait.until(currentDriver -> normalizedUrl(currentDriver.getCurrentUrl())
                .equals(normalizedUrl(categoryDestination)));
        assertEquals(normalizedUrl(categoryDestination), normalizedUrl(driver.getCurrentUrl()),
                "Sắp xếp từ thấp đến cao cần mở danh sách Điện thoại");
        assumeTrue(homePage.displayedPriceCount() >= 2,
                "BLOCKED: cần ít nhất hai sản phẩm có giá số để xác minh thứ tự tăng dần");
        assertTrue(Boolean.TRUE.equals(wait.until(ignored -> homePage.displayedPricesAreOrdered(true))),
                "Giá sản phẩm cần được sắp xếp tăng dần");

        homePage.sortProductsByPrice("Giá từ cao đến thấp");
        assumeTrue(homePage.displayedPriceCount() >= 2,
                "BLOCKED: cần ít nhất hai sản phẩm có giá số để xác minh thứ tự giảm dần");
        assertTrue(Boolean.TRUE.equals(wait.until(ignored -> homePage.displayedPricesAreOrdered(false))),
                "Giá sản phẩm cần được sắp xếp giảm dần");
    }

    // [HOME] Kiểm tra Home có thể sử dụng ở kích thước Desktop và viewport điện thoại.
    @Test
    @DisplayName("TC_11 - Hiển thị trang chủ theo kích thước viewport")
    void testTC11_HomePageResponsiveViewport() {
        Dimension originalSize = driver.manage().window().getSize();
        try {
            driver.manage().window().setSize(new Dimension(1366, 768));
            HomePage homePage = new HomePage(driver).open();
            assumeNoSiteChallenge();

            assertTrue(homePage.hasNoHorizontalOverflow(),
                    "Nội dung trang chủ không được tràn ngang ở viewport Desktop");
            long desktopViewportWidth = ((Number) ((JavascriptExecutor) driver)
                    .executeScript("return document.documentElement.clientWidth")).longValue();

            driver.manage().window().setSize(new Dimension(390, 844));
            WebDriverWait wait = new WebDriverWait(driver, TestConfig.waitTimeout());
            assertTrue(Boolean.TRUE.equals(wait.until(currentDriver ->
                            ((Number) ((JavascriptExecutor) currentDriver)
                                    .executeScript("return document.documentElement.clientWidth")).longValue()
                                    < desktopViewportWidth)),
                    "Trình duyệt cần thu hẹp viewport trước khi kiểm tra bố cục điện thoại");
            assertTrue(homePage.hasNoHorizontalOverflow(),
                    "Nội dung trang chủ không được tràn ngang ở viewport điện thoại");
            assertTrue(new CategoryPage(driver).headerCategoryIsInsideViewport("Điện thoại"),
                    "Liên kết danh mục chính cần nằm trọn trong viewport điện thoại");
        } finally {
            driver.manage().window().setSize(originalSize);
        }
    }

    // [HOME] Đo LCP qua năm lần tải độc lập với ngưỡng nhỏ hơn 2.5 giây.
    @Test
    @DisplayName("TC_12 - Đo thời gian hiển thị nội dung chính Home")
    void testTC12_HomeLargestContentfulPaint() {
        List<Double> measurements = new ArrayList<>();
        for (int run = 0; run < 5; run++) {
            HomePage homePage = new HomePage(driver).open();
            assumeNoSiteChallenge();
            Double lcp = homePage.largestContentfulPaintMilliseconds();
            assertNotNull(lcp, "Trình duyệt cần hỗ trợ thu thập Largest Contentful Paint");
            measurements.add(lcp);
        }

        LOGGER.info("TC_12 LCP samples in milliseconds: {}", measurements);
        for (Double lcp : measurements) {
            assertTrue(lcp < 2500.0,
                    "Mỗi lần tải cần có LCP dưới 2500 ms; giá trị đo được: " + lcp + " ms");
        }
    }

    private static String normalizedUrl(String url) {
        URI uri = URI.create(url).normalize();
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return uri.getScheme() + "://" + uri.getAuthority() + path;
    }
}
