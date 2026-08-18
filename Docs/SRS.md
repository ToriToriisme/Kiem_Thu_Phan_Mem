# SOFTWARE REQUIREMENTS SPECIFICATION (SRS) & TESTING DOCUMENTATION
## Dự án: Horse Racing Management System
> **Môn học:** Kiểm Chứng Phần Mềm (Software Testing)  
> **Phiên bản:** 2.0  
> **Cập nhật mới nhất:** 2026-08-08  

---

## I. TỔNG QUAN PHÂN CÔNG THÀNH VIÊN & MÔI TRƯỜNG DỰ ÁN

### 1. Phân công Công việc & Tiến độ Sprint 1 (Đã hoàn tất)
* **Thành viên 1 & 2:** Backend Architecture, Spring Boot REST APIs, Security Config & MongoDB Atlas Integration.
* **Thành viên 3 (Frontend & Manual Blackbox Testing):**
  * **Môi trường:** 
    * Source code Frontend (React/Vite) chạy trên `http://localhost:5173` thông qua `npm run dev`.
    * Backend (Spring Boot) chạy song song trên `http://localhost:8080`, kết nối MongoDB Atlas thành công.
  * **Test Case Kiểm thử Đăng ký (Register):**
    * *Data test:* `username`: "Ba", `fullname`: "Nguyễn Ba", `email`: "ba_test1@gmail.com", `roleKey`: "ROLE_SPECTATOR" (Khán giả).
    * *UI:* Điền form đăng ký ➔ Click **Đăng Ký** ➔ Thông báo thành công & tự động chuyển hướng sang trang Đăng Nhập.
    * *Network:* HTTP Request `POST /api/auth/register` trả về HTTP status `200 OK`, response body: `{"message":"User registered successfully!"}`.
    * *Database (MongoDB Atlas):* Mật khẩu đã được mã hóa theo chuẩn `bcrypt` (không lưu plain text), role liên kết chính xác tới `ROLE_SPECTATOR` ("Khán giả").
    * *Kết quả:* **PASS 🟢**
* **Thành viên 4 (Agile/Kanban & Bug Life Cycle Management):**
  * **Mục tiêu:** Thiết lập không gian làm việc Agile/Kanban cho nhóm và tự động hóa quy trình theo dõi lỗi (Bug Life Cycle) tích hợp giữa GitHub và Jira.
  * **Hạng mục hoàn tất:**
    1. *Quy trình (Workflow):* Cấu hình bảng Kanban 7 bước chuẩn hóa:  
       `New` ➔ `Assigned` ➔ `In Progress` ➔ `In Review` ➔ `Ready for QA` ➔ `Verified` ➔ `Closed`.
    2. *Tích hợp hệ thống:* Kết nối thành công Repository GitHub của nhóm với hệ thống Jira Software.
    3. *Smart Commits:* Kích hoạt tính năng tự động chuyển trạng thái thẻ (Jira Ticket) dựa trên lịch sử commit code của Developer.
    4. *Bảng quy ước từ khóa Smart Commits:*
       * `#in-progress`: Chuyển thẻ sang cột *In Progress*
       * `#in-review`: Chuyển thẻ sang cột *In Review*
       * `#ready-for-qa`: Chuyển thẻ sang cột *Ready for QA*
       * `#verified` / `#closed`: Chuyển thẻ sang cột *Verified* hoặc *Closed*
       * *Cú pháp mẫu:* `git commit -m "K2-1 #in-progress Bắt đầu fix logic phân quyền API Admin"`

---

## II. ĐẶC TẢ YÊU CẦU PHẦN MỀM (SOFTWARE REQUIREMENTS SPECIFICATION)

### 1. Phân hệ Xác thực & Phân quyền (Authentication & Authorization)
* **Auth-01 (Đăng ký / Đăng nhập):** Cho phép người dùng đăng ký tài khoản với các vai trò: Spectator (Khán giả), Horse Owner (Chủ ngựa), Jockey (Nài ngựa), Referee (Trọng tài), Admin. Mã hóa mật khẩu bằng BCrypt. Trả về Bearer JWT Token khi đăng nhập thành công.
* **Auth-02 (Role-Based Access Control - RBAC):** 
  * Public APIs: `/api/auth/**`, `/api/v1/spectator/**`, `/api/v1/horses` (GET).
  * Admin APIs: Các đường dẫn `/api/admin/**` chỉ dành riêng cho người dùng có token chứa quyền `ADMIN` (`ROLE_ADMIN`).
  * Referee APIs: `/api/referee/**` dành riêng cho Trọng tài.

