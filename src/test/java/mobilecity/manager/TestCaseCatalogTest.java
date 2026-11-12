package mobilecity.manager;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
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

        assertEquals(35, TestCaseCatalog.all().size());
        assertEquals("testTC01_LoadHomePage",
                TestCaseCatalog.findById("TC_01").orElseThrow().methodName());
        assertEquals("testDM05_OpenPhoneCategoryRoot",
                TestCaseCatalog.findById("DM_05").orElseThrow().methodName());
    }
}
