# Bộ Test Case Selenium: HOME và CATEGORY

## Mục đích và căn cứ

Tài liệu này hoàn thiện 16 Test Case ban đầu trong `KTPM2.docx`, chỉ trong phạm vi hai Use Case:

- **Xem trang chủ** — mục 3.3.2 và phần thiết kế Home.
- **Xem danh mục sản phẩm** — mục 3.3.3 và phần thiết kế Category.

Đây là tài liệu chi tiết riêng cho HOME/CATEGORY; danh mục tổng hợp toàn dự án nằm trong [TestCases.md](./TestCases.md), kế hoạch chung trong [TestPlan.md](./TestPlan.md). Các Test Case dưới đây được triển khai vào Selenium/JUnit 5 theo Page Object Model khi có thể xác minh an toàn. Những trường hợp cần dữ liệu chi nhánh chuẩn hoặc staging/mô phỏng mạng riêng vẫn được giữ trong đặc tả và không bị đánh dấu PASS khi thiếu điều kiện. Kết quả từng ca trong lần chạy hiện tại được ghi tại [TestExecutionReport.md](./TestExecutionReport.md).

### Cách đọc và quy ước

- Mỗi ghi chú `[HOME]` hoặc `[CATEGORY]` mô tả mục tiêu riêng và nằm ngay trước mã Test Case.
- `TC_` dành cho Home; `DM_` dành cho Category. Giữ nguyên mã của 16 trường hợp đầu; trường hợp mới được đánh số tiếp theo.
- Selenium nên dùng Page Object, `WebDriverWait`, role/tên truy cập hoặc locator được xác nhận từ DOM lúc triển khai. Tài liệu không đưa XPath/CSS selector chưa kiểm chứng.
- Với ca mạng, bảo trì, danh mục rỗng và hiệu năng, chỉ chạy trong môi trường/fixture phù hợp; không làm gián đoạn website công khai hoặc CI dùng chung.
- `AUTOMATABLE` nghĩa là các bước và xác nhận chính có thể chạy bằng Selenium; `PARTIALLY AUTOMATABLE` cần dữ liệu/môi trường chuyên biệt hoặc còn phần xác nhận thủ công; `MANUAL` không phù hợp để tự động hóa Selenium.
- Loại kiểm thử là loại chính của ca: `Positive`, `Negative` hoặc `Boundary`.

### Khảo sát giao diện hiện tại (không phải kết quả chạy Test Case)

Ngày 12-11-2026 đã mở trang MobileCity và xem giao diện/điều hướng công khai để tránh ghi thao tác không tồn tại. Đây chỉ là khảo sát khả năng và nhãn truy cập, không phải lần chạy chính thức, không đo hiệu năng và không điền kết quả thực tế cho các ca:

- Header hiện có bộ chọn khu vực với Hà Nội, TP. Hồ Chí Minh và Đà Nẵng; có liên kết Đăng nhập/Đăng ký, menu danh mục và ô tìm kiếm.
- Trang có banner/link hình ảnh, iframe mang tên trình phát YouTube, danh sách sản phẩm, liên kết hãng, bộ chọn sắp xếp giá và dòng “Bạn cần hỗ trợ?”.
- Liên kết Đăng nhập/Đăng ký hiện điều hướng tới trang tương ứng; không thấy popup đăng nhập/đăng ký như mô tả Test Case ban đầu.
- Một snapshot có link banner “Phụ kiện điện thoại”; sau khi nội dung/viewport được làm mới, một số banner chỉ có alt chung “hình ảnh banner”. Không xác nhận được nút “Mua Ngay” trên đúng banner nêu trong tài liệu.
- Rê chuột vào “Điện thoại” mở menu “Hãng sản xuất”; rê sang “Máy tính bảng” đổi danh sách menu; rê ra vùng nội dung làm menu đóng.
- Ở viewport giả lập 390×844, các mục danh mục vẫn xếp ngang và có phần tử điều khiển nằm ngoài bề rộng viewport. Khảo sát sơ bộ này **không xác nhận** menu ngăn kéo trên thiết bị di động; cần chạy ca responsive và kiểm tra trên thiết bị thật trước khi kết luận.

Các nhãn/đường dẫn này có thể thay đổi theo thời gian. Khi triển khai Selenium, kiểm tra lại DOM và nội dung công khai tại thời điểm chạy; không dựa vào thứ tự phần tử hoặc URL tự đoán.

## Bước 1 — Rà soát 16 Test Case ban đầu

Không xóa hoặc đổi mã nào. Các trường hợp còn đúng mục tiêu được giữ và làm rõ; nơi giao diện thực tế khác tài liệu, kỳ vọng được điều chỉnh hoặc giới hạn để không khẳng định chức năng chưa thấy.

