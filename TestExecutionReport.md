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

## Kiểm tra mục tiêu sau khi đồng bộ Test Case

- **Ngày:** 13-11-2026.
- **Home:** Run `MC-20261113-141704`, lệnh `mvn -q "-Dtest=HomeTest,TestCaseCatalogTest" test`; 10 test Home, 8 PASS, 2 BLOCKED (`TC_05`, `TC_08`), 0 FAIL/ERROR. `TC_11` PASS ở Desktop và viewport điện thoại. Năm mẫu LCP của `TC_12` (ms): `680, 592, 336, 404, 340`; đều dưới 2500 ms.
- **Ca bổ sung:** `TC_06` và `DM_05` PASS trong các lần chạy mục tiêu; kiểm tra `TestCaseCatalogTest` PASS.
- **Search (lịch sử trước khi đồng bộ mã):** Run `MC-20261113-143851` và `MC-20261113-144155` dùng các mã `TC-SEARCH-001`–`TC-SEARCH-004`; đây là kết quả của bộ test cũ.
- **Category:** Run `MC-20261113-142027`, lệnh `mvn -q "-Dtest=CategoryTest#testDM10_CategoryMenuOnMobileViewport" test`; `DM_10` FAIL vì không tìm thấy liên kết menu con sau thao tác mở trên viewport điện thoại. Ca này không tính PASS chỉ vì trang danh mục gốc có thể truy cập.
- **Search (mã theo tài liệu đặc tả):** Run `MC-20261113-145523`, lệnh `mvn -q "-Dtest=SearchTest,TestCaseCatalogTest,RunResultServiceTest" test`; 4/5 ca Search PASS (`TC_SR_01`, `TC_SR_02`, `TC_SR_04`, `TC_SR_05`), `TC_SR_03` FAIL vì không có gợi ý nhanh kèm tên, ảnh và giá trong thời hạn 2 giây. Ba test catalog/service PASS; tổng cộng 8 test, 7 PASS, 1 FAIL, 0 ERROR. Đây là lỗi chức năng quan sát được trên website trong lần chạy, không được tính là PASS.
- **FILTER & SORT (mã mục 3.4.5):** Run `MC-20261113-152814`, lệnh `mvn -q "-Dtest=FilterSortTest,TestCaseCatalogTest" test`; `TC_FS_01`–`TC_FS_03` FAIL vì trang danh mục công khai không có bộ lọc hãng/khoảng giá; `TC_FS_04` và `TC_FS_05` BLOCKED vì sau chọn sắp xếp không còn ít nhất hai giá số để xác minh thứ tự. Catalog PASS; tổng cộng 6 test, 3 FAIL, 2 BLOCKED, 1 PASS, 0 ERROR.
- **PRODUCT (mã mục 3.4.6):** Run `MC-20261113-154132`, lệnh `mvn -q "-Dtest=ProductTest,TestCaseCatalogTest,RunResultServiceTest" test`; `TC_PR_01`, `TC_PR_02` và `TC_PR_04` PASS. `TC_PR_03` FAIL: sau khi chọn 4-128GB, tùy chọn được đánh dấu chọn nhưng giá vẫn `2.450.000 ₫`, bằng giá hiển thị ở 6-128GB. Catalog/service PASS; tổng cộng 7 test, 6 PASS, 1 FAIL, 0 ERROR. `TC_PR_04` xác nhận màu và ảnh chính đổi; helper đã được sửa để đọc nhãn màu hiển thị và ảnh trong slide đang active.

## Kiểm tra mới nhất: NEWS, CONTACT, WARRANTY và toàn bộ suite

- **CONTACT & STORE targeted:** Run `MC-20261113-161443`, lệnh `mvn -q "-Dtest=ContactStoreTest" test`; `TC_CS_01` và `TC_CS_04` PASS, `TC_CS_02` FAIL do trang không hiển thị giờ làm việc, `TC_CS_03` BLOCKED vì không có bộ lọc cửa hàng theo khu vực. Không còn lỗi thực thi khi kiểm tra bản đồ.
- **Toàn bộ suite:** Run `MC-20261113-161629`, lệnh `mvn clean test`, Chrome 157 headless. 53 test: 39 PASS, 6 FAIL, 1 ERROR, 7 BLOCKED (JUnit skipped); Maven kết thúc `BUILD FAILURE` do các kết quả không đạt, không phải lỗi biên dịch.
- Sau lần chạy này, test giỏ hàng `TC-CART-001` đã bị loại khỏi catalog hiện tại. Kết quả run và ledger được giữ nguyên làm lịch sử; suite hiện tại có 52 selector.
- **NEWS & PROMOTION:** `TC_NP_01`–`TC_NP_04` đều PASS.
- **CONTACT & STORE:** `TC_CS_01`, `TC_CS_04` PASS; `TC_CS_02` FAIL do thiếu thông tin giờ làm việc; `TC_CS_03` BLOCKED do không có bộ lọc khu vực.
- **WARRANTY POLICY:** `TC_WP_01`–`TC_WP_03` PASS; `TC_WP_04` BLOCKED vì trang thực tế không có bảng để kiểm tra bố cục.
- **Các FAIL còn lại:** `TC_FS_01`–`TC_FS_03` do không có bộ lọc hãng/khoảng giá; `TC_PR_03` do giá không đổi khi đổi dung lượng; `TC_SR_03` do thiếu gợi ý tìm kiếm đủ tên, ảnh và giá. Đây là lỗi được quan sát trên website, không tính PASS.
- **ERROR:** `DM_10` hết thời gian chờ khi mở menu danh mục trên viewport điện thoại.
- **BLOCKED còn lại:** ca giỏ hàng cần thông tin đăng nhập; `TC_FS_04`, `TC_FS_05` không có đủ giá số để kiểm tra thứ tự; `TC_05` không nhận diện được banner Phụ kiện; `TC_08` thiếu hai mức giá số để xác minh sắp xếp.
- `TestCaseCatalogTest` và `RunResultServiceTest` PASS trong full suite. Chi tiết Surefire nằm trong `target/surefire-reports/`; ảnh bằng chứng lỗi nằm trong `screenshots/`.
