# Horse Racing Management System – Kiểm Thử Phần Mềm

> Tài liệu dành cho môn **Kiểm Thử Phần Mềm** — bao gồm link Postman Workspace để kiểm thử API và bản tóm tắt SRS (Software Requirements Specification) & Kế hoạch Kiểm thử của dự án.

---

## 📮 Postman – Workspace Kiểm thử API

### Xem Postman Workspace (dành cho Giảng viên & Tester)

> Link bên dưới cho phép xem toàn bộ Workspace Postman của nhóm (bao gồm Collections, Environments, API Tests, ...):

🔗 **[Xem Postman Workspace](https://toritoriisme-5288079.postman.co/workspace/80df1546-c720-43b8-84a5-b957db87f7f2)**

---

## 📋 Tóm tắt SRS (Software Requirements Specification) & Kế hoạch Kiểm thử

### 1. Giới thiệu Dự án

| Mục | Mô tả |
|-----|-------|
| **Tên dự án** | Horse Racing Management System |
| **Mục đích** | Xây dựng nền tảng web quản lý giải đua ngựa toàn diện, hỗ trợ tổ chức giải đấu, đăng ký thi đấu, điều hành cuộc đua, giám sát trọng tài và kiểm thử chất lượng phần mềm |
| **Phạm vi** | Hệ thống bao gồm Backend REST API (Spring Boot 3.x), Database MongoDB Atlas, và Frontend SPA (ReactJS + Vite) |
| **Đối tượng sử dụng** | Admin, Horse Owner (Chủ ngựa), Jockey (Nài ngựa), Referee (Trọng tài), Spectator (Khán giả) |

---

### 2. Tiến độ Thực hiện & Phân công Sprint 1 & Sprint 2

#### 🟢 Sprint 1: Môi trường, Đăng ký & Bảo mật (Đã Hoàn thành & Verified)
* **Thành viên 1 & 2:** Xây dựng backend kiến trúc N-Tier (Spring Boot), cấu hình kết nối MongoDB Atlas & Spring Security stateless JWT.
* **Thành viên 3 (Frontend & Manual Blackbox Testing):**
  * Thiết lập môi trường React/Vite (`npm run dev` trên `localhost:5173`) kết nối Backend `localhost:8080`.
  * Thực thi Manual Test Case Đăng ký (`POST /api/auth/register`) với vai trò Khán giả (`ROLE_SPECTATOR`).
  * Verify mã hóa password `bcrypt` trong MongoDB Atlas & chuyển hướng tự động ➔ **PASS 🟢**.
* **Thành viên 4 (Agile/Kanban & Bug Life Cycle Management):**
  * Thiết lập luồng Kanban 7 bước trên Jira Software: `New` ➔ `Assigned` ➔ `In Progress` ➔ `In Review` ➔ `Ready for QA` ➔ `Verified` ➔ `Closed`.
  * Móc nối Repo GitHub với Jira & kích hoạt **Smart Commits** (`#in-progress`, `#in-review`, `#ready-for-qa`, `#verified`, `#closed`).

#### 🔵 Sprint 2: Core Entities (Ngựa, Nài Ngựa, Giải Đấu & Chặng Đua) - Kế hoạch Thực thi

| Mã Task | Thành viên / Vai trò | Hạng mục Nhiệm vụ Sprint 2 |
|:---:|:---|:---|
| **K2-6** | **Thành viên 1**<br>*(Backend & Unit Test)* | Phát triển và viết Unit Test (JUnit 5 & Mockito) cho các service thực thể:<br>• `HorseServiceTest`: Unit test tạo mới Ngựa, validate tuổi ngựa (> 0) và các trường thông tin không được rỗng.<br>• `JockeyServiceTest`: Unit test CRUD Nài ngựa, validate số giấy phép hành nghề và năm kinh nghiệm.<br>• `TournamentServiceTest` & `RaceServiceTest`: Unit test logic tạo Giải đấu (validate `startDate < endDate`) và Chặng đua. |
| **K2-7** | **Thành viên 2**<br>*(API & Integration Test)* | Kiểm thử tích hợp tầng Controller và chuẩn hóa Request trên Postman:<br>• Sửa prefix đường dẫn Postman từ `/api/horses`, `/api/jockeys` ➔ `/api/v1/horses` & `/api/v1/jockeys`.<br>• Integration Test (`@SpringBootTest`, `MockMvc` / `MockMvcTester`): Gửi request tạo Ngựa/Nài ngựa & verify dữ liệu lưu vào MongoDB.<br>• Postman Automated Assertions: Script check HTTP status 200/201, tự động lưu `tournamentId` truyền sang API tạo chặng đua. |
| **K2-8** | **Thành viên 3**<br>*(Frontend & Manual Test)* | Thực thi kiểm thử thủ công hộp đen (Blackbox UI Test):<br>• Manual Test Owner Dashboard: Phân hoạch lớp tương đương (EP) & Phân tích giá trị biên (BVA) nhập liệu Tuổi ngựa, năm kinh nghiệm.<br>• Manual Test Admin Dashboard: Verify giao diện báo lỗi khi chọn `startDate > endDate`.<br>• Network Tab Audit (F12): Đảm bảo DTO gửi chuẩn xuống Backend & render đúng khi reload. |
| **K2-9** | **Thành viên 4**<br>*(Security & QA Specialist)* | Kiểm thử bảo mật phân quyền (RBAC/JWT) & Phi chức năng (Performance/Load):<br>• Security Testing: Test `@WithMockUser` / Postman dùng Token Khán giả/Nài ngựa gọi API Admin ➔ Assert `403 Forbidden`. Test token hết hạn / sửa chữ ký ➔ Assert `401 Unauthorized`.<br>• Performance Testing: Dùng JMeter / Postman Runner giả lập request tạo đồng thời nhiều Ngựa/Nài ngựa, đo Response Time & tính ổn định kết nối MongoDB.<br>• QA Defect & Log Analysis: Reproduce bug thô từ API/UI, phân tích stack trace log console & MongoDB, tạo Bug Ticket gửi lên Jira. |

---

### 3. Kết quả Bộ Test Case Bảo mật API (Security Test Suite Matrix)

Bộ test case kiểm thử phân quyền tới API Admin `GET /api/admin/management/registrations/pending`:

| Mã TC | Tên / Mô tả kịch bản test | Kết quả kỳ vọng (Expected) | Kết quả thực tế (Actual) | Trạng thái | Ghi chú / Căn cứ |
| :--- | :--- | :--- | :--- | :---: | :--- |
| **TC-SEC-01** | Test phân quyền với Token Chủ ngựa / User | Trả về `403 Forbidden` (Xác thực thành công nhưng không đủ quyền) | Trả về `401 Unauthorized` | **FAIL 🔴** | **Bug #01:** Chặn đúng logic nhưng sai HTTP Status Code (Trả về `401` thay vì `403`). |
| **TC-SEC-02** | Test phân quyền với Token Khán giả (`ROLE_SPECTATOR`) | Trả về `403 Forbidden` | Trả về `401 Unauthorized` | **FAIL 🔴** | **Bug #01:** Lỗi cấu hình Security tương tự TC-SEC-01. |
| **TC-SEC-03** | Test xác thực Khách vãng lai (`No Auth`) | Trả về `401 Unauthorized` | Trả về `401 Unauthorized` | **PASS 🟢** | Chặn khách vãng lai thành công. |
| **TC-SEC-04** | Test xác thực JWT Token bị sửa đổi / sai chữ ký | Trả về `401 Unauthorized` | Trả về `401 Unauthorized` | **PASS 🟢** | Validate chữ ký JWT chuẩn xác. |
| **TC-SEC-05** | Happy Case với Token Admin hợp lệ | Trả về `200 OK` kèm mảng JSON danh sách chờ | Trả về `200 OK` kèm danh sách chờ | **PASS 🟢** | API hoạt động đúng thiết kế. |

---

### 4. Yêu cầu Chức năng (Functional Requirements Overview)

* **Auth & Security:** Đăng ký, đăng nhập, JWT stateless token, phân quyền RBAC (`@PreAuthorize`).
* **Tournaments & Races:** Quản lý giải đấu (`/api/admin/tournaments`), chặng đua (`/api/admin/races`), xét duyệt đơn đăng ký (`/api/admin/management/registrations`).
* **Horses & Jockeys:** Quản lý ngựa đua (`/api/v1/horses`), nài ngựa (`/api/v1/jockeys`).
* **Referee Operations:** Quản lý báo cáo, vi phạm, khám sức khỏe ngựa và ghi nhận kết quả (`/api/referee/...`).
* **Spectator & Betting:** Đặt cược, xem danh sách trận, nạp ví điện tử (`/api/v1/spectator/...`).

---

### 5. Yêu cầu Phi chức năng (Non-Functional Requirements)

| ID | Loại | Yêu cầu |
|----|------|---------|
| NFR-01 | **Bảo mật** | Stateless JWT Authentication, BCrypt password hashing, RBAC với `@PreAuthorize` |
| NFR-02 | **Hiệu năng** | MongoDB NoSQL, Optimistic Locking (`@Version`) chống xung đột dữ liệu đồng thời |
| NFR-03 | **Khả dụng** | CORS hỗ trợ development, Session Stateless |
| NFR-04 | **Giao diện** | Frontend SPA ReactJS + Vite, Redux Toolkit, React Router |
| NFR-05 | **Tương thích** | Java 17+, Spring Boot 3.x, ReactJS 18+ |
