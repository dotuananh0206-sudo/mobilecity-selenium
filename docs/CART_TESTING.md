# Kiểm thử giỏ hàng

`CartTest` là kiểm thử bổ sung ngoài tám module chức năng chính. Test mở giỏ sau đăng nhập, thay đổi tạm thời số lượng mặt hàng đầu tiên, tải lại để xác minh và thử khôi phục giá trị ban đầu. Test không checkout hoặc đặt hàng. Chạy chỉ với tài khoản thử nghiệm được phép sử dụng.

## Chạy an toàn trong PowerShell

Đặt credentials trong biến môi trường trong thời gian chạy. Đoạn lệnh dưới đây hỏi username và nhập mật khẩu ở dạng ẩn; mật khẩu không được ghi vào câu lệnh Maven:

```powershell
$env:MOBILECITY_CART_CHROME_PROFILE_DIR = ".runtime/mobilecity-cart-profile"
$env:MOBILECITY_USERNAME = Read-Host "Tên đăng nhập / email / số điện thoại"
$securePassword = Read-Host "Mật khẩu" -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
    $env:MOBILECITY_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    mvn "-Dtest=CartTest" "-Dmobilecity.headless=false" test
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    Remove-Item Env:MOBILECITY_USERNAME -ErrorAction SilentlyContinue
    Remove-Item Env:MOBILECITY_PASSWORD -ErrorAction SilentlyContinue
    $securePassword.Dispose()
}
```

Nếu thiếu credentials, hoặc website yêu cầu CAPTCHA/OTP, test không vượt cơ chế đó và được ghi `BLOCKED`/skipped. Xác minh trạng thái và log sau khi chạy.

## Chrome profile riêng

Để giữ phiên đăng nhập cho lần chạy sau, dùng một thư mục riêng như `.runtime/mobilecity-cart-profile`, không dùng profile Chrome cá nhân. Profile chứa cookie và phiên xác thực:

- Không chia sẻ hoặc commit profile.
- Không chạy đồng thời nhiều phiên Chrome/test trên cùng profile.
- Xóa phiên hoặc profile khi không cần nữa.
- Thư mục `.runtime/` bị Git bỏ qua và không bị `mvn clean` xóa.

Khi phiên còn hiệu lực, có thể chạy lại với cùng profile mà không nhập credentials; phiên hết hạn cần đăng nhập lại. Test thông thường không dùng profile này.

## Remote debugging Chrome

Có thể kết nối Selenium tới Chrome đã đăng nhập bằng remote debugging. Đóng toàn bộ Chrome trước, rồi mở Chrome với profile riêng:

```powershell
$chrome = "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe"
$profile = (Join-Path (Get-Location).Path ".runtime\mobilecity-cart-profile")
Start-Process -FilePath $chrome -ArgumentList @("--remote-debugging-port=9222", "--user-data-dir=`"$profile`"", "https://mobilecity.vn/")
```

Sau khi xác nhận đăng nhập trong cửa sổ đó, chạy trong một PowerShell khác:

```powershell
$env:MOBILECITY_CHROME_DEBUGGER_ADDRESS = "127.0.0.1:9222"
mvn "-Dtest=CartTest" "-Dmobilecity.headless=false" test
```

Chrome đã kết nối sẽ không bị test đóng hoặc xóa cookie. Chỉ mở cổng remote debugging trên máy tin cậy; không để cổng này truy cập từ mạng.
