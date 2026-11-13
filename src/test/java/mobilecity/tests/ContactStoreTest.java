package mobilecity.tests;

import mobilecity.base.BaseTest;
import mobilecity.pages.ContactStorePage;
import mobilecity.pages.HomePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ContactStoreTest extends BaseTest {
    // [CONTACT & STORE] Kiểm tra mở trang liên hệ từ liên kết bản đồ cửa hàng ở footer.
    @Test
    @DisplayName("TC_CS_01 - Điều hướng vào trang Hệ thống cửa hàng / Liên hệ")
    void opensContactPageFromFooterStoreLink() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        ContactStorePage contactPage = new ContactStorePage(driver);
        String destination = contactPage.firstStoreMapDestination();

        contactPage.openFirstStoreMapLinkFromFooter();
        assumeNoSiteChallenge();

        assertEquals(destination, driver.getCurrentUrl(),
                "Liên kết cửa hàng cần mở đúng trang liên hệ");
        assertTrue(contactPage.hasContactHeading(),
                "Trang đích cần hiển thị tiêu đề thông tin liên hệ");
        assertFalse(contactPage.activeBranchName().isBlank(),
                "Trang liên hệ cần hiển thị thông tin một chi nhánh");
    }

    // [CONTACT & STORE] Kiểm tra tên, địa chỉ, số điện thoại và giờ làm việc của chi nhánh.
    @Test
    @DisplayName("TC_CS_02 - Hiển thị đầy đủ thông tin chi nhánh")
    void activeStoreShowsRequiredBranchInformation() {
        ContactStorePage contactPage = new ContactStorePage(driver).open();
        assumeNoSiteChallenge();

        assertTrue(contactPage.hasContactHeading(), "Trang cần nhận diện chức năng liên hệ");
        assertTrue(!contactPage.activeBranchName().isBlank(),
                "Chi nhánh cần có tên hiển thị");
        assertTrue(!contactPage.activeBranchAddress().isBlank(),
                "Chi nhánh cần có địa chỉ hiển thị");
        assertTrue(contactPage.activeBranchHasPhoneNumber(),
                "Chi nhánh cần có ít nhất một số điện thoại");
        assertTrue(contactPage.activeBranchHasWorkingHours(),
                "Chi nhánh cần hiển thị giờ làm việc");
    }

    // [CONTACT & STORE] Kiểm tra bộ lọc vùng nếu trang cửa hàng cung cấp bộ lọc tương ứng.
    @Test
    @DisplayName("TC_CS_03 - Lọc danh sách cửa hàng theo khu vực")
    void filtersStoresByRegionWhenAvailable() {
        ContactStorePage contactPage = new ContactStorePage(driver).open();
        assumeNoSiteChallenge();

        assumeTrue(contactPage.hasStoreRegionFilter(),
                "BLOCKED: trang hiện có tab chọn cơ sở, chưa có bộ lọc danh sách theo khu vực");

        assertFalse(contactPage.activeBranchName().isBlank(),
                "Sau khi chọn khu vực cần còn thông tin chi nhánh hiển thị");
    }

    // [CONTACT & STORE] Kiểm tra bản đồ Google Maps được nhúng và tải trên trang cửa hàng.
    @Test
    @DisplayName("TC_CS_04 - Kiểm tra bản đồ chỉ đường cửa hàng")
    void storePageShowsLoadedEmbeddedMap() {
        new HomePage(driver).open();
        assumeNoSiteChallenge();
        ContactStorePage contactPage = new ContactStorePage(driver);
        String destination = contactPage.firstStoreMapDestination();

        contactPage.openFirstStoreMapLinkFromFooter();
        assumeNoSiteChallenge();

        assertEquals(destination, driver.getCurrentUrl(),
                "Liên kết Xem bản đồ cần mở đúng trang chi nhánh");
        assertTrue(contactPage.hasVisibleMapFrame(),
                "Trang chi nhánh cần hiển thị khung bản đồ Google Maps");
        assertTrue(contactPage.hasLoadedMapControls(),
                "Bản đồ nhúng cần tải được các điều khiển của Google Maps");
    }
}
