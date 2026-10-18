package mobilecity.manager;

import java.util.List;
import java.util.Optional;

public final class TestCaseCatalog {
    private static final List<TestCaseDefinition> CASES = List.of(
            new TestCaseDefinition("TC-HOME-001", "Trang chủ tải và hiển thị tiêu đề", "Trang chủ",
                    "HomeTest", "homepageLoadsWithTitle", "Truy cập được MobileCity.",
                    "Mở trang chủ; kiểm tra tiêu đề, logo và liên kết Điện thoại.",
                    "Trang chủ có tiêu đề, logo và điều hướng danh mục."),
            new TestCaseDefinition("TC-HOME-002", "Điều hướng đến danh mục điện thoại", "Trang chủ",
                    "HomeTest", "homepageOpensPhoneCategory", "Trang chủ có liên kết danh mục.",
                    "Mở trang chủ; nhấn liên kết Điện thoại.",
                    "Mở trang danh mục điện thoại với tiêu đề phù hợp."),
            new TestCaseDefinition("TC-HOME-003", "Danh mục hiển thị sản phẩm có tên và giá", "Sản phẩm",
                    "ProductTest", "categoryShowsNamedProduct", "Danh mục điện thoại công khai.",
                    "Mở danh mục; kiểm tra tên và giá sản phẩm đầu tiên.",
                    "Thẻ sản phẩm có tên và giá được công bố."),
            new TestCaseDefinition("TC-SEARCH-001", "Ô tìm kiếm chặn nội dung để trống", "Tìm kiếm",
                    "SearchTest", "emptySearchInputIsRequired", "Trang chủ có ô tìm kiếm.",
                    "Mở trang chủ; gửi truy vấn tìm kiếm rỗng.",
                    "Trường tìm kiếm được đánh dấu là bắt buộc."),
            new TestCaseDefinition("TC-SEARCH-002", "Tìm kiếm sản phẩm theo từ khóa", "Tìm kiếm",
                    "SearchTest", "searchReturnsMatchingProducts", "Từ khóa mẫu trong testdata.properties.",
                    "Tìm kiếm từ khóa Samsung; kiểm tra tiêu đề và kết quả.",
                    "Có ít nhất một kết quả khớp từ khóa."),
            new TestCaseDefinition("TC-SEARCH-003", "Từ khóa không khớp hiển thị trạng thái rỗng", "Tìm kiếm",
                    "SearchTest", "searchShowsEmptyStateForNoMatch", "Từ khóa không tồn tại trong dữ liệu test.",
                    "Tìm kiếm chuỗi không khớp; kiểm tra thông báo kết quả rỗng.",
                    "Trang nêu rõ không tìm thấy sản phẩm phù hợp."),
            new TestCaseDefinition("TC-SEARCH-004", "Mở chi tiết sản phẩm từ kết quả tìm kiếm", "Tìm kiếm",
                    "SearchTest", "searchResultOpensProductDetails", "Tìm kiếm có ít nhất một kết quả.",
                    "Mở kết quả đầu tiên; so sánh tên sản phẩm.",
                    "Trang chi tiết khớp sản phẩm đã chọn."),
            new TestCaseDefinition("TC-PRODUCT-001", "Mở chi tiết sản phẩm từ danh mục", "Sản phẩm",
                    "ProductTest", "listedProductOpensMatchingDetailPage", "Danh mục có sản phẩm.",
                    "Mở danh mục; chọn sản phẩm đầu tiên.",
                    "Tiêu đề trang chi tiết khớp sản phẩm đã chọn."),
            new TestCaseDefinition("TC-PRODUCT-002", "Trang chi tiết hiển thị giá sản phẩm", "Sản phẩm",
                    "ProductTest", "productDetailsShowPrice", "URL sản phẩm mẫu được cấu hình.",
                    "Mở trang chi tiết; kiểm tra giá công khai.",
                    "Trang chi tiết hiển thị giá có chữ số."),
            new TestCaseDefinition("TC-PRODUCT-003", "Ảnh chính sản phẩm tải thành công", "Sản phẩm",
                    "ProductTest", "productPrimaryImageIsLoaded", "URL sản phẩm mẫu được cấu hình.",
                    "Mở trang chi tiết; kiểm tra ảnh chính đã tải.",
                    "Ảnh chính sản phẩm tải thành công."),
            new TestCaseDefinition("TC-PRODUCT-004", "Hiển thị thông số công khai", "Sản phẩm",
                    "ProductTest", "productDetailsShowPublicSpecifications", "URL sản phẩm mẫu được cấu hình.",
                    "Mở trang chi tiết; kiểm tra tiêu đề và khu vực thông số.",
                    "Trang chi tiết có khu vực thông số công khai."),
            new TestCaseDefinition("TC-CART-001", "Tăng rồi khôi phục số lượng mặt hàng đầu tiên", "Giỏ hàng",
                    "CartTest", "loginAndTemporarilyChangeFirstCartItemQuantity",
                    "Tài khoản thử nghiệm/phiên đăng nhập hợp lệ; có thể cần xử lý CAPTCHA/OTP thủ công.",
                    "Đăng nhập nếu cần; mở giỏ qua liên kết hiển thị; tăng một đơn vị rồi khôi phục.",
                    "Số lượng gốc được khôi phục; CAPTCHA/OTP hoặc thiếu credentials được BLOCKED."),
            new TestCaseDefinition("UNIT-001", "Ghi bằng chứng tiếng Việt UTF-8", "Kiểm thử nội bộ",
                    "EvidenceWriterTest", "writesVietnameseEvidenceAsUtf8", "Thư mục tạm do JUnit cung cấp.",
                    "Ghi và đọc lại nội dung tiếng Việt từ evidence log.",
                    "Nội dung được lưu UTF-8 và không bị thay ký tự."),
            new TestCaseDefinition("UNIT-002", "Ẩn thông tin nhạy cảm khỏi log", "Kiểm thử nội bộ",
                    "EvidenceWriterTest", "redactsCredentialLikeValuesFromEvidence", "Thư mục tạm do JUnit cung cấp.",
                    "Ghi các trường giống credentials vào log test.",
                    "Giá trị nhạy cảm bị loại bỏ khỏi log."),
            new TestCaseDefinition("UNIT-003", "Kiểm tra tính duy nhất của catalog JUnit", "Kiểm thử nội bộ",
                    "TestCaseCatalogTest", "catalogContainsOnlyUniqueImplementedSelectors",
                    "Catalog test case được khởi tạo.",
                    "Kiểm tra mã và selector test không trùng nhau.",
                    "Mỗi test trong catalog có ID và selector JUnit duy nhất."),
            new TestCaseDefinition("UNIT-004", "Đọc trạng thái từ báo cáo Surefire", "Kiểm thử nội bộ",
                    "RunResultServiceTest", "readsSurefireStatusesAndLeavesMissingCasesUnexecuted",
                    "Báo cáo XML tạm có PASS, FAIL, skipped và case thiếu.",
                    "Đọc XML; tổng hợp status và test chưa thực thi.",
                    "Các trạng thái JUnit được phân biệt chính xác."),
            new TestCaseDefinition("UNIT-005", "Từ chối XML có external entity", "Kiểm thử nội bộ",
                    "RunResultServiceTest", "rejectsXmlExternalEntityDeclarations",
                    "Báo cáo XML kiểm thử chứa khai báo DOCTYPE.",
                    "Thử đọc XML có external entity.",
                    "Parser từ chối XML không an toàn."),
            new TestCaseDefinition("UNIT-006", "Lưu và đọc lại lịch sử chạy", "Kiểm thử nội bộ",
                    "RunHistoryStoreTest", "persistsAndReloadsRunHistory",
                    "Thư mục tạm do JUnit cung cấp.",
                    "Lưu bản ghi run rồi đọc danh sách lịch sử.",
                    "Trường lịch sử được khôi phục chính xác."),
            new TestCaseDefinition("UNIT-007", "Chấp nhận cấu hình test được hỗ trợ", "Kiểm thử nội bộ",
                    "TestSettingsTest", "acceptsSupportedTestConfigValues",
                    "URL HTTP(S), thời gian chờ trong giới hạn và hai thư mục hợp lệ.",
                    "Xác thực giá trị cấu hình hợp lệ.",
                    "Cấu hình hợp lệ được chấp nhận."),
            new TestCaseDefinition("UNIT-008", "Từ chối cấu hình không hợp lệ", "Kiểm thử nội bộ",
                    "TestSettingsTest", "rejectsUnsupportedUrlWaitAndEmptyOutputPaths",
                    "Cấu hình lần lượt có URL, thời gian chờ hoặc đường dẫn không hợp lệ.",
                    "Xác thực từng cấu hình không hợp lệ.",
                    "Cấu hình sai bị từ chối."),
            new TestCaseDefinition("UNIT-009", "Nhận biết tín hiệu dừng an toàn", "Kiểm thử nội bộ",
                    "RunCancellationSignalTest", "detectsOnlyAnExistingConfiguredCancellationFile",
                    "Đường dẫn marker nằm trong thư mục tạm.",
                    "Kiểm tra trước và sau khi tạo tệp tín hiệu.",
                    "Chỉ nhận tín hiệu khi marker thực sự tồn tại."));

    private TestCaseCatalog() {
    }

    public static List<TestCaseDefinition> all() {
        return CASES;
    }

    public static Optional<TestCaseDefinition> find(String className, String methodName) {
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        return CASES.stream()
                .filter(testCase -> testCase.className().equals(simpleClassName)
                        && testCase.methodName().equals(methodName))
                .findFirst();
    }

    public static Optional<TestCaseDefinition> findById(String id) {
        return CASES.stream().filter(testCase -> testCase.id().equals(id)).findFirst();
    }
}
