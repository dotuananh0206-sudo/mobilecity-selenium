# Ứng dụng JavaFX Test Manager

## Khởi chạy

Từ thư mục gốc repository:

```powershell
mvn javafx:run
```

Trên Windows có thể chạy `.\run.bat`; script kiểm tra Maven trong `PATH` rồi gọi lệnh JavaFX.

## Màn hình hiện có

- Tổng quan
- Danh sách test case
- Chạy kiểm thử tự động
- Kết quả kiểm thử
- Quản lý lỗi
- Báo cáo kiểm thử
- Lịch sử chạy
- Cấu hình hệ thống

Catalog hiện có 35 mục được ánh xạ để chạy: 26 phương thức Selenium/nghiệp vụ và 9 unit test. Tám module định hướng không đồng nghĩa mọi chức năng đã có test; xem bảng phạm vi trong README và tài liệu Test Case.

Chọn toàn bộ test hoặc một tập test đã ánh xạ rồi bắt đầu chạy. Kết quả được đọc từ Maven Surefire; log và lịch sử được lưu theo cấu hình. Cấu hình ứng dụng được lưu ngoài repository trong thư mục `.mobilecity-test-manager` của user. Không lưu username/password trong cấu hình.

Ứng dụng không xác nhận website hoặc Chrome sẵn sàng chỉ bằng việc mở UI hay lưu cấu hình; môi trường WebDriver được kiểm tra khi thực thi test. Hủy chạy dùng cơ chế dừng an toàn của ứng dụng.
