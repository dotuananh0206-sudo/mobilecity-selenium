package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.config.TestConfig;
import mobilecity.pages.HomePage;
import mobilecity.pages.WarrantyPolicyPage;
import mobilecity.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class WarrantyPolicyTest extends BaseTest {
    // [WARRANTY POLICY] Kiểm tra liên kết Chính sách bảo hành tại footer.
    @Test
    @DisplayName("TC_WP_01 - Điều hướng vào Chính sách bảo hành từ footer")
    void opensWarrantyPolicyFromFooter() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        WarrantyPolicyPage policyPage = new WarrantyPolicyPage(driver);
        String destination = policyPage.footerPolicyDestination();

        policyPage.openPolicyFromFooter();
        assumeNoSiteChallenge();

        assertEquals(destination, driver.getCurrentUrl(),
                "Liên kết footer cần mở đúng URL Chính sách bảo hành");
        assertTrue(policyPage.hasPolicyHeading(),
                "Trang đích cần hiển thị tiêu đề Chính sách bảo hành");
    }

    // [WARRANTY POLICY] Kiểm tra phạm vi, thời hạn, bảo hành mở rộng và điều kiện từ chối.
    @Test
    @DisplayName("TC_WP_02 - Hiển thị đầy đủ nội dung chính sách bảo hành")
    void policyShowsRequiredWarrantyTopics() {
        WarrantyPolicyPage policyPage = new WarrantyPolicyPage(driver).open();
        assumeNoSiteChallenge();

        assertTrue(policyPage.hasPolicyHeading(), "Trang cần có tiêu đề Chính sách bảo hành");
        assertTrue(policyPage.hasPolicyContent(), "Trang cần hiển thị nội dung chính sách");
        assertTrue(policyPage.hasWarrantyDuration(),
                "Nội dung cần nêu thời hạn hoặc thời gian bảo hành");
        assertTrue(policyPage.hasWarrantyCoverage(),
                "Nội dung cần nêu phạm vi bảo hành");
        assertTrue(policyPage.hasExtendedWarrantyPackage(),
                "Nội dung cần nêu gói bảo hành mở rộng");
        assertTrue(policyPage.hasWarrantyExclusionConditions(),
                "Nội dung cần nêu điều kiện từ chối bảo hành");
    }

    // [WARRANTY POLICY] Mở liên kết bảo hành từ trang chi tiết, không thực hiện mua hàng.
    @Test
    @DisplayName("TC_WP_03 - Mở Chính sách bảo hành từ trang chi tiết sản phẩm")
    void opensWarrantyPolicyFromProductPage() {
        driver.get(TestData.url("product.sample"));
        assumeNoSiteChallenge();
        WarrantyPolicyPage policyPage = new WarrantyPolicyPage(driver);
        assumeTrue(policyPage.hasFooterPolicyLink(),
                "BLOCKED: không có liên kết Chính sách bảo hành trên trang sản phẩm");
        String destination = policyPage.footerPolicyDestination();

        policyPage.openPolicyFromFooter();
        assumeNoSiteChallenge();

        assertEquals(destination, driver.getCurrentUrl(),
                "Liên kết bảo hành từ trang sản phẩm cần mở đúng trang chính sách");
        assertTrue(policyPage.hasPolicyHeading(),
                "Trang đích cần hiển thị nội dung Chính sách bảo hành");
    }

    // [WARRANTY POLICY] Cuộn nội dung và kiểm tra bố cục văn bản/bảng trong phạm vi có thể xác minh.
    @Test
    @DisplayName("TC_WP_04 - Kiểm tra bố cục nội dung chính sách")
    void policyContentScrollsWithoutHorizontalOverflow() {
        WarrantyPolicyPage policyPage = new WarrantyPolicyPage(driver).open();
        assumeNoSiteChallenge();

        assertTrue(policyPage.hasPolicyContent(), "Trang cần có nội dung chính sách");
        assertTrue(!policyPage.contentFontFamily().isBlank(),
                "Nội dung cần được trình bày bằng font chữ đã xác định");
        assertTrue(policyPage.scrollThroughContentWithoutOverflow(),
                "Nội dung không được tràn ngang viewport khi cuộn trang");
        assumeTrue(policyPage.hasVisiblePolicyTable(),
                "BLOCKED: trang chính sách hiện không có bảng để xác minh bố cục bảng");
        assertTrue(policyPage.visiblePolicyTablesFitViewport(),
                "Các bảng chính sách không được tràn khỏi viewport");
    }
}
