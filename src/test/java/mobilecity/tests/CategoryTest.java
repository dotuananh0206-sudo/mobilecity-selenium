package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.CategoryPage;
import mobilecity.pages.HomePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryTest extends BaseTest {
    // [CATEGORY] Kiểm tra menu Điện thoại mở ra và hiển thị nhóm hãng/phân loại.
    @Test
    @DisplayName("DM_01 - Hiển thị menu danh mục khi rê chuột")
    void testDM01_HoverOpensCategoryMenu() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        CategoryPage categoryPage = new CategoryPage(driver);

        categoryPage.hoverMainCategory("Điện thoại");

        assertTrue(categoryPage.openMenuContains("iPhone Chính hãng VN/A"),
                "Menu Điện thoại cần có liên kết hãng/phân loại");
        assertTrue(categoryPage.menuTextFor("Điện thoại").contains("Hãng sản xuất"),
                "Menu cần hiển thị nhóm Hãng sản xuất");
    }

    // [CATEGORY] Kiểm tra chọn iPhone Chính hãng VN/A mở đúng danh mục đích.
    @Test
    @DisplayName("DM_02 - Điều hướng đến danh mục hãng sản xuất chính hãng")
    void testDM02_OpenOfficialIPhoneCategory() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        CategoryPage categoryPage = new CategoryPage(driver);
        categoryPage.hoverMainCategory("Điện thoại");
        String destination = categoryPage.menuLinkDestination("iPhone Chính hãng VN/A");
        categoryPage.openMenuLink("Điện thoại", "iPhone Chính hãng VN/A");
        assumeNoSiteChallenge();

        assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                "Menu cần mở đúng đích liên kết iPhone Chính hãng VN/A");
        assertTrue(categoryPage.categoryPageMentions("iPhone"),
                "Trang đích cần nhận diện danh mục iPhone");
    }

    // [CATEGORY] Kiểm tra menu cũ được thay bằng menu của danh mục mới khi rê chuột.
    @Test
    @DisplayName("DM_03 - Chuyển đổi giữa các menu danh mục chính")
    void testDM03_SwitchMainCategoryMenus() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        CategoryPage categoryPage = new CategoryPage(driver);

        categoryPage.hoverMainCategory("Điện thoại");
        assertTrue(categoryPage.menuTextFor("Điện thoại").contains("iPhone Chính hãng VN/A"),
                "Menu Điện thoại cần hiển thị liên kết iPhone");

        categoryPage.hoverMainCategory("Máy tính bảng");
        String tabletMenu = categoryPage.menuTextFor("Máy tính bảng");
        assertTrue(tabletMenu.contains("iPad"),
                "Menu Máy tính bảng cần hiển thị liên kết iPad");
        assertTrue(!tabletMenu.contains("iPhone Chính hãng VN/A"),
                "Menu Điện thoại cũ không được giữ trong menu Máy tính bảng");
    }

    // [CATEGORY] Kiểm tra menu tự ẩn khi con trỏ rời khỏi vùng danh mục.
    @Test
    @DisplayName("DM_04 - Tự động đóng menu khi di chuột ra ngoài")
    void testDM04_CategoryMenuClosesOutside() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        CategoryPage categoryPage = new CategoryPage(driver);
        categoryPage.hoverMainCategory("Điện thoại");
        assertTrue(categoryPage.openMenuContains("iPhone Chính hãng VN/A"),
                "Menu phải mở trước khi kiểm tra đóng");

        categoryPage.movePointerOutsideMenu("Điện thoại");

        assertTrue(categoryPage.isMenuClosed("Điện thoại"),
                "Menu cần ẩn sau khi con trỏ rời khu vực menu");
    }

    // [CATEGORY] Kiểm tra phân loại Điện thoại Cũ mở đúng trang danh sách.
    @Test
    @DisplayName("DM_06 - Điều hướng đến danh mục dòng máy cũ")
    void testDM06_OpenUsedPhoneCategory() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        CategoryPage categoryPage = new CategoryPage(driver);
        categoryPage.hoverMainCategory("Điện thoại");
        String destination = categoryPage.menuLinkDestination("Điện thoại Cũ");
        categoryPage.openMenuLink("Điện thoại", "Điện thoại Cũ");
        assumeNoSiteChallenge();

        assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                "Menu cần mở đúng đích Điện thoại Cũ");
        assertTrue(categoryPage.categoryPageMentions("cũ")
                        || driver.getTitle().toLowerCase().contains("cũ"),
                "Trang đích cần nhận diện phân loại điện thoại cũ");
    }

    // [CATEGORY] Kiểm tra điều hướng từ menu hãng đến danh mục Xiaomi.
    @Test
    @DisplayName("DM_07 - Điều hướng đến thương hiệu Android Xiaomi")
    void testDM07_OpenXiaomiCategory() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        CategoryPage categoryPage = new CategoryPage(driver);
        categoryPage.hoverMainCategory("Điện thoại");
        String destination = categoryPage.menuLinkDestination("Xiaomi");
        categoryPage.openMenuLink("Điện thoại", "Xiaomi");
        assumeNoSiteChallenge();

        assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                "Menu cần mở đúng đích danh mục Xiaomi");
        assertTrue(categoryPage.categoryPageMentions("Xiaomi"),
                "Trang đích cần nhận diện thương hiệu Xiaomi");
    }

    // [CATEGORY] Kiểm tra menu danh mục có thể được mở và sử dụng ở viewport điện thoại.
    @Test
    @DisplayName("DM_10 - Menu danh mục trên viewport điện thoại")
    void testDM10_CategoryMenuOnMobileViewport() {
        Dimension originalSize = driver.manage().window().getSize();
        try {
            driver.manage().window().setSize(new Dimension(390, 844));
            new HomePage(driver).open();
            assumeNoSiteChallenge();
            CategoryPage categoryPage = new CategoryPage(driver);

            assertTrue(categoryPage.headerCategoryIsInsideViewport("Điện thoại"),
                    "Danh mục Điện thoại cần nằm trong viewport điện thoại");
            categoryPage.clickMainCategoryMenu("Điện thoại");
            assertTrue(categoryPage.openMenuContains("iPhone Chính hãng VN/A"),
                    "Menu Điện thoại cần mở và hiển thị liên kết con ở viewport điện thoại");
            String destination = categoryPage.menuLinkDestination("iPhone Chính hãng VN/A");
            categoryPage.openVisibleMenuLink("iPhone Chính hãng VN/A");
            assumeNoSiteChallenge();

            assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                    "Chọn liên kết con cần mở đúng danh mục ở viewport điện thoại");
        } finally {
            driver.manage().window().setSize(originalSize);
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