### 2. Phân hệ Quản lý Giải đấu & Xét duyệt (Tournaments & Admin Approvals)
* **TRN-01:** Admin khởi tạo giải đấu (`POST /api/admin/tournaments`), quản lý các chặng đua (Race).
* **TRN-02:** Admin duyệt đơn đăng ký tham gia thi đấu (`PUT /api/admin/management/registrations/{id}/approve`) hoặc từ chối (`PUT /api/admin/management/registrations/{id}/reject`).
* **TRN-03:** Admin lấy danh sách các đơn đăng ký đang chờ duyệt (`GET /api/admin/management/registrations/pending`).

### 3. Phân hệ Giám sát Trọng tài (Referee Management)
* **REF-01:** Trọng tài ghi nhận báo cáo trận đấu, kiểm tra sức khỏe ngựa và ghi nhận vi phạm (`/api/referee/...`).
* **REF-02:** Cập nhật kết quả chặng đua (vị trí, thời gian hoàn thành, tiền thưởng).

---

## III. BỘ TEST CASE BẢO MẬT & PHÂN QUYỀN API (SECURITY TEST SUITE)

### 1. Postman Collection Test Suite (`KCPM_Test`)
Bộ kịch bản kiểm thử bảo mật API kiểm tra cơ chế phân quyền đối với endpoint Admin `GET http://localhost:8080/api/admin/management/registrations/pending`:

```json
{
  "info": {
    "_postman_id": "c3b20229-0315-474b-9162-3748f8090d39",
    "name": "KCPM_Test",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    { "name": "TC-SEC-01", "description": "Test phân quyền với Token Chủ ngựa / User" },
    { "name": "TC-SEC-02", "description": "Test phân quyền với Token Khán giả (ROLE_SPECTATOR)" },
    { "name": "TC-SEC-03", "description": "Test xác thực Khách vãng lai (No Auth)" },
    { "name": "TC-SEC-04", "description": "Test xác thực với JWT Token sai cấu trúc / bị sửa đổi" },
    { "name": "TC-SEC-05", "description": "Happy Case với Token Admin hợp lệ" }
  ]
}
```

### 2. Báo cáo Kết quả Kiểm thử Bảo mật (Security Test Execution Matrix)

| Mã TC | Tên / Mô tả kịch bản test | Kết quả kỳ vọng (Expected) | Kết quả thực tế (Actual) | Trạng thái | Ghi chú / Căn cứ |
| :--- | :--- | :--- | :--- | :---: | :--- |
| **TC-SEC-01** | **Test phân quyền:** Truy cập API Admin bằng Token của tài khoản có quyền Chủ ngựa / User. | Hệ thống từ chối truy cập và trả về mã lỗi `403 Forbidden` (Xác thực thành công nhưng không đủ quyền). | Hệ thống từ chối truy cập nhưng trả về mã lỗi `401 Unauthorized`. | **FAIL 🔴** | **Bug:** Chặn đúng logic nhưng sai chuẩn HTTP Status Code. Đáng lẽ phải là `403` thay vì `401`. |
| **TC-SEC-02** | **Test phân quyền:** Truy cập API Admin bằng Token của tài khoản có quyền Khán giả (`ROLE_SPECTATOR`). | Hệ thống từ chối truy cập và trả về mã lỗi `403 Forbidden`. | Hệ thống từ chối truy cập nhưng trả về mã lỗi `401 Unauthorized`. | **FAIL 🔴** | **Bug:** Lỗi cấu hình Security tương tự TC-SEC-01 (Trả về `401` thay vì `403`). |
| **TC-SEC-03** | **Test xác thực:** Khách vãng lai (Guest) gọi API Admin nhưng không truyền Token vào Header (`No Auth`). | Hệ thống yêu cầu xác thực và trả về mã lỗi `401 Unauthorized`. | Trả về `401 Unauthorized` kèm message lỗi xác thực. | **PASS 🟢** | Hệ thống chặn khách vãng lai thành công. |
| **TC-SEC-04** | **Test xác thực:** Truy cập API Admin bằng một Token bị sửa đổi, fake chữ ký hoặc sai cấu trúc JWT. | Hệ thống phát hiện Token không hợp lệ và trả về mã lỗi `401 Unauthorized`. | Trả về `401 Unauthorized`. | **PASS 🟢** | Hệ thống Validate chữ ký của Token chuẩn xác, chặn lọt thẻ giả. |
| **TC-SEC-05** | **Happy Case:** Truy cập API bằng Token hợp lệ của tài khoản có quyền `ADMIN`. | Trả về HTTP `200 OK` kèm theo danh sách dữ liệu các đơn đăng ký đang chờ duyệt. | Trả về `200 OK` và lấy thành công mảng JSON chứa danh sách chờ. | **PASS 🟢** | API hoạt động đúng luồng chuẩn thiết kế. |