| Mã | Quyết định | Lý do/điều chỉnh |
|---|---|---|
| TC_01 | Giữ, làm rõ | Tách kiểm tra trang tải và các vùng chính có căn cứ trong Use Case/thiết kế Home; không dùng nhận xét mơ hồ “không vỡ bố cục”. |
| TC_02 | Giữ, làm rõ | Chọn khu vực có trong danh sách; kiểm tra trạng thái được chọn và dữ liệu sản phẩm sau khi cập nhật. Không hard-code giá/tồn kho khi chưa có dữ liệu chuẩn theo chi nhánh. |
| TC_03 | Giữ | Điều hướng các mục Máy tính bảng, Tai nghe, Sửa chữa; xác nhận trang đích theo liên kết thực tế và nội dung tương ứng, không chỉ kiểm tra URL. |
| TC_04 | Giữ có giới hạn, sửa kỳ vọng | Đặc tả ban đầu nói popup, nhưng giao diện khảo sát hiện có link sang trang Đăng nhập/Đăng ký. Chỉ kiểm tra entry point trên Header/đích link; không kiểm thử xác thực hay đăng ký. |
| TC_05 | Giữ, điều chỉnh thao tác | Không xác nhận được nút “Mua Ngay” của banner Phụ kiện như tài liệu. Dùng link banner thực sự đang hiển thị; nếu link/banner không còn, ghi nhận cần cập nhật dữ liệu, không tự tìm selector thay thế. |
| TC_06 | Giữ, giới hạn kỳ vọng | Kiểm tra iframe YouTube hiện diện/kích thước/trạng thái lỗi quan sát được. Không kết luận chất lượng âm thanh, hình ảnh hoặc phát liên tục chỉ bằng Selenium. |
| TC_07 | Giữ, sửa mô tả | Kiểm tra lối tắt hãng trên Home và danh mục đích. Không khẳng định thao tác lọc tại chỗ khi giao diện hiện là liên kết điều hướng. |
| TC_08 | Giữ, cụ thể hóa | Xác nhận thứ tự giá bằng cách đọc giá của các thẻ sản phẩm đang hiển thị sau khi chọn từng chiều sắp xếp. |
| TC_09 | Giữ, giới hạn phạm vi | Chỉ xác nhận widget hỗ trợ mở/hiện panel hoặc đích hỗ trợ có thể quan sát; không gửi nội dung hay xác nhận nhân viên phản hồi. |
| DM_01 | Giữ, làm rõ | Xác nhận menu xuất hiện và có nhóm hãng/liên kết; bỏ yêu cầu cứng “đủ 3 cột” vì không phải tiêu chí của luồng Use Case và có thể đổi bố cục. |
| DM_02 | Giữ | Chọn iPhone Chính hãng VN/A từ menu; xác nhận điều hướng và danh mục đích, không phụ thuộc URL tự đoán. |
| DM_03 | Giữ | Chuyển từ menu Điện thoại sang Máy tính bảng; kiểm tra menu cũ đóng và nội dung menu mới xuất hiện. |
| DM_04 | Giữ | Rê chuột khỏi vùng menu và xác nhận menu biến mất. |
| DM_05 | Giữ | Nhấp tên danh mục gốc Điện thoại; xác nhận trang tổng hợp Điện thoại thay vì menu con. |
| DM_06 | Giữ, làm rõ | Dùng mục Điện thoại Cũ hiện có; xác nhận phân loại/trang đích. Không khẳng định “iPhone Cũ 99%” là danh mục máy cũ nếu nội dung thực tế không chứng minh điều đó. |
| DM_07 | Giữ | Chọn Xiaomi từ menu hãng; xác nhận trang theo hãng và nội dung/nhãn hãng. |

### Ca bổ sung

- `TC_10`: nhánh bảo trì/sự cố dữ liệu của Use Case Home.
- `TC_11`: yêu cầu responsive của Home.
- `TC_12`: ngưỡng tải nội dung chính `< 2.5 giây` của Home, đo bằng LCP và nêu giới hạn phép đo.
- `DM_08`: nhánh danh mục không có sản phẩm.
- `DM_09`: nhánh mất kết nối khi mở danh mục.
- `DM_10`: menu responsive trên viewport điện thoại; viewport Selenium không thay thế thiết bị thật.

## Bước 2 — Test Case HOME

[HOME] Kiểm tra trang chủ tải các vùng nội dung cốt lõi để khách hàng có thể xem banner, danh mục, sản phẩm nổi bật và tin tức.
### TC_01 – Tải giao diện trang chủ mặc định
- **Mục tiêu:** Xác nhận yêu cầu tải Home hiển thị được Header, nội dung/banner, video nhúng, sản phẩm và nội dung tin tức/điều hướng.
- **Tiền điều kiện:** Có kết nối mạng; website không ở trạng thái bảo trì; trình duyệt Desktop sạch phiên.
- **Dữ liệu kiểm thử:** Trang chủ MobileCity; viewport Desktop.
- **Các bước:**
  1. Mở trang chủ và chờ `document.readyState` đạt trạng thái tải xong cùng các vùng cần kiểm tra hiển thị.
  2. Kiểm tra Header, logo, bộ chọn khu vực và menu chính.
  3. Cuộn qua các vùng banner/video, sản phẩm nổi bật, liên kết tin tức/nội dung và widget hỗ trợ.
  4. Kiểm tra ít nhất một thẻ sản phẩm có tên và giá; kiểm tra các vùng được mô tả không bị che/tràn khỏi viewport tại kích thước Desktop.
- **Kết quả mong đợi:** Các vùng nêu trên hiện diện và có nội dung quan sát được; sản phẩm có tên/giá; không có trang lỗi thay cho Home. Không yêu cầu mọi ảnh/video phải phát xong trong bước tải trang.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE — kiểm tra vùng/nhãn/ảnh và chờ hiển thị; cần xác nhận locator DOM khi triển khai.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra thông tin khu vực và dữ liệu sản phẩm được cập nhật khi khách hàng đổi chi nhánh xem giá/tồn kho.
### TC_02 – Thay đổi khu vực xem giá và tồn kho
- **Mục tiêu:** Xác nhận lựa chọn khu vực được áp dụng và dữ liệu sản phẩm không còn hiển thị trạng thái cũ sau cập nhật.
- **Tiền điều kiện:** Home tải được; bộ chọn khu vực có sẵn; có dữ liệu kiểm thử đã xác nhận ít nhất một sản phẩm có thông tin khác nhau giữa hai chi nhánh.
- **Dữ liệu kiểm thử:** Hà Nội (mặc định) → TP. Hồ Chí Minh; sản phẩm và giá/tồn kho kỳ vọng phải lấy từ dữ liệu kiểm thử đã xác nhận tại thời điểm chạy.
- **Các bước:**
  1. Ghi lại khu vực đang chọn cùng giá/trạng thái tồn kho của sản phẩm kiểm thử.
  2. Chọn “TP. Hồ Chí Minh” từ bộ chọn khu vực.
  3. Dùng `WebDriverWait` chờ lựa chọn mới được giữ và vùng sản phẩm tải/cập nhật xong.
  4. Đối chiếu trạng thái hiển thị của sản phẩm với dữ liệu kiểm thử đã xác nhận cho TP. Hồ Chí Minh.
