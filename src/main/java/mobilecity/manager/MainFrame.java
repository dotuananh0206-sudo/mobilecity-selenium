package mobilecity.manager;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mobilecity.config.TestConfig;
import mobilecity.manager.RunHistoryStore.RunRecord;
import mobilecity.manager.RunResultService.RunSummary;
import mobilecity.manager.RunResultService.TestResult;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MainFrame {
    private static final String NAVY = "#14243A";
    private static final String BLUE = "#2563EB";
    private static final String GREEN = "#16A368";
    private static final String RED = "#DC3545";
    private static final String ORANGE = "#EA8523";
    private static final String GRAY = "#929BAA";
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Pattern RUNNING_CLASS_LINE =
            Pattern.compile("\\[INFO\\] Running ([\\w.$]+)");
    private static final List<NavigationItem> NAVIGATION = List.of(
            new NavigationItem("dashboard", "▦", "Tổng quan"),
            new NavigationItem("cases", "☷", "Danh sách test case"),
            new NavigationItem("execution", "▶", "Chạy kiểm thử tự động"),
            new NavigationItem("results", "✓", "Kết quả kiểm thử"),
            new NavigationItem("bugs", "⚠", "Quản lý lỗi"),
            new NavigationItem("reports", "▤", "Báo cáo kiểm thử"),
            new NavigationItem("history", "◷", "Lịch sử chạy"),
            new NavigationItem("settings", "⚙", "Cấu hình hệ thống"),
            new NavigationItem("project", "ⓘ", "Thông tin dự án"));

    private final Stage stage;
    private final Path projectRoot;
    private final RunResultService resultService = new RunResultService();
    private final RunHistoryStore historyStore = new RunHistoryStore();
    private final TestRunController runController;
    private final Set<String> selectedCaseIds = new HashSet<>();
    private final Map<TableView<CaseRow>, FilteredList<CaseRow>> caseRows = new IdentityHashMap<>();
    private final BorderPane root = new BorderPane();
    private final VBox sidebar = new VBox(5);
    private final StackPane pageHost = new StackPane();
    private HBox pageHeadingActions;
    private final Label breadcrumb = new Label("Tổng quan");
    private final Label statusText = new Label("Website: chưa kiểm tra  ·  Chrome: xác minh khi chạy");
    private final Label runStateLabel = new Label("Chưa có lệnh chạy trong phiên này.");
    private Label progressLabel = new Label("Chưa chạy");
    private ProgressBar progressBar = new ProgressBar(0);
    private TextArea liveLog = new TextArea();
    private final StringBuilder liveLogBuffer = new StringBuilder();
    private final AtomicInteger completedTests = new AtomicInteger();
    private final AtomicInteger livePassed = new AtomicInteger();
    private final AtomicInteger liveFailed = new AtomicInteger();
    private final AtomicInteger liveSkipped = new AtomicInteger();
    private TestSettings settings;
    private List<RunRecord> history = List.of();
    private RunRecord selectedHistory;
    private RunSummary currentSummary = new RunSummary(
            TestCaseCatalog.all().size(), 0, 0, 0, TestCaseCatalog.all().size(), List.of());
    private Path currentReportDirectory;
    private String currentPage = "dashboard";
    private boolean pendingClose;
    private int activeRunCount;
    private String executionBadgeText = "CHƯA CHẠY";
    private String executionBadgeStyle = "run-state-idle";
    private String executionDescriptionText = "Sẵn sàng bắt đầu";
    private String currentTestText = "Chưa có test case đang chạy";
    private String activeTestSelector;
    private String failureNoticeText = "Chưa ghi nhận lỗi trong phiên này.";
    private String failureNoticeStyle = "execution-error-notice";
    private boolean testClassInProgress;
    private Button stopButton;
    private Button startRunButton;
    private ComboBox<String> runMode;
    private TextField executionUrl;
    private TextField executionWaitSeconds;
    private CheckBox executionHeadless;
    private Label selectedCountLabel;
    private Label executionStateBadge;
    private Label executionStateDescription;
    private Label executionPercent;
    private Label executedCountLabel;
    private Label runningCountLabel;
    private Label passedCountLabel;
    private Label failedCountLabel;
    private Label currentTestLabel;
    private Label executionFailureNotice;
    private TableView<CaseRow> caseSelectionTable;
    private TableView<TestResult> resultTable;
    private Label selectedResultDetails;

    public MainFrame(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("MobileCity Test Manager");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setWidth(1440);
        stage.setHeight(900);
        projectRoot = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        settings = TestSettings.load();
        selectedCaseIds.addAll(TestCaseCatalog.all().stream()
                .map(TestCaseDefinition::id).toList());
        runController = new TestRunController(projectRoot, resultService, historyStore);
        loadData();
        buildWindow();
        stage.setOnCloseRequest(this::requestWindowClose);
    }

    private void buildWindow() {
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(22, 14, 18, 14));
        root.setLeft(buildSidebar());
        root.setCenter(buildWorkspace());
        root.getStyleClass().add("app-root");
        Scene scene = new Scene(root, 1440, 900);
        var stylesheet = MainFrame.class.getResource("app.css");
        if (stylesheet == null) {
            throw new IllegalStateException("Không tìm thấy stylesheet JavaFX app.css.");
        }
        scene.getStylesheets().add(stylesheet.toExternalForm());
        stage.setScene(scene);
        showPage("dashboard");
    }

    private Node buildSidebar() {
        HBox brand = new HBox(12);
        brand.setAlignment(Pos.CENTER_LEFT);
        Label mark = new Label("✓");
        mark.getStyleClass().add("brand-mark");
        VBox identity = new VBox(2, new Label("MOBILECITY"), new Label("TEST MANAGER"));
        identity.getStyleClass().add("brand-text");
        brand.getChildren().addAll(mark, identity);
        brand.setPadding(new Insets(0, 8, 0, 8));

        Label menuLabel = new Label("KHÔNG GIAN LÀM VIỆC");
        menuLabel.getStyleClass().add("sidebar-caption");
        VBox navigation = new VBox(5);
        navigation.getStyleClass().add("sidebar-navigation");
        for (NavigationItem item : NAVIGATION) {
            Button button = new Button(item.icon() + "    " + item.title());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.getStyleClass().add("navigation-button");
            button.setId("navigation-" + item.id());
            button.setOnAction(event -> showPage(item.id()));
            navigation.getChildren().add(button);
        }
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox project = new VBox(5,
                new Label("DỰ ÁN ĐANG THEO DÕI"),
                new Label("Kiểm thử website MobileCity"),
                new Label("Selenium · JUnit 5 · Maven"));
        project.getStyleClass().add("sidebar-project");
        sidebar.getChildren().setAll(brand, new Separator(), menuLabel, navigation, spacer, project);
        return sidebar;
    }

    private Node buildWorkspace() {
        BorderPane workspace = new BorderPane();
        workspace.getStyleClass().add("workspace");
        HBox topBar = new HBox(18);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");
        breadcrumb.getStyleClass().add("breadcrumb");
        HBox.setHgrow(breadcrumb, Priority.ALWAYS);
        statusText.getStyleClass().add("connection-status");
        Button refresh = secondaryButton("↻  Làm mới dữ liệu");
        refresh.setOnAction(event -> refreshData());
        Label initials = new Label("QA");
        initials.getStyleClass().add("user-avatar");
        VBox user = new VBox(2, new Label("Người vận hành"), new Label("Ứng dụng cục bộ"));
        user.getStyleClass().add("user-copy");
        topBar.getChildren().addAll(breadcrumb, statusText, refresh, initials, user);
        workspace.setTop(topBar);
        pageHost.getStyleClass().add("page-host");
        workspace.setCenter(pageHost);
        return workspace;
    }

    private void showPage(String id) {
        currentPage = id;
        caseRows.clear();
        pageHost.getChildren().setAll(switch (id) {
            case "cases" -> buildCasesPage();
            case "execution" -> buildExecutionPage();
            case "results" -> buildResultsPage();
            case "bugs" -> buildBugsPage();
            case "reports" -> buildReportsPage();
            case "history" -> buildHistoryPage();
            case "settings" -> buildSettingsPage();
            case "project" -> buildProjectPage();
            default -> buildDashboard();
        });
        String title = NAVIGATION.stream().filter(item -> item.id().equals(id))
                .map(NavigationItem::title).findFirst().orElse("Tổng quan");
        breadcrumb.setText("MobileCity Test Manager   ›   " + title);
        for (NavigationItem item : NAVIGATION) {
            var button = stage.getScene().lookup("#navigation-" + item.id());
            if (button != null) {
                button.getStyleClass().remove("navigation-selected");
                if (item.id().equals(id)) {
                    button.getStyleClass().add("navigation-selected");
                }
            }
        }
    }

    private Node buildDashboard() {
        RunSummary summary = null;
        String summarySource = "";
        try {
            DashboardData dashboardData = readDashboardData();
            summary = dashboardData.summary();
            summarySource = dashboardData.source();
        } catch (IOException e) {
            statusText.setText("Không đọc được số liệu mới nhất: " + e.getMessage());
        }
        Button reports = secondaryButton("▤  Xem báo cáo");
        Button run = primaryButton("▶  Chạy kiểm thử mới");
        VBox page = page("Tổng quan kiểm thử",
                "Theo dõi chất lượng và tiến độ kiểm thử tự động của dự án.",
                reports, run);
        reports.setOnAction(event -> showPage("reports"));
        run.setOnAction(event -> showPage("execution"));

        page.getChildren().add(infoBanner(summary == null
                ? "Không thể cập nhật số liệu từ báo cáo JUnit: " + statusText.getText()
                : summarySource + ". Số liệu lấy từ JUnit thực tế; không dùng dữ liệu minh họa."));
        page.getChildren().add(projectSummaryCard());

        int completed = summary == null ? 0 : summary.completed();
        String passRate = summary == null || completed == 0 ? "—"
                : String.format(Locale.ROOT, "%.1f%%", summary.passed() * 100.0 / completed);
        HBox metrics = new HBox(14,
                metricCard("Có kết quả / tổng test",
                        summary == null ? "—" : completed + "/" + TestCaseCatalog.all().size(),
                        BLUE, "Từ báo cáo JUnit mới nhất"),
                metricCard("Test PASS", summary == null ? "—" : Integer.toString(summary.passed()),
                        GREEN, "Bản chạy mới nhất"),
                metricCard("Test FAIL", summary == null ? "—" : Integer.toString(summary.failed()),
                        RED, "Không mặc định là lỗi website"),
                metricCard("Tỷ lệ thành công", passRate,
                        BLUE, completed == 0 ? "Chưa có kết quả thực thi" : "Trên số test có kết quả"));
        metrics.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        page.getChildren().add(metrics);

        Node distribution = summary == null
                ? emptyNotice("Phân bố chưa khả dụng vì không đọc được báo cáo.")
                : distributionCard(summary);
        HBox lower = new HBox(14, distribution, recentRunsCard());
        HBox.setHgrow(lower.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(lower.getChildren().get(1), Priority.ALWAYS);
        page.getChildren().add(lower);
        page.getChildren().add(projectInformationCard());
        return scrollable(page);
    }

    private DashboardData readDashboardData() throws IOException {
        Path defaultReportDirectory = projectRoot.resolve("target").resolve("surefire-reports");
        RunRecord latestGuiRun = history.isEmpty() ? null : history.get(0);
        Instant latestDefaultReport = latestReportTime(defaultReportDirectory);
        if (latestGuiRun != null && (latestDefaultReport == null
                || !latestDefaultReport.isAfter(latestGuiRun.startedAt()))) {
            return new DashboardData(
                    resultService.summarize(latestGuiRun.reportDirectory(), TestCaseCatalog.all()),
                    "Số liệu lần chạy gần nhất: " + latestGuiRun.id());
        }
        if (latestDefaultReport != null) {
            return new DashboardData(
                    resultService.summarize(defaultReportDirectory, TestCaseCatalog.all()),
                    "Số liệu báo cáo Surefire cập nhật lúc " + displayDate(latestDefaultReport));
        }
        return new DashboardData(
                resultService.summarize(defaultReportDirectory, TestCaseCatalog.all()),
                "Chưa có báo cáo JUnit; các test chưa có kết quả được ghi nhận là chưa thực thi");
    }

    private static Instant latestReportTime(Path reportDirectory) throws IOException {
        if (!Files.isDirectory(reportDirectory)) {
            return null;
        }
        Instant latest = null;
        try (var reports = Files.list(reportDirectory)) {
            for (Path report : reports
                    .filter(path -> path.getFileName().toString().startsWith("TEST-")
                            && path.getFileName().toString().endsWith(".xml"))
                    .toList()) {
                Instant modified = Files.getLastModifiedTime(report).toInstant();
                if (latest == null || modified.isAfter(latest)) {
                    latest = modified;
                }
            }
        }
        return latest;
    }

    private Node projectSummaryCard() {
        Label projectTitle = titleLabel("Kiểm thử tự động website MobileCity");
        projectTitle.setWrapText(true);
        VBox identity = new VBox(6,
                new Label("DỰ ÁN ĐANG THEO DÕI"),
                projectTitle);
        identity.getStyleClass().add("project-identity");
        identity.setMinWidth(270);

        HBox row = new HBox(22);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(identity, new Separator(javafx.geometry.Orientation.VERTICAL),
                summaryField("WEBSITE", "mobilecity.vn"),
                summaryField("CÔNG NGHỆ", "Selenium · Java · Maven · JUnit 5 · POM"),
                summaryField("TRÌNH DUYỆT", "Google Chrome"));
        for (int index = 2; index < row.getChildren().size(); index++) {
            HBox.setHgrow(row.getChildren().get(index), Priority.ALWAYS);
        }
        VBox card = card(row);
        card.getStyleClass().add("project-summary");
        return card;
    }

    private static Node summaryField(String heading, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        VBox field = new VBox(6, new Label(heading), valueLabel);
        field.getStyleClass().add("summary-field");
        return field;
    }

    private Node metricCard(String name, String value, String accent, String note) {
        VBox card = card();
        card.getStyleClass().addAll("metric-card", accentClass(accent));
        Label heading = new Label(name);
        heading.getStyleClass().add("metric-title");
        Label count = new Label(value);
        count.getStyleClass().add("metric-value");
        Label description = new Label(note);
        description.getStyleClass().add("muted");
        card.getChildren().addAll(heading, count, description);
        return card;
    }

    private Node distributionCard(RunSummary summary) {
        VBox card = card();
        card.getStyleClass().add("dashboard-card");
        card.getChildren().addAll(sectionHeader("Phân bố kết quả", "Theo trạng thái test case"));
        HBox content = new HBox(18);
        content.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().addAll(distributionChart(summary), new VBox(14,
                legendRow("PASS", summary.passed(), GREEN),
                legendRow("FAIL", summary.failed(), RED),
                legendRow("BLOCKED", summary.blocked(), ORANGE),
                legendRow("NOT EXECUTED", summary.notExecuted(), GRAY)));
        card.getChildren().add(content);
        return card;
    }

    private Node distributionChart(RunSummary summary) {
        int[] counts = {summary.passed(), summary.failed(), summary.blocked(), summary.notExecuted()};
        String[] colors = {GREEN, RED, ORANGE, GRAY};
        int total = summary.passed() + summary.failed() + summary.blocked()
                + summary.notExecuted();
        Canvas canvas = new Canvas(164, 164);
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setLineWidth(20);
        if (total == 0) {
            graphics.setStroke(javafx.scene.paint.Color.web("#E8EDF4"));
            graphics.strokeArc(22, 22, 120, 120, 90, 360, javafx.scene.shape.ArcType.OPEN);
        } else {
            double start = 90;
            for (int index = 0; index < counts.length; index++) {
                if (counts[index] == 0) {
                    continue;
                }
                double angle = counts[index] * 360.0 / total;
                graphics.setStroke(javafx.scene.paint.Color.web(colors[index]));
                graphics.strokeArc(22, 22, 120, 120, start, -angle, javafx.scene.shape.ArcType.OPEN);
                start -= angle;
            }
        }
        VBox center = new VBox(1, new Label(Integer.toString(total)), new Label("Test case"));
        center.setAlignment(Pos.CENTER);
        center.getStyleClass().add("chart-center");
        StackPane chart = new StackPane(canvas, center);
        chart.setMinSize(164, 164);
        chart.setMaxSize(164, 164);
        return chart;
    }

    private Node legendRow(String name, int count, String color) {
        HBox row = new HBox(9);
        row.setAlignment(Pos.CENTER_LEFT);
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + color + ";");
        Label text = new Label(name);
        text.getStyleClass().add("legend-name");
        Label amount = new Label(Integer.toString(count));
        amount.getStyleClass().add("legend-count");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(dot, text, spacer, amount);
        return row;
    }

    private Node recentRunsCard() {
        VBox card = card();
        card.getStyleClass().add("dashboard-card");
        card.getChildren().add(sectionHeader("Lần chạy gần đây", "Lịch sử được lưu trên thiết bị"));
        if (history.isEmpty()) {
            card.getChildren().add(emptyNotice("Chưa có lịch sử chạy từ giao diện. "
                    + "Kết quả Maven có sẵn vẫn được dùng nếu có báo cáo."));
            return card;
        }
        TableView<RunRecord> table = new TableView<>();
        table.setPrefHeight(192);
        table.getColumns().setAll(List.of(
                textColumn("Mã lần chạy", record -> record.id()),
                textColumn("Thời gian", record -> displayDate(record.startedAt())),
                textColumn("Tổng", record -> Integer.toString(record.selected())),
                textColumn("PASS", record -> Integer.toString(record.passed())),
                textColumn("FAIL", record -> Integer.toString(record.failed())),
                textColumn("Trạng thái", record -> record.state())));
        table.setItems(FXCollections.observableArrayList(history.stream().limit(5).toList()));
        card.getChildren().add(table);
        return card;
    }

    private Node projectInformationCard() {
        VBox card = card();
        card.getStyleClass().add("dashboard-card");
        card.getChildren().addAll(sectionHeader("Thông tin dự án", "Cấu hình kiểm thử đang sử dụng"),
                new Label("Website: https://mobilecity.vn/"),
                new Label("Selenium WebDriver · Java 17 · Maven · JUnit 5 · Page Object Model"),
                new Label("Website và Chrome chưa được kiểm tra kết nối khi mở ứng dụng."));
        return card;
    }

    private Node buildCasesPage() {
        Button choose = primaryButton("Chọn test để chạy");
        VBox page = page("Danh sách test case",
                "Danh mục ánh xạ trực tiếp tới các phương thức @Test hiện có.",
                choose);
        choose.setOnAction(event -> showPage("execution"));

        Label details = new Label("Chọn một dòng để xem tiền điều kiện, bước chạy và kết quả mong đợi.");
        details.getStyleClass().add("detail-copy");
        TableView<CaseRow> table = createCaseTable(false);
        VBox body = card();
        body.getChildren().addAll(caseFilters(table, details), table, details,
                new Label(TestCaseCatalog.all().size()
                        + " test JUnit đã được lập catalog; thêm hoặc sửa test cần thay đổi mã nguồn."));
        VBox.setVgrow(table, Priority.ALWAYS);
        page.getChildren().add(body);
        return scrollable(page);
    }

    private Node buildExecutionPage() {
        VBox page = page("Chạy kiểm thử tự động",
                "Thiết lập và điều khiển phiên chạy Selenium WebDriver.");
        Button refresh = secondaryButton("↻  Làm mới");
        refresh.setOnAction(event -> refreshData());
        stopButton = secondaryButton("■  Dừng an toàn");
        stopButton.setId("stop-test-run");
        stopButton.setOnAction(event -> runController.cancel(listener()));
        startRunButton = primaryButton("▶  Bắt đầu kiểm thử");
        startRunButton.setId("start-test-run");
        startRunButton.setOnAction(event -> startRunFromConfiguration());
        pageHeadingActions.getChildren().setAll(refresh, stopButton, startRunButton);
        page.getChildren().add(infoBanner("Phiên chạy sử dụng Maven/Surefire và kết quả JUnit thực tế. "
                + "Thông tin tài khoản không được nhập tại đây; CAPTCHA/OTP không bị vượt qua."));

        VBox configuration = buildExecutionConfiguration();
        VBox status = buildExecutionStatus();
        VBox cases = buildExecutionCaseList();
        VBox logs = buildExecutionLog();
        page.getChildren().add(twoColumnLayout(configuration, status, cases, logs));
        updateRunControls();
        return scrollable(page);
    }

    private VBox buildExecutionConfiguration() {
        executionUrl = new TextField(settings.baseUrl());
        executionUrl.setId("execution-base-url");
        executionWaitSeconds = new TextField(Integer.toString(settings.waitSeconds()));
        executionWaitSeconds.setId("execution-wait-seconds");
        executionHeadless = new CheckBox("Chạy ẩn (headless)");
        executionHeadless.setSelected(settings.headless());

        runMode = new ComboBox<>(FXCollections.observableArrayList(
                "Toàn bộ test case", "Chọn test case"));
        runMode.getSelectionModel().selectFirst();
        runMode.setMaxWidth(Double.MAX_VALUE);

        GridPane fields = new GridPane();
        fields.setHgap(12);
        fields.setVgap(12);
        ColumnConstraints labels = new ColumnConstraints();
        labels.setPercentWidth(34);
        ColumnConstraints values = new ColumnConstraints();
        values.setPercentWidth(66);
        fields.getColumnConstraints().addAll(labels, values);
        addField(fields, 0, "URL website", executionUrl);
        addField(fields, 1, "Trình duyệt", readOnlySetting("Google Chrome"));
        addField(fields, 2, "Môi trường", readOnlySetting("Local"));
        addField(fields, 3, "Chế độ chạy", runMode);
        addField(fields, 4, "Thời gian chờ tối đa (giây)", executionWaitSeconds);

        HBox evidenceSettings = new HBox(10,
                settingIndicator("✓", "Chụp ảnh khi thất bại", "Khi extension tạo được ảnh"),
                settingIndicator("✓", "Lưu log và báo cáo", "Đầu ra Maven/JUnit thực tế"));
        HBox.setHgrow(evidenceSettings.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(evidenceSettings.getChildren().get(1), Priority.ALWAYS);
        VBox card = card(sectionHeader("Cấu hình chạy", "Thiết lập môi trường kiểm thử"),
                fields, evidenceSettings, executionHeadless,
                mutedLabel("Thay đổi tại đây áp dụng cho lần chạy này. Để lưu làm mặc định, "
                        + "mở Cấu hình hệ thống."));
        card.getStyleClass().add("execution-card");
        return card;
    }

    private VBox buildExecutionStatus() {
        executionStateBadge = new Label(executionBadgeText);
        executionStateBadge.getStyleClass().addAll("run-state-pill", executionBadgeStyle);
        executionStateDescription = new Label(executionDescriptionText);
        executionStateDescription.getStyleClass().add("muted");
        HBox stateHeader = new HBox(10,
                sectionHeader("Trạng thái thực thi", "Tiến trình Maven / JUnit"),
                spacer(), executionStateBadge);
        stateHeader.setAlignment(Pos.CENTER_LEFT);

        int selected = Math.max(0, activeRunCount);
        int complete = Math.min(selected, completedTests.get());
        executionPercent = new Label((selected == 0 ? 0 : complete * 100 / selected) + "%");
        executionPercent.getStyleClass().add("progress-percent");
        progressLabel = new Label(selected == 0 ? "Chưa có phiên chạy"
                : runController.isRunning() && activeTestSelector != null
                        ? "Hoàn tất " + complete + "/" + selected + " · Đang chạy "
                                + activeTestName()
                        : "Đã chạy " + complete + "/" + selected + " test");
        progressLabel.getStyleClass().add("muted");
        HBox progressCopy = new HBox(9, executionPercent, progressLabel);
        progressCopy.setAlignment(Pos.BASELINE_LEFT);
        progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(13);
        progressBar.setProgress(selected == 0 ? 0 : (double) complete / selected);

        executedCountLabel = new Label(Integer.toString(complete));
        runningCountLabel = new Label(Integer.toString(
                runController.isRunning() && testClassInProgress ? 1 : 0));
        passedCountLabel = new Label(Integer.toString(livePassed.get()));
        failedCountLabel = new Label(Integer.toString(liveFailed.get()));
        HBox counters = new HBox(0,
                runMetric("Đã chạy", executedCountLabel, "metric-neutral"),
                runMetric("Đang chạy", runningCountLabel, "metric-blue"),
                runMetric("PASS", passedCountLabel, "metric-green"),
                runMetric("FAIL", failedCountLabel, "metric-red"));
        counters.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        currentTestLabel = new Label(currentTestText);
        currentTestLabel.setWrapText(true);
        currentTestLabel.getStyleClass().add("current-test-name");
        HBox currentTest = new HBox(11, new Label("▶"), new VBox(4,
                new Label("TEST CASE HIỆN TẠI"), currentTestLabel));
        currentTest.getStyleClass().add("current-test-card");
        executionFailureNotice = new Label(failureNoticeText);
        executionFailureNotice.setWrapText(true);
        executionFailureNotice.getStyleClass().addAll(failureNoticeStyle.split(" "));
        VBox card = card(stateHeader, executionStateDescription, progressCopy, progressBar,
                counters, currentTest);
        card.getStyleClass().add("execution-status-card");
        return card;
    }

    private VBox buildExecutionCaseList() {
        selectedCountLabel = new Label(selectedCaseIds.size() + "/"
                + TestCaseCatalog.all().size() + " test case đã chọn");
        selectedCountLabel.getStyleClass().add("muted");
        Button toggleSelection = secondaryButton(selectionButtonText());
        toggleSelection.setOnAction(event -> changeSelection(selectedCaseIds.size()
                != TestCaseCatalog.all().size()));
        HBox title = new HBox(9,
                selectedCountLabel, spacer(), toggleSelection);
        title.setAlignment(Pos.CENTER_LEFT);
        VBox listHeading = new VBox(10,
                sectionHeader("Danh sách test case", "Chọn các kịch bản cần thực thi"),
                title);

        caseSelectionTable = createCaseTable(true);
        caseSelectionTable.setPrefHeight(330);
        VBox card = card(listHeading, caseSelectionTable);
        card.getStyleClass().add("execution-list-card");
        VBox.setVgrow(caseSelectionTable, Priority.ALWAYS);
        return card;
    }

    private VBox buildExecutionLog() {
        liveLog = new TextArea();
        liveLog.setEditable(false);
        liveLog.setWrapText(true);
        liveLog.setPrefRowCount(13);
        liveLog.setText(liveLogBuffer.toString());
        liveLog.getStyleClass().add("log-view");
        liveLog.setPromptText("Nhật ký sẽ hiển thị khi bắt đầu kiểm thử.");
        Button clear = secondaryButton("Xóa nhật ký hiển thị");
        clear.setOnAction(event -> {
            liveLogBuffer.setLength(0);
            liveLog.clear();
            failureNoticeText = "Chưa ghi nhận lỗi trong phiên này.";
            failureNoticeStyle = "execution-error-notice";
            executionFailureNotice.setText(failureNoticeText);
            executionFailureNotice.getStyleClass().setAll(failureNoticeStyle.split(" "));
        });
        HBox header = new HBox(8,
                sectionHeader("Nhật ký thực thi", "Thông tin theo thời gian thực"),
                spacer(), clear);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox card = card(header, liveLog, executionFailureNotice);
        card.getStyleClass().add("execution-log-card");
        VBox.setVgrow(liveLog, Priority.ALWAYS);
        return card;
    }

    private static GridPane twoColumnLayout(Node topLeft, Node topRight,
                                            Node bottomLeft, Node bottomRight) {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        left.setHgrow(Priority.ALWAYS);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        right.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(left, right);
        RowConstraints firstRow = new RowConstraints();
        firstRow.setVgrow(Priority.SOMETIMES);
        RowConstraints secondRow = new RowConstraints();
        secondRow.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().addAll(firstRow, secondRow);
        grid.add(topLeft, 0, 0);
        grid.add(topRight, 1, 0);
        grid.add(bottomLeft, 0, 1);
        grid.add(bottomRight, 1, 1);
        for (Node node : List.of(topLeft, topRight, bottomLeft, bottomRight)) {
            if (node instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
            }
            GridPane.setHgrow(node, Priority.ALWAYS);
            GridPane.setVgrow(node, Priority.ALWAYS);
        }
        return grid;
    }

    private static Node readOnlySetting(String value) {
        Label label = new Label(value);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add("readonly-setting");
        return label;
    }

    private static Node settingIndicator(String icon, String title, String description) {
        Label check = new Label(icon);
        check.getStyleClass().add("setting-check");
        VBox text = new VBox(3, new Label(title), mutedLabel(description));
        text.getStyleClass().add("setting-indicator-copy");
        HBox row = new HBox(8, check, text);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("setting-indicator");
        return row;
    }

    private static Node runMetric(String title, Label value, String valueClass) {
        value.getStyleClass().addAll("run-metric-value", valueClass);
        VBox metric = new VBox(5, mutedLabel(title), value);
        metric.setAlignment(Pos.CENTER);
        metric.getStyleClass().add("run-metric");
        return metric;
    }

    private static Label mutedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("muted");
        return label;
    }

    private void startRunFromConfiguration() {
        if (runMode == null || runMode.getSelectionModel().getSelectedIndex() == 0) {
            startAllRun();
        } else {
            startSelectedRun();
        }
    }

    private TestSettings executionSettings() {
        int waitSeconds;
        try {
            waitSeconds = Integer.parseInt(executionWaitSeconds.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Thời gian chờ phải là số nguyên.", e);
        }
        TestSettings configured = new TestSettings(executionUrl.getText().trim(),
                executionHeadless.isSelected(), waitSeconds,
                settings.screenshotDirectory(), settings.reportDirectory());
        configured.validate();
        return configured;
    }

    private void updateSelectedCount() {
        if (selectedCountLabel != null) {
            selectedCountLabel.setText(selectedCaseIds.size() + "/"
                    + TestCaseCatalog.all().size() + " test case đã chọn");
        }
        if (caseSelectionTable != null) {
            Node card = caseSelectionTable.getParent();
            if (card instanceof VBox box && !box.getChildren().isEmpty()
                    && box.getChildren().get(0) instanceof HBox heading) {
                for (Node node : heading.getChildren()) {
                    if (node instanceof Button button) {
                        button.setText(selectionButtonText());
                    }
                }
            }
        }
    }

    private String selectionButtonText() {
        return selectedCaseIds.size() == TestCaseCatalog.all().size()
                ? "Bỏ chọn tất cả" : "Chọn tất cả";
    }

    private void updateExecutionMetrics() {
        int complete = completedTests.get();
        int selected = Math.max(0, activeRunCount);
        int percentage = selected == 0 ? 0 : Math.min(100, complete * 100 / selected);
        if (executedCountLabel != null) {
            executedCountLabel.setText(Integer.toString(complete));
            int running = runController.isRunning() && testClassInProgress ? 1 : 0;
            runningCountLabel.setText(Integer.toString(running));
            passedCountLabel.setText(Integer.toString(livePassed.get()));
            failedCountLabel.setText(Integer.toString(liveFailed.get()));
            executionPercent.setText(percentage + "%");
            progressBar.setProgress(selected == 0 ? 0 : (double) complete / selected);
            if (runController.isRunning() && complete < selected) {
                String activeDescription = activeTestSelector == null
                        ? "Đang chờ test tiếp theo"
                        : "Đang chạy " + activeTestName();
                progressLabel.setText("Hoàn tất " + complete + "/" + selected + " · "
                        + activeDescription
                        + (liveSkipped.get() == 0 ? "" : " · Bỏ qua " + liveSkipped.get()));
            } else if (complete > 0) {
                progressLabel.setText("Hoàn tất " + complete + "/" + selected + " test theo Maven");
            }
        }
    }

    private String activeTestName() {
        if (activeTestSelector == null) {
            return "";
        }
        return TestCaseCatalog.all().stream()
                .filter(testCase -> testCase.selector().equals(activeTestSelector))
                .map(TestCaseDefinition::name).findFirst().orElse(activeTestSelector);
    }

    private void updateExecutionState(String state, String styleClass, String description) {
        if (executionStateBadge == null) {
            return;
        }
        executionStateBadge.setText(state);
        executionStateBadge.getStyleClass().removeAll(
                "run-state-idle", "run-state-running", "run-state-passed",
                "run-state-warning", "run-state-failed");
        executionStateBadge.getStyleClass().add(styleClass);
        executionStateDescription.setText(description);
        executionBadgeText = state;
        executionBadgeStyle = styleClass;
        executionDescriptionText = description;
    }

    private TableView<CaseRow> createCaseTable(boolean selectable) {
        ObservableList<CaseRow> rows = FXCollections.observableArrayList();
        Map<String, TestResult> results = currentSummary.results().stream()
                .collect(Collectors.toMap(TestResult::id, result -> result, (first, second) -> second));
        for (TestCaseDefinition testCase : TestCaseCatalog.all()) {
            TestResult result = results.get(testCase.id());
            CaseRow row = new CaseRow(testCase,
                    result == null ? "NOT EXECUTED" : result.status(),
                    result == null ? 0 : result.seconds(),
                    selectedCaseIds.contains(testCase.id()));
            row.selectedProperty().addListener((observable, oldValue, selected) -> {
                if (selected) {
                    selectedCaseIds.add(testCase.id());
                } else {
                    selectedCaseIds.remove(testCase.id());
                }
                updateSelectedCount();
            });
            rows.add(row);
        }
        FilteredList<CaseRow> filtered = new FilteredList<>(rows, row -> true);
        TableView<CaseRow> table = new TableView<>(filtered);
        table.setPlaceholder(emptyNotice("Không có test case khớp bộ lọc."));
        if (selectable) {
            TableColumn<CaseRow, Boolean> choose = new TableColumn<>("");
            choose.setCellValueFactory(cell -> cell.getValue().selectedProperty());
            choose.setCellFactory(CheckBoxTableCell.forTableColumn(choose));
            choose.setEditable(true);
            choose.setMinWidth(40);
            choose.setPrefWidth(44);
            choose.setMaxWidth(44);
            choose.setStyle("-fx-alignment: CENTER;");
            table.setEditable(true);
            table.getColumns().add(choose);
            TableColumn<CaseRow, String> testCase = testCaseColumn();
            testCase.setMinWidth(205);
            testCase.setPrefWidth(340);
            TableColumn<CaseRow, String> status = statusColumn("Trạng thái", CaseRow::status);
            status.setMinWidth(90);
            status.setPrefWidth(112);
            TableColumn<CaseRow, String> duration = textColumn("Thời gian", row -> row.seconds() == 0
                    ? "—" : String.format(Locale.ROOT, "%.2f giây", row.seconds()));
            duration.setMinWidth(78);
            duration.setPrefWidth(92);
            table.getColumns().addAll(testCase, status, duration);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        } else {
            fitColumns(table);
            table.getColumns().addAll(List.of(
                    textColumn("Mã test", row -> row.definition().id()),
                    textColumn("Tên test case", row -> row.definition().name()),
                    textColumn("Chức năng", row -> row.definition().feature()),
                    statusColumn("Trạng thái gần nhất", CaseRow::status)));
        }
        caseRows.put(table, filtered);
        table.setPrefHeight(selectable ? 390 : 510);
        return table;
    }

    private static TableColumn<CaseRow, String> testCaseColumn() {
        TableColumn<CaseRow, String> column = textColumn("TEST CASE", row ->
                row.definition().id() + "  " + row.definition().name());
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                CaseRow row = getTableRow().getItem();
                Label id = new Label(row.definition().id());
                id.getStyleClass().add("test-case-id");
                Label name = new Label(row.definition().name());
                name.setWrapText(false);
                name.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
                name.getStyleClass().add("test-case-name");
                name.setMaxWidth(Double.MAX_VALUE);
                name.setTooltip(new Tooltip(row.definition().name()));
                HBox.setHgrow(name, Priority.ALWAYS);
                HBox content = new HBox(9, id, name);
                content.setAlignment(Pos.CENTER_LEFT);
                setGraphic(content);
                setText(null);
            }
        });
        return column;
    }

    private Node caseFilters(TableView<CaseRow> table, Label details) {
        TextField search = new TextField();
        search.setPromptText("Tìm theo mã hoặc tên test case");
        search.setPrefWidth(255);
        ComboBox<String> feature = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả chức năng", "Trang chủ", "Tìm kiếm", "Sản phẩm", "Giỏ hàng", "Kiểm thử nội bộ"));
        feature.getSelectionModel().selectFirst();
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả trạng thái", "PASS", "FAIL", "BLOCKED", "NOT EXECUTED"));
        status.getSelectionModel().selectFirst();
        FilteredList<CaseRow> rows = caseRows.get(table);
        if (rows == null) {
            throw new IllegalStateException("Không tìm thấy danh sách test case đang lọc.");
        }
        Runnable updateFilter = () -> {
            String query = search.getText().trim().toLowerCase(Locale.ROOT);
            String selectedFeature = feature.getValue();
            String selectedStatus = status.getValue();
            rows.setPredicate(row -> {
                TestCaseDefinition testCase = row.definition();
                boolean matchesSearch = query.isEmpty()
                        || testCase.id().toLowerCase(Locale.ROOT).contains(query)
                        || testCase.name().toLowerCase(Locale.ROOT).contains(query);
                boolean matchesFeature = selectedFeature == null
                        || selectedFeature.equals("Tất cả chức năng")
                        || testCase.feature().equals(selectedFeature);
                boolean matchesStatus = selectedStatus == null
                        || selectedStatus.equals("Tất cả trạng thái")
                        || row.status().equals(selectedStatus);
                return matchesSearch && matchesFeature && matchesStatus;
            });
        };
        search.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        feature.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        status.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, row) -> {
            if (row != null && details != null) {
                TestCaseDefinition testCase = row.definition();
                details.setText("Tiền điều kiện: " + testCase.precondition()
                        + "\nBước chạy: " + testCase.steps()
                        + "\nMong đợi: " + testCase.expectedResult());
            }
        });
        HBox filters = new HBox(9, new Label("Tìm kiếm"), search, feature, status);
        filters.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);
        return filters;
    }

    private void changeSelection(boolean selected) {
        if (caseSelectionTable == null) {
            return;
        }
        FilteredList<CaseRow> filtered = caseRows.get(caseSelectionTable);
        if (filtered == null) {
            throw new IllegalStateException("Không tìm thấy danh sách test case đang hiển thị.");
        }
        for (CaseRow row : filtered.getSource()) {
            row.setSelected(selected);
        }
    }

    private void startSelectedRun() {
        List<TestCaseDefinition> selected = TestCaseCatalog.all().stream()
                .filter(testCase -> selectedCaseIds.contains(testCase.id())).toList();
        startRun(selected);
    }

    private void startAllRun() {
        startRun(TestCaseCatalog.all());
    }

    private void startRun(List<TestCaseDefinition> selected) {
        if (selected.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Chưa chọn test",
                    "Hãy chọn ít nhất một test case để chạy.");
            return;
        }
        TestSettings runSettings;
        try {
            runSettings = executionSettings();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Cấu hình chạy không hợp lệ", e.getMessage());
            return;
        }
        if (selected.stream().anyMatch(testCase -> testCase.id().equals("TC-CART-001"))) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                    "Lựa chọn có TC-CART-001. Test có thể cần MOBILECITY_USERNAME/PASSWORD "
                            + "và có thể bị BLOCKED bởi CAPTCHA/OTP. Ứng dụng không lưu credentials "
                            + "và không vượt cơ chế xác minh. Tiếp tục?",
                    ButtonType.YES, ButtonType.NO);
            confirmation.setTitle("Xác nhận test giỏ hàng");
            confirmation.setHeaderText("Test có thể cần thông tin xác thực");
            if (confirmation.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }
        }
        activeRunCount = selected.size();
        completedTests.set(0);
        livePassed.set(0);
        liveFailed.set(0);
        liveSkipped.set(0);
        activeTestSelector = null;
        testClassInProgress = false;
        liveLogBuffer.setLength(0);
        liveLog.clear();
        updateExecutionState("CHUẨN BỊ", "run-state-running",
                "Đang khởi chạy tiến trình Maven.");
        currentTestText = "Đang khởi tạo phiên Maven";
        currentTestLabel.setText(currentTestText);
        failureNoticeText = "Chưa ghi nhận lỗi trong phiên này.";
        failureNoticeStyle = "execution-error-notice";
        executionFailureNotice.setText(failureNoticeText);
        executionFailureNotice.getStyleClass().setAll(failureNoticeStyle);
        try {
            String runId = runController.start(selected, runSettings, listener());
            settings = runSettings;
            appendLog("Bắt đầu " + runId + " — " + selected.size() + " test case đã chọn.");
            runStateLabel.setText("Đang chạy " + runId);
            progressLabel.setText("Đang chạy 0/" + selected.size());
            updateExecutionState("ĐANG CHẠY", "run-state-running",
                    "Maven đang thực thi JUnit/Selenium.");
            updateExecutionMetrics();
            updateRunControls();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            updateExecutionState("KHÔNG KHỞI CHẠY", "run-state-failed",
                    "Không thể khởi chạy tiến trình Maven.");
            currentTestText = "Không thể bắt đầu phiên kiểm thử.";
            currentTestLabel.setText(currentTestText);
            showAlert(Alert.AlertType.ERROR, "Lỗi khởi chạy",
                    "Không thể bắt đầu kiểm thử:\n" + e.getMessage());
        }
    }

    private TestRunController.Listener listener() {
        return new TestRunController.Listener() {
            @Override
            public void onOutput(String line) {
                Platform.runLater(() -> {
                    appendLog(line);
                    Matcher runningClass = RUNNING_CLASS_LINE.matcher(line);
                    if (runningClass.find() && currentTestLabel != null
                            && activeTestSelector == null) {
                        String className = runningClass.group(1);
                        String simpleName = className.substring(className.lastIndexOf('.') + 1);
                        testClassInProgress = true;
                        currentTestText = "Đang thực thi lớp " + simpleName;
                        currentTestLabel.setText(currentTestText);
                    }
                });
            }

            @Override
            public void onProgress(TestRunController.RunProgress progress) {
                Platform.runLater(() -> {
                    completedTests.set(progress.completed());
                    livePassed.set(progress.passed());
                    liveFailed.set(progress.failed());
                    liveSkipped.set(progress.skipped());
                    activeTestSelector = progress.activeTestSelector();
                    testClassInProgress = activeTestSelector != null;
                    if (activeTestSelector != null) {
                        String[] selector = activeTestSelector.split("#", 2);
                        String testName = selector.length == 2
                                ? TestCaseCatalog.find(selector[0], selector[1])
                                        .map(TestCaseDefinition::name)
                                        .orElse(activeTestSelector)
                                : activeTestSelector;
                        currentTestText = "Đang thực thi: " + testName;
                        currentTestLabel.setText(currentTestText);
                    }
                    updateExecutionMetrics();
                });
            }

            @Override
            public void onCancellationRequested() {
                Platform.runLater(() -> {
                    runStateLabel.setText("Đang yêu cầu dừng — chờ test hiện tại hoàn tất.");
                    updateExecutionState("ĐANG DỪNG", "run-state-running",
                            "Đang chờ test hiện tại và tiến trình Maven kết thúc.");
                    if (stopButton != null) {
                        stopButton.setDisable(true);
                    }
                });
            }

            @Override
            public void onCompleted(TestRunController.RunExecution execution) {
                Platform.runLater(() -> completeRun(execution));
            }
        };
    }

    private void appendLog(String line) {
        liveLogBuffer.append(line).append(System.lineSeparator());
        int maximumCharacters = 600_000;
        if (liveLogBuffer.length() > maximumCharacters) {
            liveLogBuffer.delete(0, liveLogBuffer.length() - maximumCharacters);
        }
        liveLog.setText(liveLogBuffer.toString());
        liveLog.positionCaret(liveLog.getLength());
        if (executionFailureNotice != null
                && line.matches("(?i).*\\b(ERROR|FAIL|TIMEOUT|EXCEPTION)\\b.*")) {
            executionFailureNotice.setText(line);
            executionFailureNotice.getStyleClass().setAll("execution-error-notice", "has-error");
            failureNoticeText = line;
            failureNoticeStyle = "execution-error-notice has-error";
        }
    }

    private void completeRun(TestRunController.RunExecution execution) {
        selectedHistory = null;
        try {
            history = historyStore.loadAll();
        } catch (IOException e) {
            appendLog("Không thể tải lịch sử chạy: " + e.getMessage());
        }
        selectedHistory = history.stream().filter(record -> record.id().equals(execution.id()))
                .findFirst().orElse(null);
        currentReportDirectory = execution.reportDirectory();
        currentSummary = execution.summary();

        String state = execution.cancelled() ? "Đã dừng an toàn"
                : execution.cancellationFailed() ? "Không xác nhận được dừng an toàn"
                : execution.exitCode() == 0 ? "Hoàn tất Maven"
                : "Maven có lỗi (exit code " + execution.exitCode() + ")";
        runStateLabel.setText(state + " — " + execution.id());
        completedTests.set(execution.summary().completed());
        livePassed.set(execution.summary().passed());
        liveFailed.set(execution.summary().failed());
        activeTestSelector = null;
        activeRunCount = execution.summary().selected();
        testClassInProgress = false;
        updateExecutionMetrics();
        progressLabel.setText("PASS " + execution.summary().passed()
                + " · FAIL " + execution.summary().failed()
                + " · BLOCKED " + execution.summary().blocked()
                + " · Chưa thực thi " + execution.summary().notExecuted());
        if (execution.cancelled()) {
            updateExecutionState("ĐÃ DỪNG", "run-state-idle",
                    "Tiến trình Maven đã kết thúc sau khi nhận tín hiệu dừng.");
        } else if (execution.cancellationFailed()) {
            updateExecutionState("CẦN KIỂM TRA", "run-state-failed",
                    "Chưa xác nhận được tiến trình đã kết thúc.");
        } else if (execution.exitCode() == 0 && execution.summary().failed() == 0
                && execution.summary().notExecuted() == 0 && execution.summary().blocked() == 0) {
            updateExecutionState("HOÀN TẤT", "run-state-passed",
                    "Đã đọc kết quả cuối từ XML Surefire.");
        } else if (execution.exitCode() == 0 && execution.summary().failed() == 0
                && execution.summary().notExecuted() == 0) {
            updateExecutionState("HOÀN TẤT CÓ BỎ QUA", "run-state-warning",
                    "Có test bị BLOCKED/skipped; xem lý do trong kết quả JUnit.");
        } else {
            updateExecutionState("CÓ LỖI", "run-state-failed",
                    "Kiểm tra kết quả JUnit và nhật ký Maven.");
        }
        currentTestText = "Phiên " + execution.id() + " đã kết thúc.";
        currentTestLabel.setText(currentTestText);
        if (execution.summary().failed() == 0 && execution.summary().blocked() == 0) {
            failureNoticeText = "Chưa ghi nhận lỗi trong phiên này.";
            failureNoticeStyle = "execution-error-notice";
            executionFailureNotice.setText(failureNoticeText);
            executionFailureNotice.getStyleClass().setAll(failureNoticeStyle);
        } else if (execution.summary().failed() > 0) {
            failureNoticeText = execution.summary().failed()
                    + " test FAIL — mở Kết quả kiểm thử để xem thông tin JUnit.";
            failureNoticeStyle = "execution-error-notice has-error";
            executionFailureNotice.setText(failureNoticeText);
            executionFailureNotice.getStyleClass().setAll(failureNoticeStyle.split(" "));
        } else {
            failureNoticeText = execution.summary().blocked()
                    + " test bị BLOCKED/skipped; xem lý do trong Kết quả kiểm thử.";
            failureNoticeStyle = "execution-error-notice has-warning";
            executionFailureNotice.setText(failureNoticeText);
            executionFailureNotice.getStyleClass().setAll(failureNoticeStyle.split(" "));
        }
        if (!execution.persistenceError().isBlank()) {
            appendLog("Lỗi lưu/đọc kết quả: " + execution.persistenceError());
        }
        appendLog("Maven exit code: " + execution.exitCode()
                + (execution.logFile() == null ? "" : " · Log ứng dụng: " + execution.logFile()));
        updateRunControls();
        if (pendingClose && !execution.cancellationFailed()) {
            runController.close();
            stage.close();
            return;
        }
        if (execution.cancellationFailed()) {
            pendingClose = false;
            showAlert(Alert.AlertType.ERROR, "Cần xác minh tiến trình",
                    "Không xác nhận được tiến trình đã dừng an toàn. Ứng dụng vẫn mở; "
                            + "hãy kiểm tra Trình quản lý tác vụ và nhật ký.");
        }
        showPage("execution");
    }

    private void updateRunControls() {
        boolean running = runController.isRunning();
        if (startRunButton != null) {
            startRunButton.setDisable(running);
        }
        if (runMode != null) {
            runMode.setDisable(running);
        }
        if (executionUrl != null) {
            executionUrl.setDisable(running);
        }
        if (executionWaitSeconds != null) {
            executionWaitSeconds.setDisable(running);
        }
        if (executionHeadless != null) {
            executionHeadless.setDisable(running);
        }
        if (caseSelectionTable != null) {
            caseSelectionTable.setDisable(running);
        }
        if (stopButton != null) {
            stopButton.setDisable(!running || runStateLabel.getText().startsWith("Đang yêu cầu"));
        }
    }

    private Node buildResultsPage() {
        Button openReportDirectory = secondaryButton("Mở thư mục báo cáo");
        VBox page = page("Kết quả kiểm thử",
                "Kết quả được đọc từ báo cáo XML của Maven Surefire đang chọn.",
                openReportDirectory);
        openReportDirectory.setOnAction(event -> openReportDirectory());
        page.getChildren().add(infoBanner("FAIL là kết quả JUnit thực tế, không tự động kết luận lỗi "
                + "thuộc website. Test không có dòng XML được ghi là NOT EXECUTED."));
        ComboBox<String> filter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả trạng thái", "PASS", "FAIL", "BLOCKED", "NOT EXECUTED"));
        filter.getSelectionModel().selectFirst();
        FilteredList<TestResult> filtered = new FilteredList<>(
                FXCollections.observableArrayList(currentSummary.results()), result -> true);
        resultTable = new TableView<>(filtered);
        resultTable.getStyleClass().add("results-table");
        List<TableColumn<TestResult, String>> resultColumns = List.of(
                textColumn("Mã test", TestResult::id),
                textColumn("Test case", TestResult::name),
                textColumn("Chức năng", TestResult::feature),
                statusColumn("Trạng thái", TestResult::status),
                textColumn("Thời lượng (s)",
                        result -> String.format(Locale.ROOT, "%.2f", result.seconds())));
        resultColumns.forEach(column -> {
            column.setMinWidth(90);
            column.setPrefWidth(180);
        });
        resultTable.getColumns().setAll(resultColumns);
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filter.valueProperty().addListener((observable, oldValue, newValue) ->
                filtered.setPredicate(result -> newValue == null || newValue.equals("Tất cả trạng thái")
                        || result.status().equals(newValue)));
        selectedResultDetails = new Label("Chọn một test case để xem kết quả thực tế và thông báo JUnit.");
        selectedResultDetails.setWrapText(true);
        selectedResultDetails.getStyleClass().add("detail-copy");
        resultTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, result) -> updateResultDetail(result));
        HBox actions = new HBox(8,
                secondaryButton("Mở ảnh bằng chứng"),
                secondaryButton("Mở XML Surefire"),
                secondaryButton("Xuất CSV"));
        ((Button) actions.getChildren().get(0)).setOnAction(event -> openSelectedEvidence());
        ((Button) actions.getChildren().get(1)).setOnAction(event -> openLatestSurefireXml());
        ((Button) actions.getChildren().get(2)).setOnAction(event -> exportCsv());
        VBox card = card();
        card.getChildren().addAll(new HBox(10, new Label("Lọc trạng thái"), filter),
                resultTable, selectedResultDetails, actions);
        VBox.setVgrow(resultTable, Priority.ALWAYS);
        if (!filtered.isEmpty()) {
            resultTable.getSelectionModel().selectFirst();
        }
        page.getChildren().add(card);
        return scrollable(page);
    }

    private void updateResultDetail(TestResult result) {
        if (result == null || selectedResultDetails == null) {
            return;
        }
        Optional<TestCaseDefinition> definition = TestCaseCatalog.find(
                result.className(), result.methodName());
        if (definition.isEmpty()) {
            selectedResultDetails.setText(result.status() + " · " + result.name()
                    + "\nThực tế: " + result.details());
            return;
        }
        TestCaseDefinition testCase = definition.get();
        selectedResultDetails.setText(result.status() + " · " + result.name()
                + " · " + String.format(Locale.ROOT, "%.2f giây", result.seconds())
                + "\nTiền điều kiện: " + testCase.precondition()
                + "\nCác bước: " + testCase.steps()
                + "\nMong đợi: " + testCase.expectedResult()
                + "\nThực tế: " + result.details());
    }

    private void openSelectedEvidence() {
        TestResult result = resultTable == null ? null : resultTable.getSelectionModel().getSelectedItem();
        if (result == null) {
            showAlert(Alert.AlertType.INFORMATION, "Chưa chọn test",
                    "Hãy chọn một test case trước.");
            return;
        }
        if (!"FAIL".equals(result.status())) {
            showAlert(Alert.AlertType.INFORMATION, "Không có ảnh cho trạng thái này",
                    "Chỉ test FAIL có thể có ảnh do extension tạo; PASS/BLOCKED không tự tạo ảnh.");
            return;
        }
        Path directory = resolveProjectPath(settings.screenshotDirectory());
        try {
            Instant start = selectedHistory == null ? null : selectedHistory.startedAt();
            Instant end = selectedHistory == null ? null
                    : start.plusMillis(selectedHistory.durationMillis()).plusSeconds(10);
            Optional<Path> image = findScreenshot(result.id(), directory, start, end);
            if (image.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Ảnh bằng chứng không có",
                        "Không tìm thấy ảnh trong lần chạy đang xem. Test đăng nhập/giỏ hàng "
                                + "chủ động không lưu ảnh để bảo vệ dữ liệu nhạy cảm.");
            } else {
                openPath(image.get());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi đọc ảnh", e.getMessage());
        }
    }

    private static Optional<Path> findScreenshot(String testId, Path directory,
                                                  Instant runStart, Instant runEnd) throws IOException {
        if (!Files.isDirectory(directory)) {
            return Optional.empty();
        }
        try (var paths = Files.list(directory)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".png")
                            && path.getFileName().toString().contains(testId))
                    .filter(path -> {
                        if (runStart == null) {
                            return true;
                        }
                        try {
                            Instant modified = Files.getLastModifiedTime(path).toInstant();
                            return !modified.isBefore(runStart.minusSeconds(5))
                                    && !modified.isAfter(runEnd);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .max(Comparator.comparing(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant();
                        } catch (IOException e) {
                            return Instant.EPOCH;
                        }
                    }));
        }
    }

    private Node buildBugsPage() {
        VBox page = page("Quản lý lỗi",
                "Danh sách lỗi thực được ghi nhận bởi JUnit; không tạo mã bug hoặc mức độ giả.");
        page.getChildren().add(infoBanner("Lỗi Selenium, timeout trình duyệt hoặc khẳng định thất bại "
                + "cần được phân loại thủ công. Ứng dụng không tự kết luận đó là lỗi website."));
        List<TestResult> failures = currentSummary.results().stream()
                .filter(result -> result.status().equals("FAIL")).toList();
        VBox card = card();
        if (failures.isEmpty()) {
            card.getChildren().add(emptyNotice("Không có test FAIL trong báo cáo đang xem. "
                    + "BLOCKED và NOT EXECUTED không được tính là lỗi."));
        } else {
            TextField search = new TextField();
            search.setPromptText("Tìm theo mã test hoặc tên test");
            FilteredList<TestResult> filtered = new FilteredList<>(
                    FXCollections.observableArrayList(failures), result -> true);
            search.textProperty().addListener((observable, oldValue, value) -> {
                String query = value.trim().toLowerCase(Locale.ROOT);
                filtered.setPredicate(result -> query.isEmpty()
                        || result.id().toLowerCase(Locale.ROOT).contains(query)
                        || result.name().toLowerCase(Locale.ROOT).contains(query));
            });
            TableView<TestResult> table = new TableView<>(filtered);
            fitColumns(table);
            table.getColumns().setAll(List.of(textColumn("Mã test", TestResult::id),
                    textColumn("Test case", TestResult::name),
                    textColumn("Mức độ", result -> "Chưa đánh giá"),
                    textColumn("Phân loại", result -> "Chưa phân loại"),
                    textColumn("Thông tin thực tế", TestResult::details)));
            VBox.setVgrow(table, Priority.ALWAYS);
            card.getChildren().addAll(new HBox(10, new Label("Tìm lỗi thực tế"), search), table);
        }
        page.getChildren().add(card);
        return scrollable(page);
    }

    private Node buildReportsPage() {
        VBox page = page("Báo cáo kiểm thử",
                "Mở báo cáo Maven/Surefire và xuất CSV từ kết quả JUnit thực tế.");
        VBox details = card();
        details.getChildren().addAll(sectionHeader("Báo cáo đang xem", "Kết quả thực tế"),
                new Label("Thư mục XML/text: " + currentReportDirectory),
                new Label("Test " + currentSummary.selected() + "  ·  PASS " + currentSummary.passed()
                        + "  ·  FAIL " + currentSummary.failed() + "  ·  BLOCKED "
                        + currentSummary.blocked() + "  ·  Chưa chạy " + currentSummary.notExecuted()),
                new Label("Log test: " + resolveProjectPath(settings.reportDirectory())
                        .resolve("test-execution.log")));
        page.getChildren().addAll(details, infoBanner("Dự án có báo cáo Surefire XML/text và CSV; "
                + "không có bộ tạo báo cáo HTML."));

        GridPane actions = new GridPane();
        actions.setHgap(10);
        actions.setVgap(10);
        actions.add(primaryButton("Mở XML Surefire"), 0, 0);
        actions.add(secondaryButton("Mở thư mục báo cáo"), 1, 0);
        actions.add(secondaryButton("Mở log kiểm thử"), 2, 0);
        actions.add(secondaryButton("Mở log điều phối"), 0, 1);
        actions.add(secondaryButton("Mở thư mục ảnh"), 1, 1);
        actions.add(secondaryButton("Xuất kết quả CSV"), 2, 1);
        ((Button) actions.getChildren().get(0)).setOnAction(event -> openLatestSurefireXml());
        ((Button) actions.getChildren().get(1)).setOnAction(event -> openReportDirectory());
        ((Button) actions.getChildren().get(2)).setOnAction(event -> openPath(
                resolveProjectPath(settings.reportDirectory()).resolve("test-execution.log")));
        ((Button) actions.getChildren().get(3)).setOnAction(event -> openPath(
                selectedHistory == null ? null : selectedHistory.logFile()));
        ((Button) actions.getChildren().get(4)).setOnAction(event -> openPath(
                resolveProjectPath(settings.screenshotDirectory())));
        ((Button) actions.getChildren().get(5)).setOnAction(event -> exportCsv());
        VBox actionCard = card(actions);
        page.getChildren().add(actionCard);
        return scrollable(page);
    }

    private void openLatestSurefireXml() {
        try {
            if (!Files.isDirectory(currentReportDirectory)) {
                openPath(currentReportDirectory);
                return;
            }
            try (var files = Files.list(currentReportDirectory)) {
                Optional<Path> report = files.filter(path -> path.getFileName().toString()
                                .startsWith("TEST-") && path.getFileName().toString().endsWith(".xml"))
                        .max(Comparator.comparing(path -> {
                            try {
                                return Files.getLastModifiedTime(path).toInstant();
                            } catch (IOException e) {
                                return Instant.EPOCH;
                            }
                        }));
                if (report.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Chưa có báo cáo",
                            "Thư mục chưa có XML Surefire.");
                } else {
                    openPath(report.get());
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi đọc báo cáo", e.getMessage());
        }
    }

    private void exportCsv() {
        try {
            Path exportDirectory = Path.of(System.getProperty("user.home"),
                    ".mobilecity-test-manager", "exports");
            Files.createDirectories(exportDirectory);
            String runId = selectedHistory == null ? "maven-report" : selectedHistory.id();
            Path csvPath = exportDirectory.resolve(runId + "-results.csv");
            StringBuilder content = new StringBuilder(
                    "Ma test case,Test case,Chuc nang,Trang thai,Thoi luong (giay),Chi tiet\r\n");
            for (TestResult result : currentSummary.results()) {
                content.append(csv(result.id())).append(',').append(csv(result.name())).append(',')
                        .append(csv(result.feature())).append(',').append(csv(result.status())).append(',')
                        .append(csv(String.format(Locale.ROOT, "%.3f", result.seconds()))).append(',')
                        .append(csv(result.details())).append("\r\n");
            }
            Files.writeString(csvPath, content, StandardCharsets.UTF_8);
            appendLog("Đã xuất kết quả JUnit hiện có ra " + csvPath);
            openPath(csvPath);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi xuất báo cáo", e.getMessage());
        }
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value;
        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private Node buildHistoryPage() {
        VBox page = page("Lịch sử chạy",
                "Các lần chạy từ giao diện được lưu bền vững trên thiết bị.");
        VBox card = card();
        if (history.isEmpty()) {
            card.getChildren().add(emptyNotice("Chưa có lần chạy từ giao diện được lưu. "
                    + "Báo cáo Surefire có sẵn vẫn xem được trong Kết quả kiểm thử."));
        } else {
            TableView<RunRecord> table = new TableView<>(FXCollections.observableArrayList(history));
            table.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);
            fitColumns(table);
            table.getColumns().setAll(List.of(
                    textColumn("Mã lần chạy", RunRecord::id),
                    textColumn("Bắt đầu", record -> displayDate(record.startedAt())),
                    textColumn("Thời lượng", record -> formatDuration(record.durationMillis())),
                    textColumn("Tổng", record -> Integer.toString(record.selected())),
                    textColumn("PASS", record -> Integer.toString(record.passed())),
                    textColumn("FAIL", record -> Integer.toString(record.failed())),
                    textColumn("BLOCKED", record -> Integer.toString(record.blocked())),
                    textColumn("Chưa chạy", record -> Integer.toString(record.notExecuted())),
                    textColumn("Trạng thái", RunRecord::state)));
            Button view = primaryButton("Xem báo cáo lần chạy");
            view.setOnAction(event -> {
                RunRecord record = table.getSelectionModel().getSelectedItem();
                if (record == null) {
                    showAlert(Alert.AlertType.INFORMATION, "Chưa chọn lịch sử",
                            "Hãy chọn một lần chạy.");
                } else {
                    selectHistory(record);
                }
            });
            Button open = secondaryButton("Mở thư mục lịch sử");
            open.setOnAction(event -> openPath(historyStore.historyDirectory()));
            VBox.setVgrow(table, Priority.ALWAYS);
            card.getChildren().addAll(new HBox(8, view, open), table);
        }
        page.getChildren().add(card);
        return scrollable(page);
    }

    private void selectHistory(RunRecord record) {
        selectedHistory = record;
        loadData();
        showPage("results");
    }

    private Node buildSettingsPage() {
        VBox page = page("Cấu hình hệ thống",
                "Các giá trị được truyền thành system properties khi ứng dụng khởi chạy Maven.");
        page.getChildren().add(infoBanner("Thông tin đăng nhập không nằm trong giao diện hoặc tệp cấu hình. "
                + "TC-CART-001 chỉ đọc MOBILECITY_USERNAME/PASSWORD từ môi trường."));
        TextField baseUrl = new TextField(settings.baseUrl());
        CheckBox headless = new CheckBox("Chạy Chrome ở chế độ headless");
        headless.setSelected(settings.headless());
        TextField waitSeconds = new TextField(Integer.toString(settings.waitSeconds()));
        TextField screenshotDirectory = new TextField(settings.screenshotDirectory());
        TextField reportDirectory = new TextField(settings.reportDirectory());
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(14);
        form.getStyleClass().add("settings-form");
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPercentWidth(31);
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setPercentWidth(69);
        form.getColumnConstraints().addAll(labelColumn, fieldColumn);
        addField(form, 0, "URL website mục tiêu", baseUrl);
        addField(form, 1, "Chế độ trình duyệt", headless);
        addField(form, 2, "Thời gian chờ (1–120 giây)", waitSeconds);
        addField(form, 3, "Thư mục ảnh bằng chứng",
                directoryField(screenshotDirectory));
        addField(form, 4, "Thư mục log kiểm thử", directoryField(reportDirectory));

        Label outcome = new Label(" ");
        outcome.getStyleClass().add("muted");
        Button validate = secondaryButton("Kiểm tra cấu hình");
        validate.setOnAction(event -> {
            try {
                settingsFromFields(baseUrl, headless, waitSeconds,
                        screenshotDirectory, reportDirectory).validate();
                outcome.setText("Cấu hình hợp lệ. Chrome/website được kiểm tra khi chạy test.");
                outcome.getStyleClass().setAll("success-copy");
            } catch (RuntimeException e) {
                outcome.setText(e.getMessage());
                outcome.getStyleClass().setAll("error-copy");
            }
        });
        Button reset = secondaryButton("Khôi phục mặc định");
        reset.setOnAction(event -> {
            TestSettings defaults = new TestSettings(TestConfig.baseUrl(), TestConfig.headless(),
                    Math.toIntExact(TestConfig.waitTimeout().toSeconds()),
                    TestConfig.screenshotDirectory(), TestConfig.reportDirectory());
            baseUrl.setText(defaults.baseUrl());
            headless.setSelected(defaults.headless());
            waitSeconds.setText(Integer.toString(defaults.waitSeconds()));
            screenshotDirectory.setText(defaults.screenshotDirectory());
            reportDirectory.setText(defaults.reportDirectory());
            outcome.setText("Giá trị mặc định đã điền; nhấn Lưu cấu hình để áp dụng.");
        });
        Button save = primaryButton("Lưu cấu hình");
        save.setOnAction(event -> {
            try {
                TestSettings updated = settingsFromFields(baseUrl, headless, waitSeconds,
                        screenshotDirectory, reportDirectory);
                updated.save();
                settings = updated;
                outcome.setText("Đã lưu cấu hình tại " + TestSettings.settingsFile());
                outcome.getStyleClass().setAll("success-copy");
                loadData();
            } catch (IOException | RuntimeException e) {
                outcome.setText("Không lưu được cấu hình: " + e.getMessage());
                outcome.getStyleClass().setAll("error-copy");
            }
        });
        VBox card = card(form, new Separator(), new Label(
                "Tệp cấu hình: " + TestSettings.settingsFile()),
                new HBox(8, outcome, spacer(), validate, reset, save));
        page.getChildren().add(card);
        return scrollable(page);
    }

    private Node buildProjectPage() {
        VBox page = page("Thông tin dự án",
                "Cấu hình công nghệ và đầu ra thực tế của bộ kiểm thử.");
        VBox details = card(
                sectionHeader("MobileCity Selenium Test Manager", "Ứng dụng desktop JavaFX"),
                new Label("Website kiểm thử: https://mobilecity.vn/"),
                new Label("Java 17 · Maven · Selenium WebDriver · JUnit 5 · Page Object Model"),
                new Label("Số test case có trong catalog: " + TestCaseCatalog.all().size()),
                new Label("Thư mục dự án: " + projectRoot),
                new Label("Báo cáo: " + currentReportDirectory),
                new Label("Lịch sử: " + historyStore.historyDirectory()));
        page.getChildren().add(details);
        return scrollable(page);
    }

    private Node directoryField(TextField field) {
        Button browse = secondaryButton("Chọn…");
        browse.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Chọn thư mục");
            Path initial = resolveProjectPath(field.getText());
            if (Files.isDirectory(initial)) {
                chooser.setInitialDirectory(initial.toFile());
            }
            var selected = chooser.showDialog(stage);
            if (selected != null) {
                field.setText(selected.getAbsolutePath());
            }
        });
        HBox row = new HBox(8, field, browse);
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }

    private static void addField(GridPane form, int row, String label, Node field) {
        Label caption = new Label(label);
        caption.getStyleClass().add("field-label");
        form.add(caption, 0, row);
        form.add(field, 1, row);
    }

    private static TestSettings settingsFromFields(TextField baseUrl, CheckBox headless,
                                                    TextField waitSeconds, TextField screenshots,
                                                    TextField reports) {
        int seconds;
        try {
            seconds = Integer.parseInt(waitSeconds.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Thời gian chờ phải là số nguyên.", e);
        }
        return new TestSettings(baseUrl.getText().trim(), headless.isSelected(), seconds,
                screenshots.getText().trim(), reports.getText().trim());
    }

    private void refreshData() {
        try {
            loadData();
            showPage(currentPage);
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Không thể làm mới dữ liệu", e.getMessage());
        }
    }

    private void loadData() {
        try {
            history = historyStore.loadAll();
            if (selectedHistory != null) {
                selectedHistory = history.stream()
                        .filter(record -> record.id().equals(selectedHistory.id()))
                        .findFirst().orElse(null);
            }
            if (selectedHistory == null && !history.isEmpty()) {
                selectedHistory = history.get(0);
            }
            currentReportDirectory = selectedHistory == null
                    ? projectRoot.resolve("target").resolve("surefire-reports")
                    : selectedHistory.reportDirectory();
            List<TestCaseDefinition> selected = selectedHistory == null
                    ? TestCaseCatalog.all() : selectedDefinitions(selectedHistory.selectors());
            currentSummary = resultService.summarize(currentReportDirectory, selected);
            statusText.setText("Website: chưa kiểm tra  ·  Chrome: xác minh khi chạy");
        } catch (IOException | IllegalArgumentException e) {
            statusText.setText("Không đọc được dữ liệu thực: " + e.getMessage());
        }
    }

    private static List<TestCaseDefinition> selectedDefinitions(String selectors) {
        if (selectors.isBlank()) {
            return List.of();
        }
        Set<String> selected = Set.of(selectors.split(","));
        return TestCaseCatalog.all().stream()
                .filter(testCase -> selected.contains(testCase.selector())).toList();
    }

    private void requestWindowClose(WindowEvent event) {
        if (!runController.isRunning()) {
            runController.close();
            return;
        }
        event.consume();
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Maven/Selenium đang chạy. Gửi yêu cầu dừng an toàn và chỉ đóng ứng dụng "
                        + "sau khi tiến trình kết thúc?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Đang chạy kiểm thử");
        confirmation.setHeaderText("Chưa thể đóng khi test còn hoạt động");
        if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            pendingClose = true;
            runController.cancel(listener());
        }
    }

    private void openPath(Path path) {
        if (path == null || !Files.exists(path)) {
            showAlert(Alert.AlertType.INFORMATION, "Không có bằng chứng",
                    "Không tìm thấy tệp hoặc thư mục:\n" + path);
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            showAlert(Alert.AlertType.ERROR, "Không thể mở tệp",
                    "Môi trường hiện tại không hỗ trợ mở tệp bằng ứng dụng desktop.");
            return;
        }
        try {
            Desktop.getDesktop().open(path.toFile());
        } catch (IOException | UnsupportedOperationException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mở tệp",
                    "Không thể mở " + path + ":\n" + e.getMessage());
        }
    }

    private void openReportDirectory() {
        Path directory = currentReportDirectory == null
                ? null : currentReportDirectory.toAbsolutePath().normalize();
        if (directory == null || !Files.isDirectory(directory)) {
            showAlert(Alert.AlertType.INFORMATION, "Không có thư mục báo cáo",
                    "Thư mục báo cáo hiện tại chưa tồn tại:\n" + directory
                            + "\nChạy kiểm thử trước để tạo báo cáo Surefire.");
            return;
        }

        IOException desktopFailure = null;
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(directory.toFile());
                    return;
                }
            }
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            desktopFailure = e instanceof IOException ioException
                    ? ioException : new IOException(e.getMessage(), e);
        }

        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            try {
                new ProcessBuilder("explorer.exe", directory.toAbsolutePath().toString()).start();
                return;
            } catch (IOException | SecurityException e) {
                if (desktopFailure != null) {
                    e.addSuppressed(desktopFailure);
                }
                showAlert(Alert.AlertType.ERROR, "Không thể mở thư mục báo cáo",
                        "Không thể mở " + directory + " bằng ứng dụng desktop hoặc Explorer:\n"
                                + e.getMessage());
                return;
            }
        }

        String reason = desktopFailure == null
                ? "Môi trường không hỗ trợ mở thư mục bằng ứng dụng desktop."
                : desktopFailure.getMessage();
        showAlert(Alert.AlertType.ERROR, "Không thể mở thư mục báo cáo",
                "Không thể mở " + directory + ":\n" + reason);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    private VBox page(String title, String description, Button... actions) {
        VBox content = new VBox(18);
        content.getStyleClass().add("page-content");
        HBox heading = new HBox(18);
        heading.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(5, titleLabel(title), subtitleLabel(description));
        HBox.setHgrow(titles, Priority.ALWAYS);
        pageHeadingActions = new HBox(8, actions);
        pageHeadingActions.setAlignment(Pos.CENTER_RIGHT);
        pageHeadingActions.getStyleClass().add("page-heading-actions");
        heading.getChildren().addAll(titles, pageHeadingActions);
        content.getChildren().add(heading);
        return content;
    }

    private static ScrollPane scrollable(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-scroll");
        return scroll;
    }

    private VBox card(Node... children) {
        VBox panel = new VBox(14, children);
        panel.getStyleClass().add("card");
        panel.setMaxWidth(Double.MAX_VALUE);
        return panel;
    }

    private Node infoBanner(String message) {
        Label banner = new Label("ⓘ  " + message);
        banner.setWrapText(true);
        banner.getStyleClass().add("info-banner");
        banner.setMaxWidth(Double.MAX_VALUE);
        return banner;
    }

    private Node emptyNotice(String message) {
        Label notice = new Label(message);
        notice.setWrapText(true);
        notice.setAlignment(Pos.CENTER);
        notice.getStyleClass().add("empty-notice");
        notice.setMinHeight(72);
        notice.setMaxWidth(Double.MAX_VALUE);
        return notice;
    }

    private static Node sectionHeader(String title, String subtitle) {
        VBox headings = new VBox(3, sectionTitle(title), subtitleLabel(subtitle));
        return headings;
    }

    private static Label titleLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        return label;
    }

    private static Label subtitleLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("page-subtitle");
        return label;
    }

    private static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    private static Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        button.setTooltip(new Tooltip(text));
        return button;
    }

    private static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        button.setTooltip(new Tooltip(text));
        return button;
    }

    private static <T> TableColumn<T, String> textColumn(String title,
                                                          java.util.function.Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                Optional.ofNullable(value.apply(cell.getValue())).orElse("")));
        column.setPrefWidth(title.length() * 10 + 45);
        return column;
    }

    private static <T> TableColumn<T, String> statusColumn(String title,
                                                           java.util.function.Function<T, String> value) {
        TableColumn<T, String> column = textColumn(title, value);
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null || status.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().addAll("status-pill", statusClass(status));
                setGraphic(badge);
                setText(null);
            }
        });
        return column;
    }

    private static <T> void fitColumns(TableView<T> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private static String statusClass(String status) {
        return switch (status) {
            case "PASS" -> "status-pass";
            case "FAIL" -> "status-fail";
            case "BLOCKED" -> "status-blocked";
            default -> "status-idle";
        };
    }

    private static String accentClass(String color) {
        return switch (color) {
            case GREEN -> "accent-green";
            case RED -> "accent-red";
            case ORANGE -> "accent-orange";
            default -> "accent-blue";
        };
    }

    private static Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private static Path resolveProjectPath(String configured) {
        Path path = Path.of(configured);
        return path.isAbsolute() ? path.normalize()
                : Path.of(System.getProperty("user.dir")).resolve(path).normalize();
    }

    private static String displayDate(Instant instant) {
        return DATE_TIME.format(instant);
    }

    private static String formatDuration(long millis) {
        long seconds = Math.max(0, millis / 1000);
        return "%02d:%02d".formatted(seconds / 60, seconds % 60);
    }

    private static final class CaseRow {
        private final TestCaseDefinition definition;
        private final String status;
        private final BooleanProperty selected;

        private final double seconds;

        private CaseRow(TestCaseDefinition definition, String status, double seconds, boolean selected) {
            this.definition = definition;
            this.status = status;
            this.seconds = seconds;
            this.selected = new SimpleBooleanProperty(selected);
        }

        private TestCaseDefinition definition() {
            return definition;
        }

        private String status() {
            return status;
        }

        private double seconds() {
            return seconds;
        }

        private BooleanProperty selectedProperty() {
            return selected;
        }

        private void setSelected(boolean selected) {
            this.selected.set(selected);
        }
    }

    private record NavigationItem(String id, String icon, String title) {
    }

    private record DashboardData(RunSummary summary, String source) {
    }
}