---

## IV. BÁO CÁO PHÂN TÍCH LỖI & ĐỀ XUẤT NÂNG CẤP (DEFECT REPORT)

### Bug #01: Sai HTTP Status Code khi từ chối quyền truy cập (Authorization Mismatch)
* **Liên quan:** `TC-SEC-01`, `TC-SEC-02`
* **Mô tả hiện trạng:** Khi người dùng đã được xác thực (JWT hợp lệ) nhưng thiếu vai trò `ADMIN` gọi vào endpoint `/api/admin/management/registrations/pending`, hệ thống trả về mã HTTP `401 Unauthorized` thay vì `403 Forbidden`.
* **Phân tích kỹ thuật:** Chuỗi bộ lọc `Spring Security FilterChain` hiện tại thiếu cấu hình custom `AccessDeniedHandler` để phân biệt rõ ràng:
  * **401 Unauthorized (Unauthenticated):** Chưa đăng nhập, thiếu token, hoặc token không hợp lệ / hết hạn.
  * **403 Forbidden (Unauthorized/Access Denied):** Đã đăng nhập hợp lệ nhưng tài khoản không có đủ thẩm quyền thực thi API đó.
* **Giải pháp khắc phục:**
  Bổ sung custom `AccessDeniedHandler` trong `SecurityConfig.java`:
  ```java
  .exceptionHandling(exception -> exception
      .authenticationEntryPoint((request, response, authException) -> 
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Authentication token is missing or invalid"))
      .accessDeniedHandler((request, response, accessDeniedException) -> 
          response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: You do not have permission to access this resource"))
  )
  ```

---

## V. KẾ HOẠCH & MỤC TIÊU SPRINT 2 (CORE ENTITIES & DETAILED TESTING)

### 1. Tổng quan Sprint 2
* **Phân hệ trọng tâm:** Core Entities (Ngựa, Nài Ngựa, Giải Đấu & Chặng Đua).
* **Mục tiêu:** Triển khai và kiểm thử hoàn chỉnh các chức năng cốt lõi (CRUD) liên quan đến các thực thể chính của hệ thống: Ngựa (`Horses`), Nài ngựa (`Jockeys`), Giải đấu (`Tournaments`) và Chặng đua (`Races`).

### 2. Phân công Chi tiết & Các Hạng mục Cần Hoàn thành (K2-6 đến K2-9)

#### 🔹 Thành viên 1: Backend & Unit Testing Specialist (Task: K2-6)
* **Nhiệm vụ chính:** Phát triển và viết Unit Test (JUnit 5 & Mockito) cho các service quản lý thực thể.
* **Hạng mục công việc chi tiết:**
  * **Unit Test cho `HorseService`:** Viết test case tạo mới Ngựa; kiểm tra các ràng buộc dữ liệu đầu vào (VD: Tuổi ngựa phải hợp lệ > 0, các trường bắt buộc không được rỗng).
  * **Unit Test cho `JockeyService`:** Viết trọn bộ test CRUD cho Nài ngựa (kiểm tra tính hợp lệ của số giấy phép hành nghề, năm kinh nghiệm >= 0).
  * **Unit Test cho `TournamentService` & `RaceService`:** Kiểm thử logic tạo Giải đấu (validate ngày bắt đầu phải trước ngày kết thúc) và tạo Chặng đua thuộc giải đấu.

