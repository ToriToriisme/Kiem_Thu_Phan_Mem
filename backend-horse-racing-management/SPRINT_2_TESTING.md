# Sprint 2 - Integration Test và Postman Assertions

## Nội dung thực hiện

* Xây dựng Integration Test bằng `@SpringBootTest` và `MockMvc`.
* Kiểm thử API quản lý Ngựa, Nài ngựa và Giải đấu.
* Kiểm tra các trường hợp có và không có xác thực.
* Bổ sung Assertions cho Postman Collection.
* Sử dụng database test riêng, không ghi dữ liệu vào database chính.

## Chạy Integration Test

Kiểm tra Maven đang sử dụng Java 26:

```powershell
.\mvnw.cmd -version
```

Chạy toàn bộ test:

```powershell
.\mvnw.cmd test
```

Kết quả đã kiểm tra:

```text
Tests run: 12
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

## Kiểm tra Postman

Chạy các request theo thứ tự:

```text
Register → Login → Get All Horses → Get All Jockeys → Get All Tournaments
```

Sau mỗi request, mở `Test Results` để kiểm tra các Assertions.
