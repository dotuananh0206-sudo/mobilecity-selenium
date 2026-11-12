# MobileCity Selenium Test Manager

Dự án kiểm thử giao diện website [MobileCity](https://mobilecity.vn/) bằng Java, Selenium WebDriver, JUnit 5 và Page Object Model. Ứng dụng desktop JavaFX hỗ trợ chọn test, chạy Maven và xem kết quả; đây là dự án QA, không phải mã nguồn của website MobileCity.

## Mục lục

- [Mục tiêu kiểm thử](#mục-tiêu-kiểm-thử)
- [Phạm vi kiểm thử](#phạm-vi-kiểm-thử)
- [Công nghệ và kiến trúc](#công-nghệ-và-kiến-trúc)
- [Ứng dụng JavaFX](#ứng-dụng-javafx)
- [Yêu cầu và cài đặt](#yêu-cầu-và-cài-đặt)
- [Chạy ứng dụng và kiểm thử](#chạy-ứng-dụng-và-kiểm-thử)
- [Xem báo cáo và bằng chứng](#7-xem-báo-cáo-và-bằng-chứng)
- [GitHub Actions](#github-actions)
- [An toàn](#an-toàn)
- [Tài liệu và phát triển tiếp](#tài-liệu-và-phát-triển-tiếp)

## Mục tiêu kiểm thử

- Kiểm tra hiển thị và điều hướng trên trang chủ, danh mục và trang sản phẩm.
- Kiểm tra tìm kiếm, các lối vào lọc/sắp xếp hiện có và thông tin sản phẩm công khai.
- Mở rộng dần phạm vi tới tin tức/khuyến mãi, thông tin liên hệ/cửa hàng và chính sách bảo hành.
- Thu thập kết quả thực thi và bằng chứng; tự động hóa các trường hợp phù hợp với Selenium.

## Phạm vi kiểm thử

Bảng dưới đây phân biệt thiết kế Test Case, trạng thái tự động hóa và kết quả lần chạy local gần nhất. Chi tiết trạng thái theo mã nằm trong [TestExecutionReport.md](./TestExecutionReport.md); các ca HOME/CATEGORY và độ bao phủ Use Case nằm trong [TestCases_Home_Category.md](./TestCases_Home_Category.md).

| STT | Module | Chức năng | Nội dung kiểm thử | Trạng thái hiện tại |
|---:|---|---|---|---|
| 1 | `HOME` | Xem trang chủ | Giao diện, Header, banner/video, sản phẩm, điều hướng, viewport và LCP. Có 12 ca được thiết kế; 9 phương thức HOME được tự động hóa. | Đã thiết kế và tự động hóa một phần; lần chạy gần nhất có PASS, `TC_11` FAIL, `TC_05`/`TC_08` BLOCKED; các ca chưa có code được nêu trong báo cáo. |
| 2 | `CATEGORY` | Xem danh mục sản phẩm | Menu đa cấp, đổi/đóng menu, điều hướng hãng/phân loại và viewport điện thoại. Có 10 ca được thiết kế; 7 phương thức được tự động hóa. | Đã thiết kế và tự động hóa một phần; `DM_01`–`DM_07` PASS, `DM_10` FAIL; `DM_08`/`DM_09` chưa thực thi do thiếu fixture/môi trường an toàn. |
| 3 | `SEARCH` | Tìm kiếm sản phẩm | Trường bắt buộc, kết quả khớp, trạng thái rỗng và mở trang chi tiết. | Có 4 Test Case và 4 phương thức Selenium; PASS trong lần chạy gần nhất. |
| 4 | `FILTER & SORT` | Lọc và sắp xếp sản phẩm | Lối tắt hãng từ Home và sắp xếp giá; chưa có bộ Test Case độc lập riêng cho module này. | Tự động hóa một phần qua `TC_07` và `TC_08`; `TC_07` PASS, `TC_08` BLOCKED do danh sách sau sort hiển thị “Liên hệ” thay vì đủ giá số. |
| 5 | `PRODUCT` | Xem chi tiết sản phẩm | Mở chi tiết, giá công khai, ảnh chính và thông số công khai. | Có 4 Test Case và 4 phương thức Selenium; PASS trong lần chạy gần nhất. |
| 6 | `NEWS & PROMOTION` | Xem tin tức và khuyến mãi | Chưa có Test Case nghiệp vụ hoặc lớp Selenium độc lập được xác nhận. | Chưa triển khai; cần đặc tả/fixture và tiêu chí kiểm thử trước khi tự động hóa. |
| 7 | `CONTACT & STORE` | Xem thông tin liên hệ và hệ thống cửa hàng | Chưa có Test Case nghiệp vụ hoặc lớp Selenium độc lập được xác nhận. | Chưa triển khai; cần đặc tả và dữ liệu đối chiếu được phê duyệt. |
| 8 | `WARRANTY POLICY` | Xem chính sách bảo hành | Chưa có Test Case nghiệp vụ hoặc lớp Selenium độc lập được xác nhận. | Chưa triển khai; cần đặc tả và tiêu chí kiểm thử trước khi tự động hóa. |

`CartTest` là kiểm thử bổ sung ngoài tám module chính: có một phương thức Selenium, nhưng lần chạy gần nhất bị `BLOCKED` do thiếu thông tin đăng nhập thử nghiệm. Không dùng số lượng Unit Test để thay cho Test Case nghiệp vụ.

## Công nghệ và kiến trúc

| Công nghệ | Vai trò |
|---|---|
| Java 17+ | Ngôn ngữ chạy ứng dụng và viết test; mã được biên dịch theo Java 17. |
| Maven | Tải thư viện, biên dịch mã và điều phối các lệnh chạy ứng dụng/test. |
| Selenium WebDriver 4.29.0 | Điều khiển Chrome để thao tác và kiểm tra giao diện website; Selenium Manager hỗ trợ tìm ChromeDriver. |
| JUnit Jupiter 5.11.4 | Khai báo, nhóm và chạy các test; ghi nhận kết quả từng ca. |
| JavaFX 17.0.14 | Xây dựng giao diện desktop để chọn test, chạy và xem kết quả. |
| Maven Surefire 3.5.2 | Kết nối Maven với JUnit, tạo báo cáo test XML và văn bản. |
| SLF4J 2.0.16 | Ghi nhật ký hoạt động của ứng dụng và quá trình test. |
| GitHub Actions | Tự động chạy test trên CI khi push, tạo pull request hoặc chạy workflow thủ công. |

### Cấu trúc và trách nhiệm

Mã nguồn chia theo vai trò: Page Object đóng gói locator và thao tác trên từng trang; test gọi các thao tác đó để kiểm tra hành vi. `WebDriverWait` giúp chờ điều kiện giao diện thay vì dựa vào thời gian cố định.

```text
.
├── .github/workflows/selenium-test.yml   # Workflow CI chạy test và lưu artifact
├── .mvn/jvm.config                       # Cấu hình JVM cho Maven
├── scripts/validate_run_documents.ps1    # Kiểm tra mã hóa UTF-8 tài liệu/kết quả
├── src/
│   ├── main/java/mobilecity/
│   │   ├── config/                       # URL, trình duyệt và cấu hình chạy test
│   │   ├── manager/                      # Ứng dụng JavaFX, catalog, điều phối/kết quả
│   │   ├── pages/                        # Page Object: locator và thao tác trang
│   │   └── utils/                        # Tiện ích như log và lưu bằng chứng
│   ├── main/resources/mobilecity/manager/ # Tài nguyên giao diện JavaFX
│   └── test/
│       ├── java/mobilecity/
│       │   ├── base/                     # Lớp nền và extension JUnit dùng chung
│       │   ├── manager/                  # Unit test cho thành phần quản lý
│       │   ├── tests/                    # Selenium test theo chức năng
│       │   └── utils/                    # Unit test cho tiện ích
│       └── resources/                    # Tài nguyên phục vụ test
├── screenshots/                         # Ảnh chụp test khi thất bại
├── reports/                              # Nhật ký chạy local
├── pom.xml
├── run.bat
├── TestCases_Home_Category.md
└── TestExecutionReport.md
```

`target/surefire-reports/` được Maven tạo khi chạy test. `screenshots/`, `reports/` và `target/` là đầu ra runtime; không phải tài liệu nguồn. Chưa có ảnh chụp phù hợp của giao diện JavaFX trong repository để đưa vào README.

## Ứng dụng JavaFX

Ứng dụng có các màn Tổng quan, Danh sách test case, Chạy kiểm thử tự động, Kết quả kiểm thử, Quản lý lỗi, Báo cáo kiểm thử, Lịch sử chạy và Cấu hình hệ thống. Có thể chạy cả suite hoặc chọn các test đã ánh xạ trong catalog. Cấu hình local của ứng dụng được lưu ngoài repository; xem [hướng dẫn Test Manager](./docs/TEST_MANAGER.md).

## Yêu cầu và cài đặt

- JDK 17 trở lên; Maven 3.9 trở lên.
- Google Chrome khi chạy Selenium cục bộ.
- Kết nối mạng tới MobileCity và dịch vụ tải/tìm ChromeDriver của Selenium Manager.
- Kiểm thử giỏ hàng cần tài khoản thử nghiệm được phép sử dụng; CAPTCHA/OTP có thể khiến ca bị BLOCKED.

```powershell
git clone https://github.com/dotuananh0206-sudo/mobilecity-selenium.git
Set-Location mobilecity-selenium
```

Mở `pom.xml` dưới dạng Maven project trong IDE và đặt JDK 17+. Các dependency được tải theo manifest Maven. Không cần cài ChromeDriver riêng trong cấu hình thông thường.

## Chạy ứng dụng và kiểm thử

Chạy các lệnh tại thư mục gốc repository. Lệnh khởi chạy ứng dụng JavaFX không chạy Selenium test.

### 1. Khởi chạy ứng dụng quản lý kiểm thử

| STT | Phương thức khởi chạy | Lệnh thực hiện | Mô tả |
|---:|---|---|---|
| 1 | Maven | `mvn javafx:run` | Khởi chạy ứng dụng desktop JavaFX. |
| 2 | Windows batch | `.\run.bat` | Kiểm tra Maven trong `PATH`, sau đó chạy ứng dụng JavaFX. |

### 2. Kiểm tra môi trường

| STT | Thành phần | Lệnh kiểm tra | Yêu cầu |
|---:|---|---|---|
| 1 | Java JDK | `java -version` và `javac -version` | JDK 17 trở lên. |
| 2 | Maven | `mvn -version` | Maven 3.9 trở lên. |
| 3 | Google Chrome | `@("$env:ProgramFiles\Google\Chrome\Application\chrome.exe", "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe", "$env:LOCALAPPDATA\Google\Chrome\Application\chrome.exe") \| Where-Object { Test-Path $_ }` | Chrome phải được cài để chạy Selenium cục bộ. |
| 4 | Kết nối mạng | `Test-NetConnection mobilecity.vn -Port 443` | Cần truy cập website và để Selenium Manager tìm/tải ChromeDriver khi cần. |
| 5 | Biên dịch project | `mvn -DskipTests test-compile` | Xác minh cấu hình Maven và biên dịch mà không chạy test. |

### 3. Chạy kiểm thử tổng quát

| STT | Hình thức kiểm thử | Lệnh thực hiện | Mô tả |
|---:|---|---|---|
| 1 | Chạy toàn bộ | `mvn clean test` | Xóa kết quả Maven cũ, biên dịch và chạy toàn bộ suite. |
| 2 | Chạy không clean | `mvn test` | Chạy test mà không xóa `target/` trước đó. |

Website công khai có thể thay đổi hoặc chặn tự động hóa; xem trạng thái và bằng chứng của đúng lần chạy trước khi kết luận.

### 4. Chạy kiểm thử theo từng module

Các lệnh bên dưới chọn lớp JUnit thực tế trong `src/test/java/`.

| STT | Module | Lớp Test | Lệnh thực hiện | Mục đích |
|---:|---|---|---|---|
| 1 | HOME | `HomeTest` | `mvn "-Dtest=HomeTest" test` | Trang chủ, điều hướng, banner, sắp xếp và viewport. |
| 2 | CATEGORY | `CategoryTest` | `mvn "-Dtest=CategoryTest" test` | Menu danh mục và điều hướng danh mục. |
| 3 | SEARCH | `SearchTest` | `mvn "-Dtest=SearchTest" test` | Tìm kiếm, kết quả và mở sản phẩm. |
| 4 | FILTER & SORT | `HomeTest` | `mvn "-Dtest=HomeTest" test` | Các ca hãng và sắp xếp giá nằm trong `HomeTest`; chưa có lớp riêng. |
| 5 | PRODUCT | `ProductTest` | `mvn "-Dtest=ProductTest" test` | Trang chi tiết và thông tin sản phẩm. |
| 6 | NEWS & PROMOTION | Chưa triển khai | — | Chưa có lớp Test chức năng tương ứng. |
| 7 | CONTACT & STORE | Chưa triển khai | — | Chưa có lớp Test chức năng tương ứng. |
| 8 | WARRANTY POLICY | Chưa triển khai | — | Chưa có lớp Test chức năng tương ứng. |

### 5. Cấu hình chế độ chạy

Các tham số dưới đây được đọc từ Maven system properties; mặc định headless là `true`.

| STT | Chế độ | Lệnh thực hiện | Ý nghĩa |
|---:|---|---|---|
| 1 | Hiển thị Chrome | `mvn test "-Dmobilecity.headless=false"` | Quan sát trực tiếp trình duyệt. |
| 2 | Headless | `mvn test "-Dmobilecity.headless=true"` | Chạy mà không hiển thị cửa sổ trình duyệt. |
| 3 | Thay đổi URL | `mvn test "-Dmobilecity.baseUrl=https://mobilecity.vn/"` | Chỉ định website kiểm thử. |
| 4 | Thay đổi thời gian chờ | `mvn test "-Dmobilecity.waitSeconds=30"` | Đặt thời gian chờ 30 giây; giá trị hợp lệ 1–120 giây. |
| 5 | Giữ Chrome mở | `mvn test "-Dmobilecity.headless=false" "-Dmobilecity.keepBrowserOpenSeconds=60"` | Giữ trình duyệt mở 60 giây sau khi test kết thúc; giá trị hợp lệ 0–300 giây. |

Tham số cấu hình khác và biến môi trường được liệt kê trong [CONFIGURATION.md](./docs/CONFIGURATION.md).

### 6. Chạy kiểm thử giỏ hàng

`CartTest` là kiểm thử bổ sung ngoài tám module trên. Test thay đổi tạm thời số lượng sản phẩm và thử khôi phục; không checkout hoặc đặt hàng.

| STT | Nội dung | Hướng dẫn |
|---:|---|---|
| 1 | Chuẩn bị tài khoản | Chỉ dùng tài khoản thử nghiệm được phép. CAPTCHA/OTP có thể khiến test bị chặn. |
| 2 | Cấu hình thông tin | Đặt `MOBILECITY_USERNAME` và `MOBILECITY_PASSWORD` trong biến môi trường; không ghi giá trị vào mã nguồn hoặc README. |
| 3 | Khởi chạy | `mvn "-Dtest=CartTest" "-Dmobilecity.headless=false" test` |
| 4 | Lưu ý | Không ghi mật khẩu vào lệnh, log hay repository. Hướng dẫn cấu hình an toàn, profile riêng và remote debugging: [CART_TESTING.md](./docs/CART_TESTING.md). |

### 7. Xem báo cáo và bằng chứng

| STT | Thành phần | Đường dẫn | Nội dung |
|---:|---|---|---|
| 1 | Báo cáo Surefire | `target/surefire-reports/` | Kết quả JUnit dạng XML và văn bản. |
| 2 | Log kiểm thử | `reports/test-execution.log` | Nhật ký lần chạy; thư mục có thể được đổi bằng cấu hình. |
| 3 | Screenshots | `screenshots/` | Ảnh bằng chứng khi test lỗi và driver hỗ trợ chụp. |
| 4 | Báo cáo tổng hợp | [TestExecutionReport.md](./TestExecutionReport.md) | Kết quả đã ghi nhận cho các lần chạy được báo cáo. |
| 5 | GitHub Actions | [Workflow MobileCity Selenium Tests](https://github.com/dotuananh0206-sudo/mobilecity-selenium/actions/workflows/selenium-test.yml) | Mở run để xem trạng thái và tải artifact `mobilecity-test-results-<run_number>` (lưu 14 ngày). |

Surefire reports nằm trong `target/` và có thể bị xóa bởi `mvn clean`. Hướng dẫn chi tiết về log, XML, ảnh và artifact CI nằm tại [REPORTING.md](./docs/REPORTING.md).

Lần chạy full suite local gần nhất được ghi trong [TestExecutionReport.md](./TestExecutionReport.md): ngày 12-11-2026, Windows 11, Maven 3.9.15, Java 26.0.1, Chrome 157 headless; **30 PASS, 2 FAIL, 3 skipped trên 35 test**. Trong 22 case HOME/CATEGORY: **13 PASS, 2 FAIL, 2 BLOCKED, 5 NOT EXECUTED**. Kết quả này chỉ thuộc lần chạy local nêu trên; CI chưa được chạy lại để xác minh thay đổi hiện tại.

### 8. Theo dõi trạng thái kiểm thử

| Trạng thái | Ý nghĩa |
|---|---|
| `PASS` | Kiểm thử đạt kết quả mong đợi. |
| `FAIL` | Kiểm thử không đạt kết quả mong đợi. |
| `BLOCKED` | Không thể tiếp tục thực hiện kiểm thử. |
| `NOT EXECUTED` | Chưa thực hiện kiểm thử. |

## GitHub Actions

Workflow [MobileCity Selenium Tests](./.github/workflows/selenium-test.yml) chạy khi push hoặc pull request nhắm tới `main`, hoặc khi khởi chạy thủ công (`workflow_dispatch`). Dùng `ubuntu-latest`, Temurin Java 17, Chrome stable và lệnh:

```bash
mvn --batch-mode --no-transfer-progress clean test
```

Job timeout là 20 phút. Workflow kiểm tra UTF-8 và luôn thử tải Surefire reports, log và ảnh làm artifact. Xem tab **Actions** của GitHub repository, mở run cần kiểm tra rồi tải artifact `mobilecity-test-results-*`. Nếu workflow thất bại, artifact là bằng chứng chẩn đoán, không biến run thành PASS. Không có badge CI trong README vì trạng thái live chưa được xác minh cho phiên bản hiện tại.

## An toàn

- Không vượt CAPTCHA, OTP hoặc cơ chế chống bot; ghi nhận test bị chặn đúng trạng thái.
- Không đặt hàng, checkout hoặc thanh toán thật.
- Không commit credentials, `.env`, cookie hay Chrome profile; không chia sẻ profile.
- Không ghi dữ liệu đăng nhập vào log/ảnh; ảnh chụp các test đăng nhập/giỏ hàng được hạn chế.
- Không suy diễn PASS từ test chưa chạy, artifact thiếu hoặc một lần chạy cũ.

## Tài liệu và phát triển tiếp

- [Test Case HOME/CATEGORY](./TestCases_Home_Category.md) — đặc tả, khả năng tự động hóa và độ bao phủ Use Case.
- [Báo cáo thực thi HOME/CATEGORY](./TestExecutionReport.md) — kết quả local gần nhất theo mã Test Case.
- [Cấu hình](./docs/CONFIGURATION.md) — system properties, biến môi trường và thiết lập local.
- [Kiểm thử giỏ hàng](./docs/CART_TESTING.md) — credentials, profile riêng và remote debugging.
- [Ứng dụng Test Manager](./docs/TEST_MANAGER.md) — các màn hình và cách sử dụng.
- [Báo cáo và bằng chứng](./docs/REPORTING.md) — log, Surefire, ảnh, UTF-8 và artifact CI.

`NEWS & PROMOTION`, `CONTACT & STORE` và `WARRANTY POLICY` chưa có bộ Test Case riêng trong repository hiện tại. Bổ sung đặc tả và dữ liệu kiểm thử trước, sau đó triển khai theo Java/Selenium/JUnit 5/POM; cập nhật trạng thái và kết quả dựa trên bằng chứng mới.