- **Kết quả mong đợi:** Bộ chọn thể hiện TP. Hồ Chí Minh; dữ liệu sản phẩm khớp dữ liệu chi nhánh đã xác nhận, không còn giá/tồn kho cũ. Không suy diễn rằng giá phải khác nếu dữ liệu chuẩn cho hai chi nhánh giống nhau.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — thao tác và so sánh tự động được, nhưng cần nguồn dữ liệu khu vực chuẩn, ổn định; nếu không có dữ liệu đó, không đánh dấu PASS chỉ vì select đã đổi.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra các liên kết danh mục chính đưa khách hàng đến đúng trang tương ứng từ Header trang chủ.
### TC_03 – Điều hướng thanh menu danh mục chính
- **Mục tiêu:** Xác nhận các mục menu chính nêu trong Test Case ban đầu dẫn tới trang danh mục/sản phẩm phù hợp.
- **Tiền điều kiện:** Home tải được; các liên kết kiểm thử đang hiển thị.
- **Dữ liệu kiểm thử:** Máy tính bảng, Tai nghe, Sửa chữa.
- **Các bước:**
  1. Với từng tên trong dữ liệu, quay lại Home nếu cần và tìm liên kết hiển thị theo tên truy cập.
  2. Ghi nhận `href` thực tế từ liên kết; nhấp liên kết.
  3. Chờ trang đích tải và xác nhận tên/breadcrumb/nội dung thể hiện đúng danh mục đã chọn.
  4. Không kiểm tra mã HTTP nếu không có cơ chế bắt response riêng.
- **Kết quả mong đợi:** Mỗi lựa chọn mở đúng trang theo liên kết thực tế và nội dung trang đích phù hợp; không chuyển sang trang lỗi hoặc danh mục khác.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra Header cung cấp đúng lối vào Đăng nhập và Đăng ký mà không giả định popup hoặc thực hiện xác thực.
### TC_04 – Liên kết Đăng nhập/Đăng ký từ Header
- **Mục tiêu:** Xác nhận hai entry point trên Header được hiển thị và điều hướng đến đúng đích liên kết hiện hành.
- **Tiền điều kiện:** Home tải được; chưa đăng nhập.
- **Dữ liệu kiểm thử:** Liên kết có tên “Đăng nhập” và “Đăng ký”.
- **Các bước:**
  1. Trên Home, tìm từng liên kết bằng tên truy cập hiển thị và lưu `href` từ DOM.
  2. Nhấp liên kết; chờ trang mới tải.
  3. Xác nhận URL hiện tại khớp đích liên kết đã ghi nhận và trang không phải trang lỗi.
  4. Trở lại Home trước khi kiểm tra liên kết còn lại; không nhập hoặc gửi thông tin tài khoản.
- **Kết quả mong đợi:** Cả hai entry point đều hiển thị và mở trang tương ứng; không yêu cầu popup vì giao diện khảo sát hiện điều hướng trực tiếp.
- **Mức độ ưu tiên:** Trung bình.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra liên kết banner đang hiển thị mở đúng trang nội dung đích, không phụ thuộc nút hoặc selector chưa xác minh.
### TC_05 – Điều hướng từ banner trang chủ
- **Mục tiêu:** Xác nhận banner/ảnh khuyến mãi có thể điều hướng khách hàng đến nội dung liên quan.
- **Tiền điều kiện:** Home tải được; có banner mục tiêu nhận diện được bằng nội dung/alt đã xác nhận và có đích phù hợp trong dữ liệu kiểm thử.
- **Dữ liệu kiểm thử:** Banner Phụ kiện điện thoại theo tài liệu; trước khi chạy, xác nhận nhãn/ảnh và đích thực tế từ trang hiện hành. Không suy ra chiến dịch từ alt chung “hình ảnh banner”.
- **Các bước:**
  1. Tìm banner mục tiêu bằng tên truy cập/alt hoặc nội dung ảnh đã xác nhận trước khi chạy.
  2. Ghi nhận `href` thực tế và đối chiếu với đích được duyệt trong dữ liệu kiểm thử; nếu banner chỉ có nhãn chung và không thể phân biệt, dừng ca ở trạng thái BLOCKED/Not Run.
  3. Nhấp banner đã xác định và chờ điều hướng.
  4. Xác nhận tiêu đề/nội dung trang đích khớp nội dung banner.
- **Kết quả mong đợi:** Banner/link đang hiển thị có đích phù hợp với nội dung quảng bá; không yêu cầu nút “Mua Ngay” nếu nút đó không có trên banner thực tế.
- **Mức độ ưu tiên:** Trung bình.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — chỉ tự động được khi banner/đích được nhận diện ổn định; carousel động hoặc alt chung cần xác nhận thủ công/dữ liệu kiểm thử riêng.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra iframe YouTube được nhúng và có thể quan sát trạng thái tải/lỗi mà không kết luận chất lượng âm thanh.
### TC_06 – Hiển thị và tải khung video YouTube
- **Mục tiêu:** Xác nhận vùng video nhúng tồn tại, có kích thước hiển thị và không hiện lỗi tải quan sát được.
- **Tiền điều kiện:** Home tải được; trình duyệt cho phép tải nội dung nhúng của YouTube.
- **Dữ liệu kiểm thử:** Iframe/trình phát có tên truy cập “YouTube Video Player” nếu tên này còn đúng trên DOM hiện hành.
- **Các bước:**
  1. Cuộn đến vùng video và chờ iframe xuất hiện.
  2. Xác nhận iframe có kích thước hiển thị khác 0 và không bị lớp phủ che toàn bộ.
  3. Nếu thông tin lỗi của player có thể quan sát qua accessibility/UI, kiểm tra không có trạng thái “Video unavailable” hoặc lỗi tương đương.
  4. Không tự động đánh giá âm lượng, chất lượng hình ảnh, nội dung video hay phát liên tục.
