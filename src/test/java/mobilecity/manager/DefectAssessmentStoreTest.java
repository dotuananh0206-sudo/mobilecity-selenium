package mobilecity.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefectAssessmentStoreTest {
    @Test
    void persistsAndReloadsSeverityAndClassification(@TempDir Path directory) throws IOException {
        DefectAssessmentStore store =
                new DefectAssessmentStore(directory.resolve("defect-assessments.properties"));
        Map<String, DefectAssessmentStore.Assessment> expected = Map.of(
                "TC_PR_03", new DefectAssessmentStore.Assessment("Cao", "Chức năng"),
                "DM_10", new DefectAssessmentStore.Assessment("Trung bình", "Giao diện/UX"));

        store.saveAll(expected);

        assertEquals(expected, store.loadAll());
    }
}
