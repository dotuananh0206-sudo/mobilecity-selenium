# Báo cáo thực thi Test Case HOME/CATEGORY

## Thông tin lần chạy

- **Run ID:** `MC-20261112-223440`
- **Thời điểm:** 12-11-2026, 22:34–22:36 (UTC+07:00)
- **Lệnh:** `mvn "-Dmobilecity.baseUrl=https://mobilecity.vn/" test`
- **Môi trường:** Windows 11; Apache Maven 3.9.15; Java 26.0.1; Chrome 157.0.8103.52; headless.
- **Kết quả Maven:** `BUILD FAILURE` do các kiểm tra giao diện phát hiện 2 kết quả không đạt; không có lỗi biên dịch hay lỗi thực thi test framework.

## Tổng kết

| Phạm vi | Tổng | PASS | FAIL | BLOCKED | NOT EXECUTED |
|---|---:|---:|---:|---:|---:|
| HOME | 12 | 6 | 1 | 2 | 3 |
| CATEGORY | 10 | 7 | 1 | 0 | 2 |
| **Tổng HOME/CATEGORY** | **22** | **13** | **2** | **2** | **5** |

JUnit đánh dấu 3 ca trong toàn bộ suite là skipped: `TC_05`, `TC_08` và ca giỏ hàng thiếu thông tin đăng nhập. Hai ca skipped trong phạm vi báo cáo được xếp `BLOCKED`, không tính là PASS.

## Kết quả từng Test Case

| Mã | Trạng thái | Kết quả thực tế |
|---|---|---|
| TC_01 | PASS | Trang chủ, Header, banner, iframe, danh sách sản phẩm và giá công khai được xác nhận. |
| TC_02 | NOT EXECUTED | Chưa có bộ dữ liệu chuẩn để đối chiếu giá/tồn kho theo từng khu vực; không khẳng định chỉ đổi giá trị select là đủ. |
| TC_03 | PASS | Các liên kết Máy tính bảng, Tai nghe và Sửa chữa mở đúng đích và nội dung danh mục. |
| TC_04 | PASS | Liên kết Đăng nhập và Đăng ký trên Header mở đúng trang; không gửi thông tin xác thực. |
| TC_05 | BLOCKED | JUnit skipped: banner Phụ kiện có tên/đích ổn định không hiện diện hoặc không nhận diện được trong lần chạy. |
| TC_06 | PASS | Iframe YouTube tồn tại và có kích thước hiển thị khác 0; không đánh giá âm thanh/chất lượng phát. |
| TC_07 | PASS | Lối tắt Samsung Chính hãng mở đúng danh mục thương hiệu. |
| TC_08 | BLOCKED | Chiều tăng dần cập nhật danh sách; sau chọn chiều giảm dần, các sản phẩm đầu danh sách chỉ hiển thị “Liên hệ”, không có đủ giá số để xác minh thứ tự. JUnit skipped, không coi là đạt. |
| TC_09 | NOT EXECUTED | Tương tác mở widget hỗ trợ chưa xác minh đủ ổn định để viết thao tác Selenium đáng tin cậy. |
| TC_10 | NOT EXECUTED | Cần staging/fixture bảo trì hoặc lỗi máy chủ được cho phép; không mô phỏng trên website công khai. |
| TC_11 | FAIL | Ở viewport giả lập 390×844, `documentElement.scrollWidth` lớn hơn `clientWidth`; trang chủ bị tràn ngang. |
| TC_12 | PASS | Năm mẫu LCP trong lần chạy toàn bộ suite đều nhỏ hơn 2500 ms. Xem thêm phép chạy đo mẫu bên dưới. |
| DM_01 | PASS | Menu Điện thoại mở bằng hover và hiển thị nhóm hãng/link danh mục. |
| DM_02 | PASS | Liên kết iPhone Chính hãng VN/A mở đúng danh mục. |
| DM_03 | PASS | Menu Máy tính bảng thay thế menu Điện thoại khi chuyển hover. |
| DM_04 | PASS | Menu đóng khi con trỏ rời vùng danh mục. |
| DM_05 | PASS | Nhấp danh mục gốc Điện thoại mở trang tổng hợp và có tên/giá sản phẩm. |
| DM_06 | PASS | Liên kết Điện thoại Cũ mở đúng trang phân loại. |
| DM_07 | PASS | Liên kết Xiaomi mở đúng danh mục thương hiệu. |
| DM_08 | NOT EXECUTED | Chưa có fixture danh mục rỗng đã xác nhận. |
| DM_09 | NOT EXECUTED | Cần proxy/CDP hoặc fixture cô lập để mô phỏng mất mạng an toàn; không chạy trên website công khai/CI dùng chung. |
| DM_10 | FAIL | Ở viewport 390×844, nhấp Điện thoại không làm menu con xuất hiện; xác nhận thất bại thay vì chờ timeout hoặc làm yếu điều kiện. |

## Kiểm tra LCP bổ sung

Sau lần chạy toàn bộ suite, chạy riêng `TC_12` để ghi lại các mẫu đo bằng `PerformanceObserver`:

```text
mvn "-Dmobilecity.baseUrl=https://mobilecity.vn/" "-Dtest=HomeTest#testTC12_HomeLargestContentfulPaint" test
```

Kết quả targeted run: **1 PASS**, các mẫu LCP (ms): `732, 1008, 612, 592, 368`; cả 5 mẫu đều dưới 2500 ms. Đây là phép đo trên máy/chế độ mạng/cache của lần chạy, không đại diện cho SLA trên mọi thiết bị hoặc mạng.

## Toàn bộ Maven suite

- **35 tests:** 30 PASS, 2 FAIL, 0 lỗi thực thi, 3 skipped.
- 2 FAIL thuộc `TC_11` và `DM_10`; 2 BLOCKED trong phạm vi HOME/CATEGORY là `TC_05` và `TC_08`; ca skipped còn lại là test giỏ hàng thiếu credentials.
- 9 kiểm thử unit của ứng dụng và các test Search/Product trong suite đều PASS.
- Báo cáo Surefire: `target/surefire-reports/`; ảnh bằng chứng lỗi nằm trong `screenshots/`.

Các kết quả là trạng thái thực tế quan sát được trong môi trường ghi trên, tách biệt với kết quả mong đợi trong [TestCases_Home_Category.md](./TestCases_Home_Category.md). Những ca `NOT EXECUTED` chưa được giả lập hoặc tính PASS.