- **Kết quả mong đợi:** Iframe hiện diện, có vùng hiển thị và không có thông báo lỗi tải quan sát được; phần kiểm tra này không chứng minh video có âm thanh/chất lượng tốt.
- **Mức độ ưu tiên:** Trung bình.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — có thể kiểm tra iframe/kích thước/trạng thái bề mặt; kiểm tra phát thực, âm thanh và chất lượng cần công cụ hoặc thao tác thủ công phù hợp.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra lối tắt Samsung Chính hãng trên Home dẫn đến danh mục đúng thay vì giả định lọc tại chỗ.
### TC_07 – Mở danh mục từ lối tắt hãng trên Home
- **Mục tiêu:** Xác nhận lối tắt hãng trên Home đưa khách hàng đến danh mục hãng và sản phẩm phù hợp.
- **Tiền điều kiện:** Home tải được; liên kết Samsung Chính hãng đang hiển thị; danh mục có dữ liệu kiểm thử hoặc trạng thái rỗng được định nghĩa.
- **Dữ liệu kiểm thử:** “Samsung Chính hãng”.
- **Các bước:**
  1. Tìm liên kết hãng trên vùng Hãng của Home và ghi nhận `href` thực tế.
  2. Nhấp liên kết, chờ trang đích tải.
  3. Kiểm tra tiêu đề/breadcrumb nhận diện Samsung Chính hãng.
  4. Nếu có thẻ sản phẩm, xác nhận tên/nhãn hãng phù hợp; nếu không có sản phẩm thì kiểm tra trạng thái rỗng theo DM_08.
- **Kết quả mong đợi:** Mở đúng danh mục hãng; không yêu cầu các thẻ sản phẩm Home tự lọc tại chỗ nếu giao diện hiện điều hướng bằng link.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra danh sách sản phẩm đổi đúng thứ tự khi chọn giá tăng dần và giảm dần.
### TC_08 – Sắp xếp sản phẩm theo giá trên Home
- **Mục tiêu:** Xác nhận hai chiều sắp xếp giá tác động đúng đến danh sách Điện thoại mở từ điều khiển sắp xếp trên Home.
- **Tiền điều kiện:** Home tải được; bộ chọn sắp xếp giá trên Home và danh sách Điện thoại đều có thể tương tác.
- **Dữ liệu kiểm thử:** “Giá từ thấp đến cao”, “Giá từ cao đến thấp”.
- **Các bước:**
  1. Trên Home, chọn “Giá từ thấp đến cao”; xác nhận trang Điện thoại mở và chờ danh sách cập nhật.
  2. Đọc giá số của sản phẩm theo thứ tự DOM và xác nhận dãy không giảm.
  3. Mở “Sắp xếp theo giá” trên trang Điện thoại và chọn “Giá cao đến thấp”.
  4. Chờ danh sách sản phẩm cập nhật; đọc lại giá số và xác nhận dãy không tăng.
  5. Bỏ qua ký hiệu tiền tệ/dấu phân tách khi chuyển chuỗi giá thành số; không dùng tên sản phẩm làm đại diện cho giá.
- **Kết quả mong đợi:** Dãy giá số quan sát được tăng dần rồi giảm dần tương ứng với lựa chọn. Nếu danh sách sau thao tác chỉ cung cấp “Liên hệ” thay vì ít nhất hai giá số, ca được ghi BLOCKED, không tính PASS.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — thao tác và so sánh tự động được khi danh sách cung cấp giá số; dữ liệu “Liên hệ” không thể so sánh bằng số.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra widget hỗ trợ có thể mở nội dung/đích liên hệ mà không gửi tin nhắn hoặc khẳng định có nhân viên phản hồi.
### TC_09 – Mở tiện ích hỗ trợ khách hàng
- **Mục tiêu:** Xác nhận widget “Bạn cần hỗ trợ?” có thể được khách hàng kích hoạt và hiển thị lựa chọn hỗ trợ.
- **Tiền điều kiện:** Home tải được; widget của nhà cung cấp chat không bị chặn bởi môi trường.
- **Dữ liệu kiểm thử:** Widget/nhãn “Bạn cần hỗ trợ?”.
- **Các bước:**
  1. Chờ widget hiển thị ở viewport; nếu cần, cuộn tới vị trí widget.
  2. Kích hoạt widget bằng thao tác người dùng.
  3. Xác nhận panel chat hoặc link/kênh hỗ trợ được mở và nhìn thấy.
  4. Đóng panel nếu có thể; không gửi tin nhắn, không gọi điện, không tạo yêu cầu hỗ trợ.
- **Kết quả mong đợi:** Widget phản hồi bằng panel/đích hỗ trợ quan sát được; không bị che khuất hoàn toàn hoặc không phản hồi.
- **Mức độ ưu tiên:** Trung bình.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — mở panel thường tự động được; nội dung cross-origin, dịch vụ bên thứ ba và chất lượng hỗ trợ cần xác nhận riêng.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra thông báo bảo trì hoặc lỗi máy chủ trong môi trường kiểm thử có thể kiểm soát, không gây lỗi cho website công khai.
### TC_10 – Hiển thị nhánh bảo trì/sự cố tải dữ liệu
- **Mục tiêu:** Bao phủ luồng rẽ nhánh Home khi máy chủ lỗi kết nối cơ sở dữ liệu hoặc đang bảo trì.
- **Tiền điều kiện:** Có staging/fixture được phép mô phỏng trạng thái bảo trì hoặc lỗi dữ liệu; không chạy bằng cách làm hỏng kết nối website công khai.
- **Dữ liệu kiểm thử:** Trạng thái bảo trì/lỗi được cấu hình trong môi trường kiểm thử; thông báo theo đặc tả: “Website đang được nâng cấp, vui lòng quay lại sau ít phút” hoặc màn hình lỗi đã được phê duyệt.
- **Các bước:**
  1. Mở Home trong môi trường đã cấu hình trạng thái bảo trì/lỗi.
  2. Chờ trang phản hồi hoặc đạt timeout được cấu hình.
  3. Kiểm tra thông báo lỗi/bảo trì được hiển thị.
  4. Xác nhận không hiển thị dữ liệu sản phẩm cũ như thể tải thành công nếu đặc tả yêu cầu dừng tiến trình tải dữ liệu.
