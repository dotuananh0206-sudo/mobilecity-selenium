package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.ProductPage;
import mobilecity.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest extends BaseTest {
    @Test
    @DisplayName("TC-CAT-001 - Danh mục hiển thị sản phẩm có tên")
    void categoryShowsNamedProduct() {
        driver.get(TestData.url("category.url"));
        ProductPage products = new ProductPage(driver);

        assertTrue(products.firstListedProductName().length() > 0,
                "Danh mục cần hiển thị tên sản phẩm");
    }

    @Test
    @DisplayName("TC-PDP-001 - Mở trang chi tiết từ sản phẩm trong danh mục")
    void listedProductOpensMatchingDetailPage() {
        driver.get(TestData.url("category.url"));
        ProductPage products = new ProductPage(driver);
        String productName = products.firstListedProductName();
        products.openFirstListedProduct();

        assertTrue(products.hasProductDetails(), "Trang sản phẩm cần hiển thị tiêu đề");
        assertTrue(products.productHeading().toLowerCase().contains(productName.toLowerCase()),
                "Tiêu đề chi tiết cần khớp sản phẩm được mở");
    }
}
