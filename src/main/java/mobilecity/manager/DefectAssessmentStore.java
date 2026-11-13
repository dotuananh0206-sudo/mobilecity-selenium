package mobilecity.manager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class DefectAssessmentStore {
    public record Assessment(String severity, String classification) {
    }

    private static final String PREFIX = "assessment.";
    private static final String SEVERITY_SUFFIX = ".severity";
    private static final String CLASSIFICATION_SUFFIX = ".classification";

    private final Path assessmentFile;

    public DefectAssessmentStore() {
        this(Path.of(System.getProperty("user.home"),
                ".mobilecity-test-manager", "defect-assessments.properties"));
    }

    public DefectAssessmentStore(Path assessmentFile) {
        this.assessmentFile = assessmentFile;
    }

    public Map<String, Assessment> loadAll() throws IOException {
        if (!Files.exists(assessmentFile)) {
            return Map.of();
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(assessmentFile)) {
            properties.load(reader);
        }

        Map<String, Assessment> assessments = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith(PREFIX) || !key.endsWith(SEVERITY_SUFFIX)) {
                continue;
            }
            String id = key.substring(PREFIX.length(), key.length() - SEVERITY_SUFFIX.length());
            String severity = properties.getProperty(key);
            String classification = properties.getProperty(PREFIX + id + CLASSIFICATION_SUFFIX);
            if (id.isBlank() || severity == null || severity.isBlank()
                    || classification == null || classification.isBlank()) {
                throw new IOException("Đánh giá lỗi không đầy đủ trong " + assessmentFile);
            }
            assessments.put(id, new Assessment(severity, classification));
        }
        return Map.copyOf(assessments);
    }

    public void saveAll(Map<String, Assessment> assessments) throws IOException {
        Properties properties = new Properties();
        for (Map.Entry<String, Assessment> entry : assessments.entrySet()) {
            String id = entry.getKey();
            Assessment assessment = entry.getValue();
            if (id == null || id.isBlank() || assessment == null
                    || assessment.severity() == null || assessment.severity().isBlank()
                    || assessment.classification() == null
                    || assessment.classification().isBlank()) {
                throw new IllegalArgumentException("Đánh giá lỗi cần mã, mức độ và phân loại.");
            }
            properties.setProperty(PREFIX + id + SEVERITY_SUFFIX, assessment.severity());
            properties.setProperty(PREFIX + id + CLASSIFICATION_SUFFIX,
                    assessment.classification());
        }

        Path parent = assessmentFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(assessmentFile)) {
            properties.store(writer, "MobileCity defect assessments");
        }
    }
}