- **Kết quả mong đợi:** Hiển thị thông báo lỗi/bảo trì có thể nhận biết; không trả về giao diện thành công giả hoặc dữ liệu không còn hợp lệ.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Negative.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — cần môi trường/fixture kiểm soát lỗi; nếu không có, ghi BLOCKED/Not Run, không suy diễn từ việc website công khai đang hoạt động.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Kiểm tra bố cục Home thích ứng ở màn Desktop và viewport điện thoại giả lập, đồng thời không nhầm viewport với thiết bị thật.
### TC_11 – Hiển thị trang chủ theo kích thước viewport
- **Mục tiêu:** Xác nhận nội dung chính Home vẫn truy cập/đọc được khi đổi kích thước cửa sổ.
- **Tiền điều kiện:** Home tải được; trình duyệt Selenium cho phép đổi viewport.
- **Dữ liệu kiểm thử:** Desktop 1366×768; viewport 390×844.
- **Các bước:**
  1. Mở Home tại kích thước Desktop; kiểm tra Header, banner, danh sách sản phẩm và các điều khiển chính không bị cắt.
  2. Đổi viewport sang 390×844 và chờ bố cục cập nhật.
  3. Kiểm tra không có thành phần cốt lõi bị tràn ngang/che khuất; xác nhận menu có lối mở phù hợp với giao diện di động nếu được thiết kế.
  4. Có thể cuộn dọc để kiểm tra banner/video/sản phẩm vẫn tiếp cận được.
- **Kết quả mong đợi:** Bố cục sử dụng được ở cả hai kích thước, các nội dung chính không bị mất hoặc nằm ngoài viewport theo cách cản trở thao tác. Kết quả viewport không được ghi là kiểm thử trên điện thoại thật.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE — kiểm tra responsive ở viewport trình duyệt; cần kiểm thử thủ công bổ sung trên thiết bị thật để xác nhận hành vi cảm ứng/hiển thị thực.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[HOME] Đo Largest Contentful Paint trong điều kiện mạng/trình duyệt được ghi nhận và đối chiếu ngưỡng nghiêm ngặt dưới 2.5 giây.
### TC_12 – Đo thời gian hiển thị nội dung chính Home
- **Mục tiêu:** Đối chiếu yêu cầu thời gian tải nội dung chính của Home dưới 2.5 giây.
- **Tiền điều kiện:** Môi trường đo có cấu hình trình duyệt, mạng và cache được cố định/ghi lại; có thể thu thập LCP trước khi điều hướng.
- **Dữ liệu kiểm thử:** 5 lần điều hướng độc lập với cùng cấu hình; ngưỡng LCP `< 2500 ms`.
- **Các bước:**
  1. Khởi tạo phép thu thập `largest-contentful-paint` trước mỗi lần điều hướng (ví dụ qua instrumentation của trình duyệt), không chỉ đo thời gian chờ của WebDriver.
  2. Mở Home và ghi lại LCP, trạng thái cache, mạng, thời gian UTC và trình duyệt cho từng lần hợp lệ.
  3. Lặp lại đủ 5 lần trong cấu hình đã định; không trộn kết quả từ mạng hoặc cấu hình cache khác nhau.
  4. Báo cáo từng mẫu; phép đo không thay thế kiểm thử tải trang đầy đủ hoặc đánh giá trên thiết bị thật.
- **Kết quả mong đợi:** Mỗi một trong 5 mẫu hợp lệ có LCP nhỏ hơn 2500 ms; mẫu đúng 2500 ms không đạt điều kiện “dưới 2.5 giây”. Nếu không thu được LCP thì phép đo không hợp lệ, không xem là PASS.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Boundary.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — thu thập LCP có thể tự động hóa bằng instrumentation; kết quả phụ thuộc mạng, cache, máy chạy và nội dung động nên phải lưu cấu hình/bằng chứng, không dùng `Thread.sleep` hoặc riêng thời gian `driver.get()` làm đại diện.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

## Bước 3 — Test Case CATEGORY

[CATEGORY] Kiểm tra menu con của Điện thoại xuất hiện khi rê chuột và chứa các liên kết hãng/phân loại có thể chọn.
### DM_01 – Hiển thị menu danh mục khi rê chuột
- **Mục tiêu:** Xác nhận menu đa cấp được mở từ mục Điện thoại trên giao diện Desktop.
- **Tiền điều kiện:** Website tải được; viewport Desktop; menu chính hiển thị.
- **Dữ liệu kiểm thử:** Mục “Điện thoại”; nhãn menu “Hãng sản xuất”.
- **Các bước:**
  1. Dùng thao tác hover Selenium lên liên kết “Điện thoại” trong Header.
  2. Dùng `WebDriverWait` chờ menu con hiển thị.
  3. Kiểm tra nhãn “Hãng sản xuất” và một số link hãng/phân loại hiện có như iPhone Chính hãng VN/A hoặc Xiaomi.
  4. Không khẳng định số cột cố định; kiểm tra nội dung và khả năng nhìn/chọn được.
- **Kết quả mong đợi:** Menu xuất hiện dưới mục đã rê chuột; có tiêu đề/nhóm link phù hợp và link có thể thao tác.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[CATEGORY] Kiểm tra link iPhone Chính hãng VN/A trong menu mở đúng danh mục và nội dung đích tương ứng.
### DM_02 – Điều hướng đến danh mục iPhone Chính hãng VN/A
- **Mục tiêu:** Xác nhận lựa chọn hãng/phân loại trong menu điều hướng đến danh mục đã chọn.
- **Tiền điều kiện:** Menu Điện thoại mở được; link iPhone Chính hãng VN/A hiển thị.
- **Dữ liệu kiểm thử:** “iPhone Chính hãng VN/A”.
- **Các bước:**
  1. Rê chuột vào “Điện thoại” để mở menu.
  2. Ghi nhận `href` thật của link iPhone Chính hãng VN/A.
  3. Nhấp link và chờ trang đích tải.
  4. Kiểm tra tiêu đề/breadcrumb/danh mục nhận diện lựa chọn; nếu có sản phẩm, xác nhận sản phẩm thuộc danh mục.
