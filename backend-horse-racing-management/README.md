# Backend - Horse Racing Management System 🏇

Đây là hệ thống Backend phục vụ cho dự án Quản lý Đua Ngựa và Cá cược, được xây dựng dựa trên kiến trúc 3 lớp (3-Tier Architecture) chuẩn mực của Java Spring Boot, kết hợp với các nguyên lý Lập trình hướng đối tượng (OOP) chặt chẽ.

## 🛠️ Công nghệ & Thư viện sử dụng
Hệ thống sử dụng các thư viện cốt lõi (xem chi tiết trong `pom.xml`):
- **Spring Boot Web**: Chạy máy chủ và cung cấp RESTful APIs.
- **Spring Data MongoDB**: Giao tiếp với cơ sở dữ liệu NoSQL (MongoDB).
- **Spring Security & JJWT**: Bảo vệ hệ thống bằng bộ lọc an ninh và công nghệ JSON Web Token (JWT) (version 0.11.5).
- **Lombok**: Giảm thiểu code rườm rà (tự động tạo Getter, Setter, Constructor).
- **Spring Validation**: Kiểm tra tính hợp lệ của dữ liệu đầu vào.

---

## 🏗️ Kiến trúc 3 Lớp (3-Tier Architecture)
Dự án được phân chia thư mục (Separation of Concerns) rất rõ ràng để dễ bảo trì:

1. **`controller` (Tiếp tân)**: Đón nhận Request từ Frontend (gọi qua API), chuyển giao dữ liệu xuống Service, và trả Response về cho Frontend. Không chứa logic nghiệp vụ phức tạp.
2. **`service` (Não bộ)**: Nơi chứa 100% logic phức tạp (tính tiền thưởng, phân quyền, kiểm tra logic nghiệp vụ). Nhận lệnh từ Controller và xử lý.
3. **`repository` (Thủ kho)**: Cầu nối duy nhất với MongoDB. Nhờ `MongoRepository`, các hàm giao tiếp DB được tự động sinh ra mà không cần viết lệnh SQL/NoSQL thủ công.
4. **`entity` & `dto`**: 
   - **Entity**: Bản vẽ cấu trúc bảng lưu dưới Database.
   - **DTO (Data Transfer Object)**: Hộp đóng gói dữ liệu để vận chuyển giữa Client và Server (giúp bảo mật và gọn nhẹ).
5. **`config` & `security`**: Nơi khai báo các `@Bean` (tạo đối tượng đưa vào kho IoC Container của Spring) và thiết lập tường lửa (CORS, JWT Filter).

---

## 🔄 Luồng chạy của một Request
Mỗi khi Frontend gửi một Request lên Backend, luồng dữ liệu sẽ đi qua các trạm sau:
1. **Trạm 1 (Jwt Filter)**: Đón Request, bóc tách Token JWT, moi `username` ra, vào DB tra cứu Role (Vai trò) và cấp quyền đi tiếp.
2. **Trạm 2 (Controller & @PreAuthorize)**: Kiểm tra xem User này có quyền (`hasRole`) gọi API này không. Nếu có, nhận lấy cục `DTO`.
3. **Trạm 3 (Service)**: Controller đẩy `DTO` xuống Service. Service tháo ra, kiểm tra quyền sở hữu (Ownership check), tính toán logic.
4. **Trạm 4 (Repository & DB)**: Service gọi Repository để chọc xuống Database lấy/Lưu `Entity`.
5. **Trạm 5 (Return)**: Service biến `Entity` thành `DTO` ném lại cho Controller, Controller trả về cho UI.

---

## 🎓 Ứng dụng Lập trình Hướng đối tượng (OOP)
Hệ thống là một minh chứng sống động cho 4 tính chất của OOP:

### 1. Tính Đóng Gói (Encapsulation)
- **Thể hiện ở**: `Entity`, `DTO` (các biến `private` được truy xuất qua Get/Set).
- **Thể hiện ở**: `@Bean` (giấu đi sự phức tạp của quá trình khởi tạo đối tượng, ví dụ: giấu công nghệ `BCrypt` bên trong hàm tạo `PasswordEncoder`, các class khác cứ lấy ra xài mà không cần biết ruột).

### 2. Tính Kế Thừa (Inheritance)
- **Thể hiện ở**: `Repository`. Ví dụ: `BetRepository extends MongoRepository<Bet, String>`. Kế thừa toàn bộ các hàm `save()`, `findById()` từ thằng cha mà không cần viết code.

### 3. Tính Trừu Tượng (Abstraction)
- **Thể hiện ở**: `Service Interface` (VD: `JockeyService.java`). Nó chỉ cung cấp "Cái vỏ" (Tên hàm) cho Controller gọi, giấu đi hoàn toàn "Cái ruột" phức tạp (cách kết nối DB, tính toán) ở file `JockeyServiceImpl`.
- **Thể hiện ở**: `Repository Interface` (chỉ khai báo tên hàm `findByRaceId`, Spring Data MongoDB tự động dịch ra lệnh DB ngầm).

### 4. Tính Đa Hình (Polymorphism)
- **Thể hiện ở**: Kỹ thuật *Dependency Injection (DI)*. Trong Controller khai báo gọi Interface (Cái vỏ): `@Autowired private JockeyService service;`. Nhưng lúc chạy thật, Spring Boot linh hoạt "nhét" thằng Con (`JockeyServiceImpl`) vào để chạy. Code mang tính cắm-và-chạy (Plug & Play) rất cao.
