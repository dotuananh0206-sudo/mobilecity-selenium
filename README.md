# Bộ kiểm thử giao diện Selenium cho MobileCity

Dự án Maven cung cấp một bộ kiểm thử khói Selenium WebDriver nhỏ, có thể chạy lặp lại trên các trang công khai của [MobileCity](https://mobilecity.vn/). Các bài kiểm thử sử dụng Java 17+, JUnit 5 và mô hình Page Object.

## Mục tiêu và an toàn

- Kiểm tra trang chủ, trang danh mục/sản phẩm và các thành phần đăng nhập/đăng ký/tìm kiếm hiển thị công khai.
- Tái sử dụng bộ chọn phần tử và cấu hình trình duyệt trong các lớp page/configuration.
- Lưu kết quả Maven thực tế và bằng chứng khi kiểm thử thất bại sau mỗi lần chạy.
- Không gửi truy vấn tìm kiếm, thông tin đăng nhập/đăng ký thật, OTP, giao dịch mua hàng, thanh toán hay dữ liệu thanh toán.
- Không vượt CAPTCHA hoặc cơ chế chống bot. Ghi nhận luồng bị chặn là `BLOCKED`; không đánh dấu bài kiểm thử chưa chạy là `PASS`.
- Đây là bộ kiểm thử khói trên trang công khai, không phải bằng chứng rằng toàn bộ trang web không có lỗi.

## Công nghệ

| Thành phần | Công dụng |
| --- | --- |
| Java 17+ | Cài đặt bài kiểm thử và Page Object; phiên bản biên dịch Maven được đặt là 17 |
| Maven | Quản lý phụ thuộc và vòng đời build/kiểm thử |
| Selenium WebDriver | Tự động hóa trình duyệt; Selenium Manager xử lý ChromeDriver |
| JUnit 5 | Framework kiểm thử và các phép khẳng định |
| Chrome | Trình duyệt chạy cục bộ và trên GitHub Actions |
| Maven Surefire | Chạy JUnit và tạo báo cáo XML/văn bản |
| GitHub Actions | Chạy bộ kiểm thử trên máy ảo Ubuntu do GitHub cung cấp |

`pom.xml` khai báo các phụ thuộc, phiên bản, phiên bản Java biên dịch và cấu hình Surefire. Đây là tệp mô tả dự án Maven; chỉnh sửa tệp này khi cần thay đổi phụ thuộc hoặc plugin build.

## Cấu trúc dự án

```text
.
├── .github/workflows/selenium-test.yml  # Quy trình CI
├── pom.xml                              # Phụ thuộc và cấu hình build/kiểm thử Maven
├── src/
│   ├── main/java/mobilecity/
│   │   ├── config/                      # Cấu hình trình duyệt và kiểm thử
│   │   ├── pages/                       # Các Page Object
│   │   └── utils/                       # Tiện ích dữ liệu kiểm thử và bằng chứng
│   └── test/
│       ├── java/mobilecity/
│       │   ├── base/                    # Vòng đời WebDriver và bằng chứng lỗi
│       │   └── tests/                   # Các lớp kiểm thử JUnit
│       └── resources/testdata/          # Dữ liệu kiểm thử giả lập/công khai
├── screenshots/                         # Ảnh chụp khi thất bại (được tạo tự động)
├── reports/                             # Nhật ký chạy cục bộ (được tạo tự động)
├── target/surefire-reports/             # Báo cáo XML và văn bản của Maven Surefire
├── .env.example                         # Chỉ chứa tên biến/giá trị mẫu, không có bí mật
└── .gitignore
```

`src/test/java` chứa các lớp kiểm thử. `src/main/java` chứa Page Object, cấu hình và tiện ích có thể tái sử dụng. Maven Surefire tạo `target/surefire-reports/`; thư mục này sẽ bị xóa và tạo lại khi chạy `mvn clean`. `screenshots/` chứa ảnh chụp khi một phương thức kiểm thử thất bại và phiên trình duyệt vẫn còn hoạt động. `reports/test-execution.log` lưu kết quả kiểm thử cục bộ.

## Yêu cầu và thiết lập

- JDK 17 trở lên (`java -version`).
- Maven 3.9+ được cài đặt và có trong `PATH` (`mvn -version`). Maven đã có sẵn trên máy ảo Ubuntu của GitHub-hosted runners; quy trình CI không cần cài Maven riêng.
- Cài Google Chrome để chạy cục bộ.
- Có kết nối mạng tới MobileCity và, trong lần chạy đầu, tới các địa chỉ Selenium Manager dùng để tải/tìm ChromeDriver.

Selenium Manager đi kèm Selenium và thường tự chọn/tải ChromeDriver tương thích. Thông thường không cần cài `chromedriver` riêng. Không tắt bảo mật trình duyệt hoặc vượt cơ chế chống bot của trang web để làm cho bài kiểm thử chạy qua.

Có thể cung cấp cấu hình tùy chọn bằng thuộc tính Maven hoặc biến môi trường; thuộc tính Maven được ưu tiên:

| Thuộc tính Maven | Biến môi trường | Mặc định |
| --- | --- | --- |
| `mobilecity.baseUrl` | `MOBILECITY_BASE_URL` | `https://mobilecity.vn/` |
| `mobilecity.headless` | `MOBILECITY_HEADLESS` | `true` |
| `mobilecity.waitSeconds` | `MOBILECITY_WAIT_SECONDS` | `15` |
| `mobilecity.screenshotDir` | `MOBILECITY_SCREENSHOT_DIR` | `screenshots` |
| `mobilecity.reportDir` | `MOBILECITY_REPORT_DIR` | `reports` |

Không lưu mật khẩu, token, dữ liệu cá nhân hoặc thông tin tài khoản thật trong `.env.example`, dữ liệu kiểm thử, mã nguồn hay nhật ký. Bộ kiểm thử hiện tại không cần thông tin đăng nhập.

## Mở dự án bằng IntelliJ IDEA hoặc Eclipse

1. Mở/nhập thư mục này (`mobilecity-selenium`) dưới dạng một dự án Maven hiện có.
2. Chọn JDK 17+ cho dự án và Maven importer.
3. Cho phép Maven tải các phụ thuộc từ `pom.xml`.
4. Chạy một lớp kiểm thử trong `src/test/java` hoặc sử dụng các lệnh Maven bên dưới.

Trong IntelliJ IDEA, chọn **File → Open** rồi chọn thư mục này hoặc `pom.xml`. Trong Eclipse, chọn **File → Import → Existing Maven Projects**, chọn thư mục dự án rồi hoàn tất.

## Chạy kiểm thử trên máy

Chạy toàn bộ bộ kiểm thử tự động đã được triển khai từ thư mục dự án:

```powershell
mvn clean test
```

Chạy một lớp kiểm thử:

```powershell
mvn -Dtest=HomeTest test
```

Một số lệnh khác:

```powershell
mvn -Dtest=SearchTest test
mvn test -Dmobilecity.headless=false
mvn test -Dmobilecity.baseUrl=https://mobilecity.vn/
```

`mvn clean test` xóa kết quả Maven cũ trong `target/`, sau đó biên dịch và chạy bộ kiểm thử. Lệnh này không thể khiến một trang không truy cập được hoặc một bài kiểm thử bị chặn trở thành thành công. Hãy kiểm tra báo cáo Surefire và nhật ký sau mỗi lần chạy.

## Xem kết quả và ảnh chụp

- **Báo cáo Surefire:** `target/surefire-reports/` (`TEST-*.xml` và `*.txt`).
- **Nhật ký chạy cục bộ:** `reports/test-execution.log`.
- **Ảnh chụp khi thất bại:** `screenshots/`.
- **Bằng chứng GitHub Actions:** mở lần chạy workflow và tải artifact `mobilecity-test-results-*`. Artifact bao gồm báo cáo Surefire, nhật ký chạy và mọi ảnh chụp đã được tạo.

Một bài kiểm thử giỏ hàng bị JUnit hủy sẽ được ghi là bỏ qua trong Surefire và phân loại là `BLOCKED` trong sổ theo dõi dự án. Việc thiếu tệp artifact hoặc ảnh chụp không chứng minh bài kiểm thử thành công. Khi cập nhật hồ sơ dự án, hãy lưu cùng nhau Test Run ID, môi trường, phạm vi chính xác, kết quả và bằng chứng.

## Kho GitHub và đẩy thay đổi

Kho mã nguồn là [dotuananh0206-sudo/mobilecity-selenium](https://github.com/dotuananh0206-sudo/mobilecity-selenium). Thư mục dự án này là thư mục gốc của kho. Không khởi tạo hoặc đẩy thư mục bài tập cha lên kho: các workbook nội bộ, báo cáo, ảnh chụp cũ và thông tin cá nhân không thuộc kho mã nguồn công khai.

Xem lại và đẩy thay đổi từ thư mục này:

```powershell
git status --short --branch
git add .github src pom.xml README.md .gitignore .env.example
git status --short
git commit -m "Describe the reviewed change"
git push
```

Trước khi commit, kiểm tra danh sách tệp đã stage để chắc chắn chỉ chứa mã nguồn/cấu hình/tài liệu dự kiến. Nếu kho từ xa có commit mới, hãy fetch và hòa giải lịch sử trước khi đẩy. Không tùy tiện force-push hoặc ghi đè lịch sử từ xa. Không commit `.env`, thông tin xác thực, kết quả `target/` được tạo tự động, ảnh chụp hoặc báo cáo cục bộ.

## GitHub Actions và artifact

Tệp workflow: `.github/workflows/selenium-test.yml`.

- Chạy khi có push lên `main` và pull request nhắm tới `main`.
- Hỗ trợ chạy thủ công bằng `workflow_dispatch`.
- Sử dụng máy ảo `ubuntu-latest` do GitHub cung cấp, Temurin Java 17 và Chrome stable.
- Chạy `mvn --batch-mode --no-transfer-progress clean test`.
- Giới hạn thời gian mỗi job là 20 phút.
- Tải báo cáo Surefire, nhật ký và ảnh chụp lên artifact `mobilecity-test-results-*`, kể cả khi Maven test thất bại.
- Kết quả Maven khác 0 sẽ làm workflow thất bại; tải artifact không biến một lần chạy thất bại thành thành công.

GitHub Actions chạy trên máy ảo của GitHub; nó **không** chạy bên trong trang kho mã hoặc IDE cục bộ của bạn. Để xem một lần chạy, mở tab **Actions** của kho, chọn **MobileCity Selenium Tests**, rồi mở lần chạy tương ứng với commit cần xem. Để tải bằng chứng, mở **Summary** của lần chạy đã hoàn tất và tải artifact. Artifact được lưu trong 14 ngày; hãy tải xuống trước khi hết hạn nếu cần dùng cho báo cáo.

Lịch sử CI mới nhất đã được xác minh tại phiên bản README này:

- [Lần chạy #1](https://github.com/dotuananh0206-sudo/mobilecity-selenium/actions/runs/38047040851): **FAIL**, 4 lỗi kiểm thử, 2 lỗi thực thi, 1 bài bị chặn/bỏ qua trong 7 phương thức đã triển khai; artifact `mobilecity-test-results-1`.
- [Lần chạy #2](https://github.com/dotuananh0206-sudo/mobilecity-selenium/actions/runs/38047533363): **FAIL**, 4 lỗi kiểm thử, 2 lỗi thực thi, 1 bài bị chặn/bỏ qua trong 7 phương thức đã triển khai; artifact `mobilecity-test-results-2`.

Cả hai lần chạy CI cũng còn 6 trong số 13 trường hợp đã thiết kế ở trạng thái `NOT EXECUTED` vì bộ kiểm thử hiện tại chưa triển khai chúng. Các lỗi CI có nghĩa workflow hiện chưa xanh. Những lỗi assertion/timeout được ghi nhận không đủ để kết luận sản phẩm MobileCity có lỗi; hãy kiểm tra artifact của lần chạy và phản hồi trang thực tế trước khi sửa bộ chọn hoặc đưa ra kết luận. Baseline cục bộ mới nhất `MC-20261010-174513` có 6 `PASS` và 1 `BLOCKED` trong 7 phương thức đã chạy, cùng 6 trường hợp được thiết kế chưa chạy; kết quả cục bộ này không thay thế các lần CI thất bại.

## Sự cố thường gặp

| Hiện tượng | Cần kiểm tra |
| --- | --- |
| Không nhận diện được `mvn` hoặc `java` | Cài JDK 17+ và Maven 3.9+, thêm chúng vào `PATH`, mở lại terminal rồi kiểm tra `java -version` / `mvn -version`. |
| Selenium Manager không tìm thấy/tải được ChromeDriver | Xác nhận Chrome đã cài và mạng/proxy cho phép Selenium Manager tải xuống. Đọc lỗi Selenium/Maven; không thay bằng tệp driver chưa được xác minh. |
| Chrome không khởi động trong môi trường headless cục bộ | Xác nhận Chrome đã cài và cập nhật; thử `mvn test -Dmobilecity.headless=false` để quan sát trình duyệt. Không thêm cờ trình duyệt thiếu an toàn khi chưa hiểu tác động. |
| Chờ phần tử hết thời gian hoặc không thấy thành phần | Kiểm tra ảnh chụp, XML Surefire và trang/DOM thực tế. Trang có thể đã thay đổi hoặc trả về chuyển hướng/trang thử thách. Chỉ cập nhật bộ chọn dựa trên bằng chứng DOM quan sát được. |
| CAPTCHA, thử thách chống bot, hạn chế truy cập | Dừng tương tác và ghi trường hợp đó là `BLOCKED` hoặc `NOT EXECUTED`; tuyệt đối không tìm cách vượt cơ chế kiểm soát. |
| Kiểm thử cục bộ thành công nhưng GitHub Actions thất bại | So sánh thông tin trình duyệt/runtime, commit chính xác, ảnh chụp CI và báo cáo Surefire. Thành công cục bộ không chứng minh CI hoặc trang web chạy thành công trong môi trường khác. |
| Một bài kiểm thử bị bỏ qua | Kiểm tra XML/văn bản Surefire và nhật ký chạy. Bài kiểm thử giỏ hàng chủ động hủy nếu không tìm thấy liên kết giỏ hàng công khai; trạng thái là `BLOCKED`, không phải kiểm thử giỏ hàng thành công. |
| Không thấy ảnh chụp | Ảnh chụp chỉ được tạo khi phương thức kiểm thử thất bại và phiên trình duyệt vẫn còn. Hãy dùng báo cáo Surefire và nhật ký chạy làm bằng chứng bổ sung. |

## Lưu ý về kết quả đã xác minh

Kết quả chạy phụ thuộc vào thời điểm và môi trường. Hãy xem hồ sơ chạy đã được lưu trong dự án cùng báo cáo kiểm thử hiện tại để biết lần chạy mới nhất đã hoàn tất; không dùng lại kết quả `PASS` cũ làm kết quả của lần chạy mới. Không có kết quả kiểm thử nào trong tài liệu này chứng nhận MobileCity không có lỗi.
