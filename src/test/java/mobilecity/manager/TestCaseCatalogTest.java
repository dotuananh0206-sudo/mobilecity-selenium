package mobilecity.manager;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCaseCatalogTest {
    @Test
    void catalogContainsOnlyUniqueImplementedSelectors() {
        Set<String> ids = new HashSet<>();
        Set<String> selectors = new HashSet<>();
        for (TestCaseDefinition testCase : TestCaseCatalog.all()) {
            assertTrue(ids.add(testCase.id()), "Test case IDs must be unique");
            assertTrue(selectors.add(testCase.selector()), "JUnit selectors must be unique");
        }

        assertEquals(52, TestCaseCatalog.all().size());
        assertTrue(TestCaseCatalog.findById("TC-CART-001").isEmpty());
        assertEquals(List.of("Trang chủ", "Danh mục", "Tìm kiếm", "Lọc & Sắp xếp", "Sản phẩm",
                        "Tin tức & Khuyến mãi", "Liên hệ & Cửa hàng", "Chính sách bảo hành",
                        "Kiểm thử nội bộ"),
                TestCaseCatalog.all().stream().map(TestCaseDefinition::feature).distinct().toList());
        assertEquals("testTC01_LoadHomePage",
                TestCaseCatalog.findById("TC_01").orElseThrow().methodName());
        assertEquals("testDM05_OpenPhoneCategoryRoot",
                TestCaseCatalog.findById("DM_05").orElseThrow().methodName());
        assertEquals("HomeTest",
                TestCaseCatalog.findById("TC_07").orElseThrow().className());
        assertEquals("HomeTest",
                TestCaseCatalog.findById("TC_08").orElseThrow().className());
        for (String id : List.of("TC_FS_01", "TC_FS_02", "TC_FS_03", "TC_FS_04", "TC_FS_05")) {
            assertEquals("FilterSortTest",
                    TestCaseCatalog.findById(id).orElseThrow().className());
        }
        for (String id : List.of("TC_PR_01", "TC_PR_02", "TC_PR_03", "TC_PR_04")) {
            assertEquals("ProductTest",
                    TestCaseCatalog.findById(id).orElseThrow().className());
        }
        for (String id : List.of("TC_NP_01", "TC_NP_02", "TC_NP_03", "TC_NP_04")) {
            assertEquals("NewsPromotionTest",
                    TestCaseCatalog.findById(id).orElseThrow().className());
        }
        for (String id : List.of("TC_CS_01", "TC_CS_02", "TC_CS_03", "TC_CS_04")) {
            assertEquals("ContactStoreTest",
                    TestCaseCatalog.findById(id).orElseThrow().className());
        }
        for (String id : List.of("TC_WP_01", "TC_WP_02", "TC_WP_03", "TC_WP_04")) {
            assertEquals("WarrantyPolicyTest",
                    TestCaseCatalog.findById(id).orElseThrow().className());
        }
        assertEquals("searchShowsQuickSuggestionsWhileTyping",
                TestCaseCatalog.findById("TC_SR_03").orElseThrow().methodName());
    }
}
