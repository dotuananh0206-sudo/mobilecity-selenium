# Kết quả, log và bằng chứng

## Trạng thái test

JavaFX Test Manager đọc kết quả JUnit/Surefire và dùng các trạng thái:

- `PASS`: JUnit hoàn tất test mà không có failure/error.
- `FAIL`: test có assertion failure hoặc lỗi thực thi.
- `BLOCKED`: test bị skip với lý do có chứa `BLOCKED`, ví dụ thiếu điều kiện môi trường hoặc website yêu cầu CAPTCHA/OTP.
- `NOT EXECUTED`: test được chọn nhưng không có kết quả JUnit tương ứng, hoặc bị skip vì cấu hình/điều kiện không mang nhãn BLOCKED.

Lệnh Maven có thể trả về `BUILD FAILURE` khi có test `FAIL`; trạng thái đó không thay đổi phân loại từng test. `BLOCKED` và `NOT EXECUTED` không phải là PASS.

## Vị trí đầu ra

| Đầu ra | Vị trí mặc định | Nội dung |
|---|---|---|
| Báo cáo Surefire | `target/surefire-reports/` | XML và văn bản JUnit; Maven tạo mới khi chạy. |
| Log chạy | `reports/test-execution.log` | Trạng thái/kết quả của test trong lần chạy. |
| Screenshot | `screenshots/` | Ảnh chụp test lỗi nếu driver hỗ trợ; một số test xác thực/giỏ không chụp để bảo vệ dữ liệu. |
| GitHub Actions artifact | `mobilecity-test-results-<run_number>` | Surefire, log và ảnh được tạo; retention 14 ngày. |

Mở file đầu ra bằng công cụ hỗ trợ UTF-8. Thư mục Surefire thuộc `target/` và có thể bị xóa bởi `mvn clean`.

## Xem kết quả nhanh trên Windows

```powershell
Get-ChildItem .\target\surefire-reports
Get-Content .\reports\test-execution.log -Encoding utf8
Get-ChildItem .\screenshots
```

## Kiểm tra mã hóa tài liệu

Repository có script `scripts/validate_run_documents.ps1` để kiểm tra các tệp text/XML trong thư mục được chọn:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/validate_run_documents.ps1 -Directory .
```

Workflow GitHub Actions chạy validator sau Maven và upload artifact dù bước test/validator thất bại. Validator không thay thế test tự động hay xác nhận nội dung nghiệp vụ.

## Diễn giải lần chạy

Luôn ghi ngày giờ, Run ID, môi trường, phạm vi, trạng thái và bằng chứng của chính lần chạy đang báo cáo. Không dùng kết quả local để tuyên bố CI PASS hoặc ngược lại; không suy luận website có defect chỉ từ timeout Selenium khi chưa loại trừ challenge, thay đổi DOM hay vấn đề môi trường.
