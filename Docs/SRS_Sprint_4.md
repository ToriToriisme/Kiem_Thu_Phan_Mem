# SRS Sprint 4 — Horse Racing Management System

> **Software Requirements Specification — Kế Hoạch Sprint 4**  
> **Dự án:** Horse Racing Management System  
> **Cập nhật:** 2026-08-19  

---

## 👥 1. Phân Công Trách Nhiệm Sprint 4 & Sprint 5

### 🛠 Lê Đức Thiện — Backend & Unit Test
* **Sprint 4**:
  * Fix BUG-04 (`SecurityConfig.java` 401 vs 403) & BUG-01 (Giới hạn cược), BUG-02 (Giới hạn tuổi ngựa).
  * Viết Unit Test cho `HorseService`, `JockeyService`, `TournamentService`.
  * Cài đặt và chạy SonarQube scan cho Backend.
* **Sprint 5**:
  * Fix các lỗi từ SonarQube report, viết Unit Test bổ sung và tạo file `docker-compose.yml`.

### 🔗 Nguyễn Thanh Phú — API & Integration Test (MSSV: 075205013708)
* **Sprint 4**:
  * Thêm automated assertions và lưu token tự động cho 35 endpoints Postman.
  * Viết Integration Test cho `HorseController`, `JockeyController`, `TournamentController`.
  * Chuẩn hóa format ngày tháng JSON trả về từ Controller (hỗ trợ BUG-03).
  * Viết `Dockerfile` cho Backend Spring Boot.
* **Sprint 5**:
  * Viết test kịch bản lỗi biên Postman, Integration Test quy trình trả thưởng, xuất báo cáo Newman.

### 🖥 Đỗ Cao Nhất — Frontend & Manual Test (MSSV: 082205001294)
* **Sprint 4**:
  * Fix BUG-03 (viết helper `formatDateTime` triệt tiêu lỗi `Invalid day/date`).
  * Khóa input form cược `$1 - $5000` (BUG-01) và tuổi ngựa `2 - 15` (BUG-02).
  * Tối ưu UI/UX responsive trên mobile, tablet và desktop.
  * Cài đặt CodeceptJS + Playwright và viết E2E test đăng nhập/đăng ký.
  * Viết `Dockerfile` cho Frontend (Multi-stage Nginx).
* **Sprint 5**:
  * Viết E2E test cho luồng Owner & Admin, thực thi kiểm thử hộp đen xuất file Excel.

### 🔒 Chu Trần Duy Hoàng — Security & QA Workflow (MSSV: 066206012384)
* **Sprint 4**:
  * Mở rộng Security Regression Suite với `@WithMockUser` cho các controller còn lại.
  * Thiết kế ma trận Test Case Giá trị biên (BVA) cho hạn mức cược và tuổi ngựa.
  * Phân tích Security Hotspots trên SonarQube Dashboard.
  * Quản lý vòng đời Bug trên Jira Board.
* **Sprint 5**:
  * Chạy Final Security Audit, thực hiện kiểm thử hiệu năng (Load Test), tổng kết slide báo cáo Tuần 8.

---

## 📋 2. Yêu Cầu Chức Năng Sprint 4 (Functional Requirements)

Các yêu cầu chức năng được fix, phát triển và kiểm thử trong Sprint 4:

### 2.1 Bug Fixes — Yêu cầu chức năng cần sửa lỗi

| Mã FR (SRS tổng) | Bug liên quan | Nội dung fix | Phụ trách |
|:--|:--:|:--|:--|
| **FR-BET-01** | BUG-01 | Thêm validation `@Min(1) @Max(5000)` cho `amount` trong `BetDTO.java`. Frontend khóa input `min=1 max=5000`. | Lê Đức Thiện (BE) + Đỗ Cao Nhất (FE) |
| **FR-HORSE-01** | BUG-02 | Thêm validation `@Min(2) @Max(15)` cho `age` trong `HorseDTO.java`. Frontend khóa input `min=2 max=15`. | Lê Đức Thiện (BE) + Đỗ Cao Nhất (FE) |
| **FR-AUTH-03** | BUG-04 | Bổ sung `AccessDeniedHandler` trong `SecurityConfig.java` để trả đúng `403 Forbidden` khi user thiếu quyền. | Lê Đức Thiện |

### 2.2 Kiểm thử chức năng mở rộng

