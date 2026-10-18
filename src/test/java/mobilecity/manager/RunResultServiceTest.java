package mobilecity.manager;

import mobilecity.manager.RunResultService.RunSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunResultServiceTest {
    @Test
    void readsSurefireStatusesAndLeavesMissingCasesUnexecuted(@TempDir Path reports)
            throws IOException {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <testsuite name="mobilecity.tests.HomeTest" tests="2" errors="0" failures="1" skipped="0">
                  <testcase classname="mobilecity.tests.HomeTest" name="homepageLoadsWithTitle" time="0.5"/>
                  <testcase classname="mobilecity.tests.HomeTest" name="homepageOpensPhoneCategory" time="1.0">
                    <failure message="Assertion failed">Stack detail</failure>
                  </testcase>
                  <testcase classname="mobilecity.tests.SearchTest" name="searchReturnsMatchingProducts" time="0">
                    <skipped message="BLOCKED: cần đăng nhập"/>
                  </testcase>
                  <testcase classname="mobilecity.tests.SearchTest" name="searchShowsEmptyStateForNoMatch" time="0">
                    <skipped message="Disabled by test configuration"/>
                  </testcase>
                </testsuite>
                """;
        Files.writeString(reports.resolve("TEST-mobilecity.tests.HomeTest.xml"),
                xml, StandardCharsets.UTF_8);
        List<TestCaseDefinition> selected = List.of(
                TestCaseCatalog.findById("TC-HOME-001").orElseThrow(),
                TestCaseCatalog.findById("TC-HOME-002").orElseThrow(),
                TestCaseCatalog.findById("TC-SEARCH-002").orElseThrow(),
                TestCaseCatalog.findById("TC-SEARCH-003").orElseThrow(),
                TestCaseCatalog.findById("TC-PRODUCT-003").orElseThrow());

        RunSummary summary = new RunResultService().summarize(reports, selected);

        assertEquals(1, summary.passed());
        assertEquals(1, summary.failed());
        assertEquals(1, summary.blocked());
        assertEquals(2, summary.notExecuted());
        assertEquals(5, summary.results().size());
    }

    @Test
    void rejectsXmlExternalEntityDeclarations(@TempDir Path reports) throws IOException {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE testsuite [<!ENTITY xxe SYSTEM "file:///Windows/win.ini">]>
                <testsuite name="x"><testcase classname="x" name="x">&xxe;</testcase></testsuite>
                """;
        Files.writeString(reports.resolve("TEST-hostile.xml"), xml, StandardCharsets.UTF_8);

        org.junit.jupiter.api.Assertions.assertThrows(IOException.class,
                () -> new RunResultService().readResults(reports));
    }
}