- **Kết quả mong đợi:** Trình duyệt mở đúng đích của liên kết và nội dung trang thể hiện iPhone Chính hãng VN/A; không rơi vào trang lỗi.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[CATEGORY] Kiểm tra menu Điện thoại đóng và menu Máy tính bảng thay thế khi khách hàng chuyển con trỏ giữa hai mục.
### DM_03 – Chuyển đổi giữa menu danh mục chính
- **Mục tiêu:** Xác nhận trạng thái menu không bị giữ/đè lên nhau khi chuyển giữa hai danh mục chính.
- **Tiền điều kiện:** Website tải được; viewport Desktop; menu Điện thoại có thể mở.
- **Dữ liệu kiểm thử:** “Điện thoại” → “Máy tính bảng”.
- **Các bước:**
  1. Hover Điện thoại; chờ menu con và ghi nhận một link đặc trưng đang hiện.
  2. Hover Máy tính bảng; chờ menu mới.
  3. Kiểm tra link riêng của menu mới (ví dụ iPad/Samsung Galaxy Tab nếu còn hiển thị) và xác nhận link đặc trưng của menu Điện thoại không còn thuộc menu đang mở.
- **Kết quả mong đợi:** Menu Máy tính bảng thay thế menu Điện thoại; không hiển thị chồng lấp hoặc nội dung cũ như menu hiện hành.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[CATEGORY] Kiểm tra menu danh mục được ẩn khi con trỏ rời cả mục gốc và vùng menu con.
### DM_04 – Tự động đóng menu khi rê chuột ra ngoài
- **Mục tiêu:** Xác nhận menu không còn mở sau khi khách hàng chuyển tới vùng nội dung bên ngoài.
- **Tiền điều kiện:** Website tải được; menu Điện thoại đang có thể mở.
- **Dữ liệu kiểm thử:** Mục gốc Điện thoại, vùng menu con và vùng nội dung Home bên ngoài menu.
- **Các bước:**
  1. Hover Điện thoại và chờ menu con hiện.
  2. Di chuyển con trỏ tới vùng nội dung nằm ngoài menu.
  3. Chờ trạng thái ẩn bằng `WebDriverWait`; không dùng `Thread.sleep`.
- **Kết quả mong đợi:** Menu con không còn hiển thị/tương tác được; nội dung trang bên dưới có thể tiếp cận.
- **Mức độ ưu tiên:** Trung bình.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[CATEGORY] Kiểm tra nhấp tên danh mục gốc mở trang tổng hợp thay vì chỉ mở menu con.
### DM_05 – Nhấp trực tiếp vào danh mục gốc Điện thoại
- **Mục tiêu:** Xác nhận liên kết danh mục gốc dẫn đến trang danh sách Điện thoại.
- **Tiền điều kiện:** Header hiển thị mục Điện thoại; website truy cập được.
- **Dữ liệu kiểm thử:** Liên kết gốc “Điện thoại”.
- **Các bước:**
  1. Xác định link gốc theo tên truy cập trong Header, không dùng link Điện thoại nằm trong banner/carousel.
  2. Ghi nhận `href` thực tế; nhấp link.
  3. Chờ trang đích và xác nhận tiêu đề/breadcrumb thể hiện danh mục Điện thoại.
- **Kết quả mong đợi:** Trang tổng hợp Điện thoại được mở; đích không phải một hãng hoặc dòng máy con khác.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[CATEGORY] Kiểm tra liên kết phân loại Điện thoại Cũ mở trang danh sách đúng phân loại hàng đã qua sử dụng.
### DM_06 – Điều hướng đến danh mục điện thoại cũ
- **Mục tiêu:** Xác nhận phân loại máy cũ trong menu điều hướng đúng và trang đích nhận diện đúng phân loại.
- **Tiền điều kiện:** Menu Điện thoại mở được; link “Điện thoại Cũ” hiện diện.
- **Dữ liệu kiểm thử:** “Điện thoại Cũ”.
- **Các bước:**
  1. Mở menu Điện thoại.
  2. Chọn link Điện thoại Cũ đang hiển thị.
  3. Chờ trang đích; kiểm tra tiêu đề/breadcrumb/nhãn phân loại và sản phẩm nếu có.
- **Kết quả mong đợi:** Trang thuộc phân loại điện thoại cũ được mở; nếu không có sản phẩm thì áp dụng nhánh DM_08, không thay bằng danh mục Apple chỉ vì tên link gần giống.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[CATEGORY] Kiểm tra chọn Xiaomi trong menu hãng mở danh mục Xiaomi và chỉ hiển thị nội dung phù hợp với thương hiệu.
### DM_07 – Điều hướng đến thương hiệu Android Xiaomi
- **Mục tiêu:** Xác nhận điều hướng theo hãng sản xuất từ menu đa cấp.
- **Tiền điều kiện:** Menu Điện thoại mở được; link Xiaomi hiện diện; dữ liệu trang đích tải được hoặc có trạng thái rỗng chuẩn.
- **Dữ liệu kiểm thử:** “Xiaomi”.
- **Các bước:**
  1. Mở menu Điện thoại.
  2. Chọn link Xiaomi.
  3. Chờ trang đích tải; kiểm tra nhãn/tiêu đề/breadcrumb Xiaomi.
  4. Nếu danh sách sản phẩm có dữ liệu, kiểm tra tên/nhãn sản phẩm phù hợp với Xiaomi.