#### 🔹 Thành viên 2: API & Integration Testing Specialist (Task: K2-7)
* **Nhiệm vụ chính:** Kiểm thử tích hợp tầng Controller và chuẩn hóa các request trên Postman.
* **Hạng mục công việc chi tiết:**
  * **Chuẩn hóa Prefix Route trên Postman:** Thay đổi toàn bộ các route gọi thực thể từ `/api/horses`, `/api/jockeys` thành `/api/v1/horses` và `/api/v1/jockeys`.
  * **Integration Test (`@SpringBootTest`, `MockMvc` / `MockMvcTester`):** Gửi request tạo Ngựa/Nài ngựa và xác minh dữ liệu được lưu thành công vào MongoDB (hoặc in-memory DB phục vụ test).
  * **Viết Assertions tự động trên Postman:** Thiết lập script kiểm tra mã trạng thái trả về (`200 OK`, `201 Created`) và tự động lưu `tournamentId` sau khi tạo thành công để truyền cho API tạo chặng đua.

#### 🔹 Thành viên 3: Frontend & Manual Testing Specialist (Task: K2-8)
* **Nhiệm vụ chính:** Thực thi kiểm thử thủ công hộp đen (Blackbox Manual Test) trên UI đối với giao diện quản trị và chủ ngựa.
* **Hạng mục công việc chi tiết:**
  * **Manual Test màn hình Owner Dashboard:** Áp dụng kỹ thuật Phân hoạch lớp tương đương (EP) và Phân tích giá trị biên (BVA) để nhập liệu các trường Tuổi ngựa, năm kinh nghiệm nài ngựa trên UI.
  * **Manual Test màn hình Admin Dashboard (Tạo giải đấu & chặng đua):** Kiểm tra giao diện xem có hiển thị đúng thông báo lỗi khi Admin chọn ngày bắt đầu giải đấu muộn hơn ngày kết thúc hay không.
  * **Kiểm tra Network Tab (F12):** Đảm bảo UI gửi đúng cấu trúc DTO xuống Backend và hiển thị dữ liệu chính xác sau khi tải lại trang.

#### 🔹 Thành viên 4: Security & QA Specialist (Task: K2-9)
* **Nhiệm vụ chính:** Kiểm thử bảo mật phân quyền (RBAC/JWT) và kiểm thử phi chức năng (Performance/Load Testing).
* **Hạng mục công việc chi tiết:**
  * **Security Testing (RBAC & JWT):** Viết các lớp kiểm thử tích hợp dùng `@WithMockUser` hoặc Postman đính kèm Token Khán giả (`ROLE_SPECTATOR`) / Nài ngựa (`ROLE_JOCKEY`) để gọi API Admin tạo giải đấu (`POST /api/admin/tournaments`) hoặc chặng đua. Xác minh kết quả trả về phải là `HTTP 403 Forbidden`. Đồng thời gửi request chứa Token hết hạn hoặc chữ ký bị sửa đổi để kiểm tra bộ lọc `JwtAuthenticationFilter` chặn với mã `HTTP 401 Unauthorized`.
  * **Performance & Load Testing:** Sử dụng công cụ sinh tải (JMeter hoặc Postman Collection Runner) giả lập gửi đồng thời nhiều request tạo mới Ngựa/Nài ngựa. Đo lường Response Time và kiểm tra tính ổn định của kết nối MongoDB.
  * **QA Defect & Log Analysis:** Tiếp nhận báo cáo lỗi thô từ Thành viên 2 (API) và Thành viên 3 (Manual UI). Chạy tái tạo lỗi (Reproduce) dưới local, bóc tách log console và MongoDB stack trace để tìm chính xác dòng code bị lỗi, sau đó tạo Bug Ticket chi tiết lên Jira.
