package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.config.TestConfig;
import mobilecity.pages.CategoryPage;
import mobilecity.pages.HomePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FilterSortTest extends BaseTest {
    // [FILTER & SORT] Lọc danh sách điện thoại theo hãng OPPO.
    @Test
    @DisplayName("TC_FS_01 - Lọc sản phẩm theo hãng")
    void filterProductsByBrand() {
        CategoryPage categoryPage = openPhoneCategory();
        assertTrue(categoryPage.hasFilterTrigger("Hãng"),
                "Thanh lọc cần hiển thị bộ lọc hãng");
        categoryPage.openFilter("Hãng");
        assertTrue(categoryPage.hasFilterOption("OPPO"),
                "Bộ lọc hãng cần có lựa chọn OPPO");
        categoryPage.selectFilterOption("OPPO");

        assertTrue(waitFor(() -> categoryPage.displayedProductsMatchBrand("OPPO")),
                "Danh sách sau lọc chỉ được chứa sản phẩm OPPO");
    }

    // [FILTER & SORT] Lọc điện thoại có giá thấp hơn 5.000.000 VNĐ.
    @Test
    @DisplayName("TC_FS_02 - Lọc sản phẩm theo khoảng giá")
    void filterProductsByPriceRange() {
        CategoryPage categoryPage = openPhoneCategory();
        String priceFilter = categoryPage.availableFilterTrigger("Khoảng giá", "Mức giá");
        assertNotNull(priceFilter, "Thanh lọc cần hiển thị bộ lọc khoảng giá");
        categoryPage.openFilter(priceFilter);
        assertTrue(categoryPage.hasFilterOption("Dưới 5 triệu"),
                "Bộ lọc giá cần có lựa chọn Dưới 5 triệu");
        categoryPage.selectFilterOption("Dưới 5 triệu");

        assertTrue(waitFor(() -> categoryPage.displayedPricesAreBelow(5_000_000)),
                "Danh sách sau lọc chỉ được có giá dưới 5.000.000 VNĐ");
    }

    // [FILTER & SORT] Kết hợp bộ lọc hãng Apple và khoảng giá 10–15 triệu.
    @Test
    @DisplayName("TC_FS_03 - Lọc kết hợp nhiều tiêu chí")
    void filterProductsByBrandAndPriceRange() {
        CategoryPage categoryPage = openPhoneCategory();
        assertTrue(categoryPage.hasFilterTrigger("Hãng"),
                "Thanh lọc cần hiển thị bộ lọc hãng");
        categoryPage.openFilter("Hãng");
        assertTrue(categoryPage.hasFilterOption("Apple"),
                "Bộ lọc hãng cần có lựa chọn Apple");
        categoryPage.selectFilterOption("Apple");

        String priceFilter = categoryPage.availableFilterTrigger("Khoảng giá", "Mức giá");
        assertNotNull(priceFilter, "Thanh lọc cần hiển thị bộ lọc khoảng giá");
        categoryPage.openFilter(priceFilter);
        assertTrue(categoryPage.hasFilterOption("Từ 10 - 15 triệu"),
                "Bộ lọc giá cần có lựa chọn Từ 10 - 15 triệu");
        categoryPage.selectFilterOption("Từ 10 - 15 triệu");

        assertTrue(waitFor(() ->
                        categoryPage.displayedProductsMatchBrandAndPrice("Apple", 10_000_000, 15_000_000)),
                "Danh sách sau lọc chỉ được có sản phẩm Apple trong khoảng 10–15 triệu VNĐ");
    }

    // [FILTER & SORT] Sắp xếp danh sách giá không giảm từ thấp đến cao.
    @Test
    @DisplayName("TC_FS_04 - Sắp xếp giá tăng dần")
    void sortPricesAscending() {
        HomePage homePage = openPhoneListing();
        assumeTrue(homePage.displayedPriceCount() >= 2,
                "BLOCKED: cần ít nhất hai sản phẩm có giá số để kiểm tra sắp xếp");
        homePage.sortProductsByPrice("Giá từ thấp đến cao");
        assumeTrue(homePage.displayedPriceCount() >= 2,
                "BLOCKED: danh sách sau sắp xếp tăng dần không có đủ hai giá số để xác minh");

        assertTrue(waitFor(() -> homePage.displayedPricesAreOrdered(true)),
                "Giá sản phẩm phải theo thứ tự không giảm");
    }

    // [FILTER & SORT] Sắp xếp danh sách giá không tăng từ cao xuống thấp.
    @Test
    @DisplayName("TC_FS_05 - Sắp xếp giá giảm dần")
    void sortPricesDescending() {
        HomePage homePage = openPhoneListing();
        assumeTrue(homePage.displayedPriceCount() >= 2,
                "BLOCKED: cần ít nhất hai sản phẩm có giá số để kiểm tra sắp xếp");
        homePage.sortProductsByPrice("Giá từ cao đến thấp");
        assumeTrue(homePage.displayedPriceCount() >= 2,
                "BLOCKED: danh sách sau sắp xếp giảm dần không có đủ hai giá số để xác minh");

        assertTrue(waitFor(() -> homePage.displayedPricesAreOrdered(false)),
                "Giá sản phẩm phải theo thứ tự không tăng");
    }

    private CategoryPage openPhoneCategory() {
        HomePage homePage = new HomePage(driver).open();
        assumeNoSiteChallenge();
        String destination = homePage.phoneCategoryDestination();
        homePage.openPhoneCategory();
        assumeNoSiteChallenge();
        assertEquals(normalizedUrl(destination), normalizedUrl(driver.getCurrentUrl()),
                "Cần mở trang danh mục Điện thoại trước khi lọc/sắp xếp");
        return new CategoryPage(driver);
    }

    private HomePage openPhoneListing() {
        openPhoneCategory();
        return new HomePage(driver);
    }

    private boolean waitFor(BooleanSupplier condition) {
        try {
            return Boolean.TRUE.equals(new WebDriverWait(driver, TestConfig.waitTimeout())
                    .until(ignored -> condition.getAsBoolean()));
        } catch (TimeoutException timeout) {
            return false;
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
