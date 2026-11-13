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

Catalog hiện có 52 mục được ánh xạ để chạy: 43 phương thức Selenium/nghiệp vụ và 9 unit test. Một số ca có thể được ghi nhận `BLOCKED` nếu website thực tế không cung cấp chức năng hoặc dữ liệu cần kiểm tra; xem bảng phạm vi trong README và báo cáo thực thi.

Tài liệu tham khảo: [Test Plan](../TestPlan.md), [danh mục tổng hợp Test Case](../TestCases.md), [chi tiết HOME/CATEGORY](../TestCases_Home_Category.md) và [báo cáo thực thi](../TestExecutionReport.md). Có 48 ca chức năng được thiết kế; 5 ca HOME/CATEGORY chưa có selector do cần dữ liệu hoặc môi trường chuyên biệt.

Ở màn **Chạy kiểm thử tự động**, có thể tìm theo mã/tên, lọc theo chức năng rồi chọn từng test bằng checkbox. Các nút **Chỉ chọn kết quả**, **Thêm kết quả** và **Bỏ kết quả** áp dụng cho những dòng đang hiển thị; nút chọn tất cả ở tiêu đề áp dụng cho toàn bộ catalog. Sau khi xác nhận lựa chọn, bắt đầu chạy theo chế độ **Chọn test case**.

Ở màn **Quản lý lỗi**, chọn trực tiếp mức độ và phân loại trong từng dòng FAIL; thay đổi được lưu tự động theo mã test tại `.mobilecity-test-manager/defect-assessments.properties`. Mức độ gồm **Chặn phát hành**, **Nghiêm trọng**, **Cao**, **Trung bình**, **Thấp** và **Chưa đánh giá**; phân loại gồm **Chức năng**, **Giao diện/UX**, **Hiệu năng**, **Tương thích**, **Dữ liệu**, **Tự động hóa/kiểm thử**, **Môi trường**, **Khác** và **Chưa phân loại**. Dùng “Chưa đánh giá/Chưa phân loại” khi chưa đủ chứng cứ; JUnit FAIL không tự động chứng minh lỗi thuộc website.

Kết quả được đọc từ Maven Surefire; log và lịch sử được lưu theo cấu hình. Cấu hình ứng dụng được lưu ngoài repository trong thư mục `.mobilecity-test-manager` của user. Không lưu username/password trong cấu hình.

Ứng dụng không xác nhận website hoặc Chrome sẵn sàng chỉ bằng việc mở UI hay lưu cấu hình; môi trường WebDriver được kiểm tra khi thực thi test. Hủy chạy dùng cơ chế dừng an toàn của ứng dụng.