- **Kết quả mong đợi:** Trang đích thể hiện đúng hãng Xiaomi; không hiển thị nhầm danh mục của hãng khác.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** AUTOMATABLE.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[CATEGORY] Kiểm tra nhánh danh mục rỗng có thông báo rõ ràng thay vì để trang trắng hoặc báo tải thành công giả.
### DM_08 – Danh mục không có sản phẩm
- **Mục tiêu:** Bao phủ nhánh hết hàng/ngừng kinh doanh trong Use Case Category.
- **Tiền điều kiện:** Có staging/fixture chứa một danh mục đã xác nhận không có sản phẩm; không tự đoán danh mục rỗng trên website công khai.
- **Dữ liệu kiểm thử:** Link danh mục rỗng được cấu hình trong fixture; không hard-code link không được xác minh.
- **Các bước:**
  1. Mở menu và chọn danh mục rỗng đã cấu hình.
  2. Chờ trang danh mục tải xong.
  3. Kiểm tra không có thẻ sản phẩm.
  4. Kiểm tra thông báo rỗng là “Hiện tại chưa có sản phẩm nào thuộc danh mục này” hoặc “Sản phẩm đang được cập nhật” theo nội dung được đặc tả/duyệt.
- **Kết quả mong đợi:** Trang vẫn điều hướng đến danh mục; trạng thái không có sản phẩm được thông báo rõ; không để vùng nội dung trống không giải thích.
- **Mức độ ưu tiên:** Trung bình.
- **Loại kiểm thử:** Negative.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — tự động được khi có fixture danh mục rỗng ổn định; nếu không có thì ghi BLOCKED/Not Run.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[CATEGORY] Kiểm tra thông báo lỗi khi yêu cầu trang danh mục bị ngắt mạng trong môi trường mô phỏng cô lập.
### DM_09 – Mất kết nối khi truy cập danh mục
- **Mục tiêu:** Bao phủ nhánh mất mạng xảy ra trong lúc khách hàng chọn một mục menu.
- **Tiền điều kiện:** Có môi trường kiểm thử cô lập hỗ trợ mô phỏng/chặn request danh mục; không tắt mạng máy CI hoặc ảnh hưởng các tiến trình khác.
- **Dữ liệu kiểm thử:** Một link danh mục đã xác nhận; quy tắc mô phỏng lỗi chỉ áp dụng cho request kiểm thử đó.
- **Các bước:**
  1. Mở trang có menu danh mục và xác nhận mạng ban đầu hoạt động.
  2. Cấu hình môi trường cô lập để request điều hướng danh mục bị ngắt/lỗi.
  3. Nhấp link danh mục và chờ phản hồi lỗi.
  4. Kiểm tra thông báo kết nối/trang lỗi có thể quan sát; khôi phục cấu hình mạng ngay sau ca kiểm thử.
- **Kết quả mong đợi:** Người dùng nhận biết được lỗi kết nối/tải trang; không hiển thị danh sách cũ như thể đã chuyển trang thành công.
- **Mức độ ưu tiên:** Trung bình.
- **Loại kiểm thử:** Negative.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — cần proxy/CDP/fixture cô lập và xác nhận cách trình duyệt hiển thị lỗi; không chạy mô phỏng mạng trên website công khai/CI dùng chung.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

[CATEGORY] Kiểm tra menu danh mục có thể mở bằng thao tác chạm ở viewport điện thoại và liên kết con vẫn sử dụng được.
### DM_10 – Menu danh mục trên viewport điện thoại
- **Mục tiêu:** Bao phủ yêu cầu menu Category thu gọn dạng ngăn kéo trên giao diện di động.
- **Tiền điều kiện:** Trang truy cập được; trình duyệt hỗ trợ đổi viewport; xác định trước thiết kế responsive mong đợi từ giao diện đã duyệt.
- **Dữ liệu kiểm thử:** Viewport 390×844; danh mục Điện thoại; một link con hãng/phân loại.
- **Các bước:**
  1. Đặt viewport 390×844 và tải lại trang.
  2. Kiểm tra điều khiển mở menu nằm trong viewport và có thể thao tác; không dùng hover làm thay thế cho chạm.
  3. Mở menu; xác nhận nội dung danh mục hiển thị/ cuộn được, không bị cắt khỏi viewport.
  4. Chọn link con và xác nhận điều hướng đến danh mục tương ứng.
- **Kết quả mong đợi:** Menu có thể mở bằng tương tác phù hợp với viewport di động, link con đọc/chọn được và trang đích đúng; không tràn ngang cản trở thao tác.
- **Mức độ ưu tiên:** Cao.
- **Loại kiểm thử:** Positive.
- **Khả năng tự động hóa:** PARTIALLY AUTOMATABLE — Selenium có thể kiểm tra viewport và thao tác chuột giả lập; cần kiểm tra thủ công trên điện thoại thật để xác nhận cảm ứng, bàn phím và kích thước thực. Khảo sát sơ bộ 390×844 nêu ở trên chưa xác nhận drawer, nên chưa thể coi ca này đạt.
- **Kết quả thực tế:** Xem trạng thái theo mã tại [TestExecutionReport.md](./TestExecutionReport.md).

## Bước 4 — Kiểm tra ghi chú và mã

- Mỗi mã có đúng một dòng `[HOME]`/`[CATEGORY]` ngay phía trên.
- Home dùng liên tục `TC_01`–`TC_12`; Category dùng liên tục `DM_01`–`DM_10`; không trùng mã.
- 16 mã ban đầu được giữ nguyên; 6 ca mới nối tiếp mã cuối của từng chức năng.

## Bước 5 — Khả năng tự động hóa Selenium

| Chức năng | AUTOMATABLE | PARTIALLY AUTOMATABLE | MANUAL | Tổng |
|---|---:|---:|---:|---:|
| HOME | 5 (TC_01, TC_03, TC_04, TC_07, TC_11) | 7 (TC_02, TC_05, TC_06, TC_08, TC_09, TC_10, TC_12) | 0 | 12 |
| CATEGORY | 7 (DM_01–DM_07) | 3 (DM_08–DM_10) | 0 | 10 |
| **Tổng** | **12** | **10** | **0** | **22** |

`MANUAL = 0` không có nghĩa mọi khía cạnh đều tự động được: TC_06 không xác nhận âm thanh/chất lượng phát; TC_11/DM_10 không thay thế thiết bị thật; TC_02, TC_09, TC_10, TC_12 và DM_08/DM_09 phụ thuộc dữ liệu/dịch vụ hoặc môi trường chuyên biệt. Các phần đó được ghi trong `PARTIALLY AUTOMATABLE`, không được tính là đã xác minh chỉ bằng Selenium.

