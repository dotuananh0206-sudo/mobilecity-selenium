package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.config.TestConfig;
import mobilecity.pages.ProductPage;
import mobilecity.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ProductTest extends BaseTest {
    // [PRODUCT] Kiểm tra mở đúng trang chi tiết khi chọn sản phẩm trong danh mục.
    @Test
    @DisplayName("TC_PR_01 - Điều hướng vào trang chi tiết")
    void listedProductOpensMatchingDetailPage() {
        driver.get(TestData.url("category.url"));
        assumeNoSiteChallenge();
        ProductPage products = new ProductPage(driver);
        String productName = products.firstListedProductName();
        String productUrl = products.firstListedProductUrl();
        String categoryUrl = driver.getCurrentUrl();
        products.openFirstListedProduct();
        assumeNoSiteChallenge();

        assertNotEquals(categoryUrl, driver.getCurrentUrl(),
                "Nhấp vào sản phẩm cần thay đổi URL");
        assertEquals(productUrl, driver.getCurrentUrl(),
                "URL cần trỏ đến đúng sản phẩm đã chọn");
        assertTrue(products.hasProductDetails(), "Trang chi tiết cần hiển thị tên sản phẩm");
        assertTrue(products.productHeading().toLowerCase().contains(productName.toLowerCase()),
                "Tiêu đề chi tiết cần khớp sản phẩm được mở");
    }

    // [PRODUCT] Kiểm tra tên, giá, ảnh, thông số, bảo hành và hành động mua trên trang chi tiết.
    @Test
    @DisplayName("TC_PR_02 - Hiển thị đầy đủ thông tin sản phẩm")
    void productDetailsShowRequiredInformation() {
        ProductPage productPage = openSampleProduct();

        assertTrue(productPage.hasProductDetails(), "Trang chi tiết cần có tên sản phẩm");
        assertTrue(productPage.productPrice().matches("(?s).*\\d.*"),
                "Trang chi tiết cần hiển thị giá bán");
        assertTrue(productPage.hasLoadedPrimaryImage(),
                "Trang chi tiết cần có ảnh sản phẩm đã tải");
        assertTrue(productPage.hasWarrantyOption(),
                "Trang chi tiết cần hiển thị tùy chọn/chính sách bảo hành");
        assertTrue(productPage.hasPurchaseAction(),
                "Trang chi tiết cần hiển thị hành động mua hàng");
        assertTrue(productPage.hasSpecificationSection(),
                "Trang chi tiết cần hiển thị thông số kỹ thuật");
    }

    // [PRODUCT] Kiểm tra chọn phiên bản bộ nhớ khác làm cập nhật giá bán.
    @Test
    @DisplayName("TC_PR_03 - Thay đổi phiên bản dung lượng")
    void changingStorageVersionUpdatesPrice() {
        ProductPage productPage = openSampleProduct();
        assumeTrue(productPage.hasStorageOption("6-128GB")
                        && productPage.hasStorageOption("4-128GB"),
                "BLOCKED: sản phẩm mẫu không có đủ hai phiên bản bộ nhớ cần kiểm tra");

        productPage.selectStorageOption("6-128GB");
        assertTrue(waitFor(() -> productPage.isStorageOptionSelected("6-128GB")),
                "Phiên bản 6-128GB cần được đánh dấu đã chọn");
        String originalPrice = productPage.productPrice();
        assertTrue(originalPrice.matches("(?s).*\\d.*"),
                "Phiên bản ban đầu cần có giá bán dạng số");

        productPage.selectStorageOption("4-128GB");
        assertTrue(waitFor(() -> productPage.isStorageOptionSelected("4-128GB")),
                "Phiên bản 4-128GB cần được đánh dấu đã chọn");
        String updatedPrice = productPage.productPrice();

        assertTrue(updatedPrice.matches("(?s).*\\d.*"),
                "Phiên bản mới cần hiển thị giá bán dạng số");
        assertNotEquals(originalPrice, updatedPrice,
                "Giá bán cần cập nhật khi đổi phiên bản bộ nhớ");
    }

    // [PRODUCT] Kiểm tra màu thứ hai được chọn và ảnh/giá tương ứng được cập nhật.
    @Test
    @DisplayName("TC_PR_04 - Thay đổi tùy chọn màu sắc")
    void changingColorUpdatesProductPresentation() {
        ProductPage productPage = openSampleProduct();
        assumeTrue(productPage.colorOptionCount() >= 2,
                "BLOCKED: sản phẩm mẫu không có ít nhất hai lựa chọn màu");

        String firstColor = productPage.colorOptionName(0);
        productPage.selectColorAt(0);
        assertTrue(waitFor(() -> productPage.isColorOptionSelected(firstColor)
                        && productPage.selectedColorName().equals(firstColor)),
                "Màu ban đầu cần được chọn để đối chiếu");
        String originalImage = productPage.primaryImageSource();

        String secondColor = productPage.colorOptionName(1);
        productPage.selectColorAt(1);
        assertTrue(waitFor(() -> productPage.isColorOptionSelected(secondColor)
                        && productPage.selectedColorName().equals(secondColor)),
                "Màu thứ hai cần được đánh dấu đã chọn");
        assertTrue(waitFor(productPage::hasLoadedPrimaryImage),
                "Ảnh chính cần tải sau khi đổi màu");
        assertNotEquals(originalImage, productPage.primaryImageSource(),
                "Ảnh chính cần đổi theo màu được chọn");
        assertTrue(productPage.productPrice().matches("(?s).*\\d.*"),
                "Giá bán tương ứng với màu được chọn cần hiển thị");
    }

    private ProductPage openSampleProduct() {
        driver.get(TestData.url("product.sample"));
        assumeNoSiteChallenge();
        return new ProductPage(driver);
    }

    private boolean waitFor(BooleanSupplier condition) {
        try {
            return Boolean.TRUE.equals(new WebDriverWait(driver, TestConfig.waitTimeout())
                    .until(ignored -> condition.getAsBoolean()));
        } catch (TimeoutException timeout) {
            return false;
        }
    }
}
