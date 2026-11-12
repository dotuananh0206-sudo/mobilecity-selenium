# Cấu hình kiểm thử

## Java và Maven

Maven compiler biên dịch mã với Java release 17. Cần JDK 17 trở lên và Maven 3.9 trở lên. Chrome cần được cài khi chạy Selenium cục bộ; Selenium Manager tìm/tải ChromeDriver khi cần.

## Cấu hình test

Test đọc Java system property trước, rồi mới đọc biến môi trường:

| System property | Biến môi trường | Mặc định | Giới hạn/ghi chú |
|---|---|---|---|
| `mobilecity.baseUrl` | `MOBILECITY_BASE_URL` | `https://mobilecity.vn/` | URL HTTP/HTTPS hợp lệ. |
| `mobilecity.headless` | `MOBILECITY_HEADLESS` | `true` | `true` hoặc `false`. |
| `mobilecity.waitSeconds` | `MOBILECITY_WAIT_SECONDS` | `15` | Số nguyên 1–120 giây. |
| `mobilecity.screenshotDir` | `MOBILECITY_SCREENSHOT_DIR` | `screenshots` | Thư mục ảnh khi test lỗi. |
| `mobilecity.reportDir` | `MOBILECITY_REPORT_DIR` | `reports` | Thư mục log chạy. |
| `mobilecity.keepBrowserOpenSeconds` | `MOBILECITY_KEEP_BROWSER_OPEN_SECONDS` | `0` | Giữ Chrome sau suite trong 0–300 giây. |
| `mobilecity.cartChromeProfileDir` | `MOBILECITY_CART_CHROME_PROFILE_DIR` | Không bật | Chrome profile dành riêng cho kiểm thử giỏ. |
| `mobilecity.chromeDebuggerAddress` | `MOBILECITY_CHROME_DEBUGGER_ADDRESS` | Không bật | Địa chỉ Chrome remote debugging. |

Ví dụ PowerShell:

```powershell
$env:MOBILECITY_HEADLESS = "false"
$env:MOBILECITY_WAIT_SECONDS = "30"
mvn test
```

`.env.example` chỉ ghi tên và giá trị cấu hình không nhạy cảm; ứng dụng không tự động nạp file này. Không lưu credentials trong `.env`, file cấu hình, Maven arguments hoặc repository.

## Cấu hình JavaFX Test Manager

Các thiết lập đã lưu trong ứng dụng được ghi ngoài repository tại:

- Windows: `%USERPROFILE%\.mobilecity-test-manager\settings.properties`
- Linux/macOS: `$HOME/.mobilecity-test-manager/settings.properties`

Tệp này chứa URL, headless, timeout, thư mục screenshot và report; không lưu username/password.