## Bước 6 — Đối chiếu độ bao phủ Use Case

| Đặc tả/nhánh | Test Case bao phủ | Ghi chú |
|---|---|---|
| Home — truy cập và hiển thị giao diện chính | TC_01 | Header, banner/video, sản phẩm và nội dung tin tức/điều hướng. |
| Home — xem banner, duyệt danh mục, sản phẩm | TC_01, TC_03, TC_05, TC_07, TC_08 | TC_05 kiểm tra đích link banner thực tế; TC_07 kiểm tra lối tắt hãng, không khẳng định lọc tại chỗ. |
| Home — khu vực/giá/tồn kho và Header | TC_02, TC_04 | TC_04 giới hạn ở lối vào Header, không chạy đăng nhập/đăng ký. |
| Home — iframe video và hỗ trợ | TC_06, TC_09 | Không đánh giá âm thanh hoặc chất lượng chăm sóc khách hàng. |
| Home — lỗi máy chủ/cơ sở dữ liệu hoặc bảo trì | TC_10 | Chỉ thực hiện với staging/fixture an toàn. |
| Home — responsive | TC_11 | Kiểm tra viewport; thiết bị thật cần kiểm tra riêng. |
| Home — nội dung chính dưới 2.5 giây | TC_12 | Đo LCP, không dùng riêng thời gian `driver.get()`. |
| Category — mở menu chính/menu đa cấp | DM_01, DM_03 | Desktop hover; chuyển đổi menu không chồng lấp. |
| Category — chọn hãng/phân loại và điều hướng | DM_02, DM_05, DM_06, DM_07 | Bao gồm hãng chính hãng, danh mục gốc, máy cũ và Xiaomi. |
| Category — tự đóng menu | DM_04 | Rời vùng menu và chờ menu ẩn. |
| Category — danh mục hết hàng/không còn kinh doanh | DM_08 | Kiểm tra thông báo rỗng trên fixture đã chuẩn bị. |
| Category — mất mạng trong lúc mở danh mục | DM_09 | Cô lập mô phỏng mạng, không làm ảnh hưởng CI. |
| Category — responsive/menu ngăn kéo | DM_10 | So sánh viewport giả lập; cần kiểm tra thêm trên thiết bị thật. |

## Bước 7 — Đối chiếu mã Test Case với Selenium

| Mã | Lớp/phương thức Selenium | Trạng thái triển khai |
|---|---|---|
| TC_01 | `HomeTest.testTC01_LoadHomePage` | Đã triển khai |
| TC_02 | — | Chưa triển khai: cần bộ dữ liệu chuẩn giá/tồn kho theo chi nhánh |
| TC_03 | `HomeTest.testTC03_NavigateMainCategories` | Đã triển khai |
| TC_04 | `HomeTest.testTC04_HeaderAuthenticationLinks` | Đã triển khai |
| TC_05 | `HomeTest.testTC05_AccessoryBannerNavigation` | Đã triển khai; cần banner nhận diện được |
| TC_06 | `HomeTest.testTC06_EmbeddedYouTubeFrameIsVisible` | Đã triển khai kiểm tra iframe/khung; không kiểm tra âm thanh |
| TC_07 | `HomeTest.testTC07_OpenSamsungOfficialCategory` | Đã triển khai điều hướng link hãng đang hiển thị |
| TC_08 | `HomeTest.testTC08_SortProductsByPrice` | Đã triển khai; chiều giảm dần bị BLOCKED khi sản phẩm chỉ hiển thị “Liên hệ” |
| TC_09 | — | Chưa triển khai thao tác mở chat: tương tác widget bên thứ ba chưa xác minh ổn định |
| TC_10 | — | Chưa triển khai: cần staging/fixture bảo trì được cho phép |
| TC_11 | `HomeTest.testTC11_HomePageResponsiveViewport` | Đã triển khai kiểm tra viewport, không thay thế thiết bị thật |
| TC_12 | `HomeTest.testTC12_HomeLargestContentfulPaint` | Đã triển khai đo LCP 5 lần; kết quả phụ thuộc môi trường |
| DM_01 | `CategoryTest.testDM01_HoverOpensCategoryMenu` | Đã triển khai |
| DM_02 | `CategoryTest.testDM02_OpenOfficialIPhoneCategory` | Đã triển khai |
| DM_03 | `CategoryTest.testDM03_SwitchMainCategoryMenus` | Đã triển khai |
| DM_04 | `CategoryTest.testDM04_CategoryMenuClosesOutside` | Đã triển khai |
| DM_05 | `HomeTest.testDM05_OpenPhoneCategoryRoot` | Đã triển khai; giữ xác minh tên/giá sản phẩm của test cũ |
| DM_06 | `CategoryTest.testDM06_OpenUsedPhoneCategory` | Đã triển khai |
| DM_07 | `CategoryTest.testDM07_OpenXiaomiCategory` | Đã triển khai |
| DM_08 | — | Chưa triển khai: cần fixture danh mục rỗng đã xác nhận |
| DM_09 | — | Chưa triển khai: cần mô phỏng mạng cô lập, không gây ảnh hưởng CI |
| DM_10 | `CategoryTest.testDM10_CategoryMenuOnMobileViewport` | Đã triển khai viewport giả lập; lần chạy hiện tại phát hiện menu không mở bằng thao tác nhấp |

Các ca chưa triển khai được tính là `NOT EXECUTED`/`BLOCKED` trong báo cáo, không có phương thức Selenium giả lập kết quả thành công.

## Tổng hợp số lượng

| Chức năng | Tổng Test Case | Positive | Negative | Boundary | AUTOMATABLE | PARTIALLY AUTOMATABLE | MANUAL |
|---|---:|---:|---:|---:|---:|---:|---:|
| HOME | 12 | 10 | 1 | 1 | 5 | 7 | 0 |
| CATEGORY | 10 | 8 | 2 | 0 | 7 | 3 | 0 |
| **Tổng** | **22** | **18** | **3** | **1** | **12** | **10** | **0** |