| Mã FR (SRS tổng) | Loại kiểm thử Sprint 4 | Phụ trách |
|:--|:--|:--|
| **FR-HORSE-01, 02, 03** | Unit Test `HorseService` + Integration Test `HorseController` | Lê Đức Thiện + Nguyễn Thanh Phú |
| **FR-JOCKEY-01, 02** | Unit Test `JockeyService` + Integration Test `JockeyController` | Lê Đức Thiện + Nguyễn Thanh Phú |
| **FR-TRN-01, 02, 03, 04** | Unit Test `TournamentService` + Integration Test `TournamentController` | Lê Đức Thiện + Nguyễn Thanh Phú |
| **FR-AUTH-01, 02** | E2E Test Login/Register (CodeceptJS + Playwright) | Đỗ Cao Nhất |
| **Toàn bộ FR** | Postman Assertions tự động cho 35 endpoints | Nguyễn Thanh Phú |

---

## 🔒 3. Yêu Cầu Phi Chức Năng Sprint 4 (Non-Functional Requirements)

Các yêu cầu phi chức năng được kiểm thử và cải thiện trong Sprint 4:

| Mã NFR (SRS tổng) | Mô tả yêu cầu | Phương pháp kiểm thử / cải thiện Sprint 4 | Phụ trách |
|:--|:--|:--|:--|
| **NFR-SEC-02** | Xác thực JWT Stateless | Mở rộng Security Regression Suite `@WithMockUser` cho tất cả controllers | Chu Trần Duy Hoàng |
| **NFR-SEC-03** | Phân quyền chính xác 401 vs 403 | Fix BUG-04 + Verify bằng Security Regression Test | Lê Đức Thiện + Chu Trần Duy Hoàng |
| **NFR-SEC-04** | Code quality & Security Hotspots | Cài đặt SonarQube Community, chạy Scan Backend, phân tích Hotspots | Lê Đức Thiện + Chu Trần Duy Hoàng |
| **NFR-USE-01** | Responsive Design | Tối ưu UI/UX trên Mobile / Tablet / Desktop | Đỗ Cao Nhất |
| **NFR-USE-02** | Hiển thị ngày tháng chính xác | Fix BUG-03 (`formatDateTime` helper) + chuẩn hóa JSON Date DTO | Đỗ Cao Nhất + Nguyễn Thanh Phú |
| **NFR-USE-03** | Thông báo lỗi rõ ràng | Validation messages tiếng Việt cho form cược & form ngựa | Lê Đức Thiện (BE) + Đỗ Cao Nhất (FE) |

### Test Case BVA (Boundary Value Analysis) — Sprint 4

#### BVA cho hạn mức cược (FR-BET-01):
| Test Input | Expected Result | Mã NFR |
|:--:|:--|:--:|
| `$0` | `400 Bad Request` — Dưới hạn mức tối thiểu | NFR-USE-03 |
| `$1` | `200 OK` — Giá trị biên dưới hợp lệ | — |
| `$2,500` | `200 OK` — Giá trị trung bình | — |
| `$5,000` | `200 OK` — Giá trị biên trên hợp lệ | — |
| `$5,001` | `400 Bad Request` — Vượt hạn mức tối đa | NFR-USE-03 |
| `$10,000,000` | `400 Bad Request` — Chặn ở cả FE và BE | NFR-REL-01 |

#### BVA cho tuổi ngựa (FR-HORSE-01):
| Test Input | Expected Result | Mã NFR |
|:--:|:--|:--:|
| `1 tuổi` | `400 Bad Request` — Dưới tuổi tối thiểu | NFR-USE-03 |
| `2 tuổi` | `201 Created` — Giá trị biên dưới hợp lệ | — |
| `8 tuổi` | `201 Created` — Giá trị trung bình | — |
| `15 tuổi` | `201 Created` — Giá trị biên trên hợp lệ | — |
| `16 tuổi` | `400 Bad Request` — Vượt tuổi tối đa | NFR-USE-03 |
| `-5 tuổi` | `400 Bad Request` — Giá trị âm không hợp lệ | NFR-USE-03 |

---

## 🎯 4. Tiêu Chí Hoàn Thành (Definition of Done) Sprint 4
1. Toàn bộ 4 bugs (BUG-01 đến BUG-04) được fix và verified bởi QA.
2. Unit Test & Integration Test Backend đạt độ bao phủ cơ bản không lỗi.
3. CodeceptJS chạy E2E test giao diện PASS.
4. Postman 35 endpoints có script assert tự động.
5. Dockerfile Backend và Frontend được chuẩn bị sẵn sàng cho đóng gói tuần 7.
6. SonarQube scan Backend — không có Security Hotspot mức Critical.
7. Toàn bộ BVA test case cho hạn mức cược và tuổi ngựa đều PASS.
