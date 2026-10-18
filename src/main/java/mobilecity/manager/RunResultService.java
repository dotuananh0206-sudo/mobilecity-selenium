package mobilecity.manager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RunResultService {
    public record TestResult(
            String id,
            String name,
            String feature,
            String className,
            String methodName,
            String status,
            double seconds,
            String details) {
    }

    public record RunSummary(
            int selected,
            int passed,
            int failed,
            int blocked,
            int notExecuted,
            List<TestResult> results) {
        public int completed() {
            return passed + failed + blocked;
        }
    }

    public List<TestResult> readResults(Path reportDirectory) throws IOException {
        if (!Files.isDirectory(reportDirectory)) {
            return List.of();
        }
        List<Path> reportFiles;
        try (var files = Files.list(reportDirectory)) {
            reportFiles = files
                    .filter(path -> path.getFileName().toString().startsWith("TEST-")
                            && path.getFileName().toString().endsWith(".xml"))
                    .sorted()
                    .toList();
        }

        List<TestResult> results = new ArrayList<>();
        for (Path reportFile : reportFiles) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setXIncludeAware(false);
                factory.setExpandEntityReferences(false);
                var builder = factory.newDocumentBuilder();
                builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                    @Override
                    public void warning(org.xml.sax.SAXParseException exception) throws SAXException {
                        throw exception;
                    }

                    @Override
                    public void error(org.xml.sax.SAXParseException exception) throws SAXException {
                        throw exception;
                    }

                    @Override
                    public void fatalError(org.xml.sax.SAXParseException exception) throws SAXException {
                        throw exception;
                    }
                });
                Element root = builder.parse(reportFile.toFile()).getDocumentElement();
                NodeList testCases = root.getElementsByTagName("testcase");
                for (int index = 0; index < testCases.getLength(); index++) {
                    Element testCase = (Element) testCases.item(index);
                    String className = testCase.getAttribute("classname");
                    String methodName = testCase.getAttribute("name");
                    TestCaseDefinition definition = TestCaseCatalog.find(className, methodName).orElse(null);
                    Element failure = firstChildElement(testCase, "failure");
                    Element error = firstChildElement(testCase, "error");
                    Element skipped = firstChildElement(testCase, "skipped");
                    String status;
                    String details;
                    if (failure != null || error != null) {
                        Element issue = failure == null ? error : failure;
                        status = "FAIL";
                        details = elementDetails(issue);
                    } else if (skipped != null) {
                        details = elementDetails(skipped);
                        status = details.toUpperCase(java.util.Locale.ROOT).contains("BLOCKED")
                                ? "BLOCKED"
                                : "NOT EXECUTED";
                    } else {
                        status = "PASS";
                        details = "JUnit hoàn tất thành công.";
                    }
                    double seconds = parseSeconds(testCase.getAttribute("time"));
                    results.add(new TestResult(
                            definition == null ? className + "#" + methodName : definition.id(),
                            definition == null ? methodName : definition.name(),
                            definition == null ? "Khác" : definition.feature(),
                            className,
                            methodName,
                            status,
                            seconds,
                            details));
                }
            } catch (ParserConfigurationException | SAXException e) {
                throw new IOException("Không thể đọc báo cáo JUnit: " + reportFile, e);
            }
        }
        results.sort(Comparator.comparing(TestResult::feature).thenComparing(TestResult::id));
        return List.copyOf(results);
    }

    public RunSummary summarize(Path reportDirectory, List<TestCaseDefinition> selected)
            throws IOException {
        List<TestResult> results = new ArrayList<>(readResults(reportDirectory));
        Set<String> actualSelectors = new HashSet<>();
        for (TestResult result : results) {
            actualSelectors.add(result.className().substring(result.className().lastIndexOf('.') + 1)
                    + "#" + result.methodName());
        }
        for (TestCaseDefinition testCase : selected) {
            if (!actualSelectors.contains(testCase.selector())) {
                results.add(new TestResult(
                        testCase.id(), testCase.name(), testCase.feature(), testCase.className(),
                        testCase.methodName(), "NOT EXECUTED", 0,
                        "JUnit chưa tạo kết quả cho test case này."));
            }
        }
        results.sort(Comparator.comparing(TestResult::feature).thenComparing(TestResult::id));
        int passed = count(results, "PASS");
        int failed = count(results, "FAIL");
        int blocked = count(results, "BLOCKED");
        int notExecuted = count(results, "NOT EXECUTED");
        return new RunSummary(selected.size(), passed, failed, blocked, notExecuted,
                List.copyOf(results));
    }

    public RunSummary summarizeAllAvailable(Path reportDirectory) throws IOException {
        return summarize(reportDirectory, TestCaseCatalog.all());
    }

    private static int count(List<TestResult> results, String status) {
        return (int) results.stream().filter(result -> result.status().equals(status)).count();
    }

    private static Element firstChildElement(Element parent, String tagName) {
        NodeList children = parent.getElementsByTagName(tagName);
        return children.getLength() == 0 ? null : (Element) children.item(0);
    }

    private static String elementDetails(Element element) {
        String message = element.getAttribute("message");
        if (message.isBlank()) {
            message = element.getAttribute("type");
        }
        if (message.isBlank()) {
            message = element.getTextContent();
        }
        String normalized = message.replaceAll("[\\r\\n\\t]+", " ").trim();
        return normalized.length() > 2000 ? normalized.substring(0, 2000) : normalized;
    }

    private static double parseSeconds(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
