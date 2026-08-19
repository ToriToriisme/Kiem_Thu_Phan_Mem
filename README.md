# SOFTWARE REQUIREMENTS SPECIFICATION (SRS) & TESTING DOCUMENTATION
## Dự án: Horse Racing Management System
> **Môn học:** Kiểm Chứng Phần Mềm (Software Testing)  
> **Phiên bản:** 3.0  
> **Cập nhật mới nhất:** 2026-08-19  

---

## I. GIỚI THIỆU DỰ ÁN (PROJECT INTRODUCTION)

### 1. Mục đích (Purpose)
Hệ thống **Horse Racing Management System** là một nền tảng web toàn diện giúp **số hóa và tự động hóa** các quy trình quản lý giải đua ngựa, bao gồm: quản lý giải đấu, đăng ký thi đấu, giám sát trọng tài, cá cược trực tuyến và trả thưởng tự động. Dự án được xây dựng trong khuôn khổ môn học Kiểm Chứng Phần Mềm, tập trung vào việc áp dụng các phương pháp kiểm thử phần mềm đa tầng (Unit Test, Integration Test, Manual Test, Security Test, E2E Test, Performance Test).

### 2. Đối tượng sử dụng tài liệu (Intended Audience)
| Đối tượng | Mục đích sử dụng |
|:--|:--|
| Giảng viên hướng dẫn | Đánh giá tiến độ và chất lượng dự án |
| Đội phát triển (4 thành viên) | Tham chiếu yêu cầu khi phát triển và kiểm thử |
| QA / Tester | Đối chiếu yêu cầu để thiết kế test case |

### 3. Phạm vi sản phẩm (Product Scope)
Hệ thống hỗ trợ toàn bộ vòng đời của một giải đua ngựa từ khâu đăng ký đến khi trả thưởng, với **5 vai trò người dùng** phân quyền rõ ràng:
* **Admin (Quản trị viên):** Quản lý toàn bộ hệ thống, người dùng, phân quyền, giải đấu và duyệt đơn đăng ký.
* **Horse Owner (Chủ ngựa):** Quản lý ngựa đua, nài ngựa, đăng ký tham gia giải đấu và nhận thưởng.
* **Jockey (Nài ngựa):** Xem lịch thi đấu và kết quả cá nhân.
* **Referee (Trọng tài):** Điều hành cuộc đua, cập nhật kết quả, ghi nhận vi phạm.
* **Spectator (Khán giả):** Theo dõi giải đấu, đặt cược và nhận thưởng tự động.

### 4. Công nghệ sử dụng (Technology Stack)
| Tầng | Công nghệ | Phiên bản |
|:--|:--|:--|
| Backend | Java Spring Boot, Spring Security (JWT), Spring Data MongoDB | Spring Boot 4.0.6, Java 17+ |
| Frontend | ReactJS, Vite | React 19, Vite 8 |
| Database | MongoDB Atlas (NoSQL) | — |
| Bảo mật | JWT (jjwt), BCrypt, RBAC | jjwt 0.11.5 |
| Kiểm thử | JUnit 5, Mockito, MockMvc, Postman, CodeceptJS + Playwright | — |
| CI/CD | GitHub Actions | — |
| Quản lý dự án | Jira Software (Kanban) | — |

### 5. Thuật ngữ & Từ viết tắt (Definitions & Acronyms)
| Thuật ngữ | Định nghĩa |
|:--|:--|
| SRS | Software Requirements Specification — Đặc tả yêu cầu phần mềm |
| RBAC | Role-Based Access Control — Phân quyền dựa trên vai trò |
| JWT | JSON Web Token — Tiêu chuẩn mã thông báo xác thực stateless |
| BCrypt | Thuật toán mã hóa mật khẩu một chiều |
| CRUD | Create, Read, Update, Delete — Các thao tác cơ bản trên dữ liệu |
| EP | Equivalence Partitioning — Kỹ thuật phân hoạch lớp tương đương |
| BVA | Boundary Value Analysis — Kỹ thuật phân tích giá trị biên |
| E2E | End-to-End Testing — Kiểm thử đầu cuối |
| DTO | Data Transfer Object — Đối tượng truyền dữ liệu giữa các tầng |
| CI/CD | Continuous Integration / Continuous Deployment — Tích hợp & triển khai liên tục |

---

## II. TIẾN ĐỘ THỰC HIỆN & PHÂN CÔNG (PROGRESS & TEAM ASSIGNMENT)

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

## III. ĐẶC TẢ YÊU CẦU CHỨC NĂNG (FUNCTIONAL REQUIREMENTS)

### 1. Phân hệ Xác thực & Phân quyền (Authentication & Authorization)
| Mã | Tên yêu cầu | Mô tả chi tiết |
|:--|:--|:--|
| **FR-AUTH-01** | Đăng ký tài khoản | Cho phép người dùng đăng ký tài khoản mới với các trường: `username`, `password`, `email`, `fullname`, `roleKey`. Mật khẩu được mã hóa BCrypt trước khi lưu. API: `POST /api/auth/register` → `200 OK`. |
| **FR-AUTH-02** | Đăng nhập & JWT Token | Cho phép người dùng đăng nhập bằng `username` và `password`. Hệ thống trả về Bearer JWT Token khi thông tin hợp lệ. API: `POST /api/auth/login`. |
| **FR-AUTH-03** | Phân quyền RBAC | Hệ thống phân quyền truy cập API dựa trên vai trò (Role) chứa trong JWT Token. Public APIs (`/api/auth/**`, `/api/v1/spectator/**`, `/api/v1/horses` GET) cho phép truy cập tự do. Admin APIs (`/api/admin/**`) yêu cầu role `ROLE_ADMIN`. Referee APIs (`/api/referee/**`) yêu cầu xác thực. |

### 2. Phân hệ Quản lý Ngựa đua (Horse Management)
| Mã | Tên yêu cầu | Mô tả chi tiết |
|:--|:--|:--|
| **FR-HORSE-01** | Tạo mới ngựa đua | Chủ ngựa tạo hồ sơ ngựa đua mới với các trường: `name`, `age`, `breed`, `ownerId`. Tuổi ngựa phải nằm trong khoảng **2 – 15 tuổi**. API: `POST /api/v1/horses`. |
| **FR-HORSE-02** | Xem danh sách ngựa | Hiển thị danh sách tất cả ngựa đua. API: `GET /api/v1/horses` (Public). |
| **FR-HORSE-03** | Cập nhật thông tin ngựa | Chủ ngựa cập nhật thông tin ngựa của mình. API: `PUT /api/v1/horses/{id}`. |

### 3. Phân hệ Quản lý Nài ngựa (Jockey Management)
| Mã | Tên yêu cầu | Mô tả chi tiết |
|:--|:--|:--|
| **FR-JOCKEY-01** | Tạo mới nài ngựa | Chủ ngựa đăng ký nài ngựa với các trường: `name`, `licenseNumber`, `experienceYears` (≥ 0), `ownerId`. API: `POST /api/v1/jockeys`. |
| **FR-JOCKEY-02** | Xem danh sách nài ngựa | Hiển thị danh sách nài ngựa. API: `GET /api/v1/jockeys`. |

### 4. Phân hệ Quản lý Giải đấu & Xét duyệt (Tournaments & Admin Approvals)
| Mã | Tên yêu cầu | Mô tả chi tiết |
|:--|:--|:--|
| **FR-TRN-01** | Tạo giải đấu | Admin khởi tạo giải đấu với các trường: `name`, `description`, `startDate`, `endDate`, `status`. Ngày bắt đầu phải trước ngày kết thúc. API: `POST /api/admin/tournaments`. |
| **FR-TRN-02** | Duyệt / Từ chối đăng ký | Admin duyệt (`PUT /api/admin/management/registrations/{id}/approve`) hoặc từ chối (`PUT /api/admin/management/registrations/{id}/reject`) đơn đăng ký tham gia thi đấu. |
| **FR-TRN-03** | Xem đơn đăng ký chờ duyệt | Admin lấy danh sách các đơn đăng ký đang chờ duyệt. API: `GET /api/admin/management/registrations/pending`. |
| **FR-TRN-04** | Quản lý chặng đua (Race) | Admin tạo và quản lý các chặng đua thuộc giải đấu. API: `POST /api/admin/races`, `GET /api/admin/races`. |

### 5. Phân hệ Giám sát Trọng tài (Referee Management)
| Mã | Tên yêu cầu | Mô tả chi tiết |
|:--|:--|:--|
| **FR-REF-01** | Báo cáo trận đấu | Trọng tài ghi nhận báo cáo trận đấu bao gồm: kết quả, sự cố, vi phạm. API: `/api/referee/reports`. |
| **FR-REF-02** | Kiểm tra sức khỏe ngựa | Trọng tài ghi nhận kết quả kiểm tra sức khỏe ngựa trước cuộc đua. API: `/api/referee/health-checks`. |
| **FR-REF-03** | Cập nhật kết quả chặng đua | Cập nhật vị trí, thời gian hoàn thành và tiền thưởng cho từng ngựa trong chặng đua. API: `/api/referee/race-results`. |
| **FR-REF-04** | Ghi nhận vi phạm | Ghi nhận vi phạm (loại ngựa do phạm quy, chấn thương). API: `/api/referee/violations`. |

### 6. Phân hệ Cá cược & Khán giả (Betting & Spectator)
| Mã | Tên yêu cầu | Mô tả chi tiết |
|:--|:--|:--|
| **FR-BET-01** | Đặt cược | Khán giả đặt cược vào một con ngựa cho một chặng đua cụ thể. Số tiền cược phải nằm trong khoảng **$1 – $5,000** mỗi vé cược. API: `POST /api/v1/spectator/bets`. |
| **FR-BET-02** | Xem danh sách cược | Khán giả xem lại các vé cược đã đặt. API: `GET /api/v1/spectator/bets`. |
| **FR-SPEC-01** | Xem lịch đua & kết quả | Khán giả xem danh sách các chặng đua sắp diễn ra và kết quả các chặng đã kết thúc. API: `GET /api/v1/spectator/races`. |

### 7. Phân hệ Trả thưởng (Reward & Prize Distribution)
| Mã | Tên yêu cầu | Mô tả chi tiết |
|:--|:--|:--|
| **FR-RWD-01** | Trả thưởng cá cược tự động | Khi chặng đua kết thúc, hệ thống tự động đối chiếu vé cược và cộng tiền thưởng vào ví người thắng cược. |
| **FR-RWD-02** | Chia tiền thưởng giải đấu | Hệ thống tự động chia quỹ giải thưởng cho các Chủ ngựa dựa trên thứ hạng ngựa đua. |
| **FR-REG-01** | Đăng ký tham gia thi đấu | Chủ ngựa đăng ký ngựa + nài ngựa tham gia một chặng đua thuộc giải đấu. API: `POST /api/v1/registrations`. |

---

## IV. YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS)

### 1. Hiệu năng (Performance)
| Mã | Yêu cầu | Tiêu chí đo lường |
|:--|:--|:--|
| **NFR-PERF-01** | Thời gian phản hồi API | 95% request API phải trả về kết quả trong vòng **2 giây** dưới điều kiện tải bình thường (≤ 50 người dùng đồng thời). |
| **NFR-PERF-02** | Xử lý đồng thời đặt cược | Hệ thống phải xử lý tối thiểu **20 request đặt cược đồng thời** mà không gây mất dữ liệu hoặc tràn số (overflow). |
| **NFR-PERF-03** | Kết nối MongoDB Atlas | Kết nối tới MongoDB Atlas phải ổn định, có khả năng tự động retry khi mất kết nối tạm thời. |

### 2. Bảo mật (Security)
| Mã | Yêu cầu | Tiêu chí đo lường |
|:--|:--|:--|
| **NFR-SEC-01** | Mã hóa mật khẩu | Tất cả mật khẩu người dùng phải được mã hóa bằng BCrypt trước khi lưu vào database. Không bao giờ lưu mật khẩu dạng plain text. |
| **NFR-SEC-02** | Xác thực JWT Stateless | Mọi API (trừ Public APIs) yêu cầu Bearer JWT Token hợp lệ trong header `Authorization`. Token hết hạn hoặc bị sửa đổi phải bị từ chối với `401 Unauthorized`. |
| **NFR-SEC-03** | Phân quyền chính xác | Người dùng đã xác thực nhưng thiếu quyền truy cập phải nhận `403 Forbidden` (không phải `401`). Hệ thống phải phân biệt rõ ràng giữa chưa đăng nhập (401) và không đủ quyền (403). |
| **NFR-SEC-04** | CORS Configuration | API Backend chỉ cho phép các origin được cấu hình trong whitelist truy cập (production). |

### 3. Khả năng sử dụng (Usability)
| Mã | Yêu cầu | Tiêu chí đo lường |
|:--|:--|:--|
| **NFR-USE-01** | Responsive Design | Giao diện hiển thị đúng và dễ sử dụng trên 3 kích thước màn hình: Desktop (≥1024px), Tablet (768-1023px), Mobile (≤767px). |
| **NFR-USE-02** | Hiển thị ngày tháng | Tất cả ngày tháng trên giao diện phải hiển thị đúng định dạng (DD/MM/YYYY HH:mm). Không xuất hiện `Invalid Date` hoặc `NaN`. |
| **NFR-USE-03** | Thông báo lỗi rõ ràng | Khi người dùng nhập dữ liệu sai (cược vượt hạn mức, tuổi ngựa không hợp lệ), hệ thống phải hiển thị thông báo lỗi bằng tiếng Việt, mô tả rõ ràng vấn đề và cách khắc phục. |

### 4. Độ tin cậy (Reliability)
| Mã | Yêu cầu | Tiêu chí đo lường |
|:--|:--|:--|
| **NFR-REL-01** | Tính toàn vẹn dữ liệu cược | Hệ thống đảm bảo không có vé cược nào bị mất hoặc trùng lặp khi có nhiều người đặt cược đồng thời. |
| **NFR-REL-02** | Trả thưởng chính xác | Tiền thưởng phải được tính toán và phân phối chính xác 100% theo kết quả cuộc đua. |

---

## V. KẾT QUẢ BỘ TEST CASE BẢO MẬT API (SECURITY TEST SUITE)

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
| **TC-SEC-01** | **Test phân quyền:** Truy cập API Admin bằng Token của tài khoản có quyền Chủ ngựa / User. | Hệ thống từ chối truy cập và trả về mã lỗi `403 Forbidden` (Xác thực thành công nhưng không đủ quyền). | Hệ thống từ chối truy cập nhưng trả về mã lỗi `401 Unauthorized`. | **FAIL 🔴** | **Bug:** Chặn đúng logic nhưng sai chuẩn HTTP Status Code. Đáng lẽ phải là `403` thay vì `401`. → Xem **BUG-04**. |
| **TC-SEC-02** | **Test phân quyền:** Truy cập API Admin bằng Token của tài khoản có quyền Khán giả (`ROLE_SPECTATOR`). | Hệ thống từ chối truy cập và trả về mã lỗi `403 Forbidden`. | Hệ thống từ chối truy cập nhưng trả về mã lỗi `401 Unauthorized`. | **FAIL 🔴** | **Bug:** Lỗi cấu hình Security tương tự TC-SEC-01. → Xem **BUG-04**. |
| **TC-SEC-03** | **Test xác thực:** Khách vãng lai (Guest) gọi API Admin nhưng không truyền Token vào Header (`No Auth`). | Hệ thống yêu cầu xác thực và trả về mã lỗi `401 Unauthorized`. | Trả về `401 Unauthorized` kèm message lỗi xác thực. | **PASS 🟢** | Hệ thống chặn khách vãng lai thành công. |
| **TC-SEC-04** | **Test xác thực:** Truy cập API Admin bằng một Token bị sửa đổi, fake chữ ký hoặc sai cấu trúc JWT. | Hệ thống phát hiện Token không hợp lệ và trả về mã lỗi `401 Unauthorized`. | Trả về `401 Unauthorized`. | **PASS 🟢** | Hệ thống Validate chữ ký của Token chuẩn xác, chặn lọt thẻ giả. |
| **TC-SEC-05** | **Happy Case:** Truy cập API bằng Token hợp lệ của tài khoản có quyền `ADMIN`. | Trả về HTTP `200 OK` kèm theo danh sách dữ liệu các đơn đăng ký đang chờ duyệt. | Trả về `200 OK` và lấy thành công mảng JSON chứa danh sách chờ. | **PASS 🟢** | API hoạt động đúng luồng chuẩn thiết kế. |

---

## VI. CÁC BUG CÒN TỒN ĐỌNG (OUTSTANDING BUGS)

### Tổng hợp các lỗi được phát hiện qua Sprint 1 – Sprint 3:

| Mã Bug | Tên Bug / Hiện tượng | Mức độ | Yêu cầu liên quan | Trạng thái |
|:---:|:---|:---:|:---:|:---:|
| **BUG-01** | Số dư đặt cược không giới hạn — người dùng có thể đặt $10,000,000, gây vỡ quỹ trả thưởng | 🔴 Critical | FR-BET-01, NFR-REL-01 | 🔧 Chờ fix Sprint 4 |
| **BUG-02** | Tuổi ngựa không có giới hạn logic — cho phép nhập tuổi âm, 0 hoặc 100 | 🟡 Major | FR-HORSE-01 | 🔧 Chờ fix Sprint 4 |
| **BUG-03** | Lỗi hiển thị ngày tháng trên UI: `Invalid day` / `Invalid Date` / `NaN` | 🟡 Major | NFR-USE-02 | 🔧 Chờ fix Sprint 4 |
| **BUG-04** | API trả `401 Unauthorized` thay vì `403 Forbidden` khi user đã đăng nhập nhưng thiếu quyền | 🔴 Critical | NFR-SEC-03, FR-AUTH-03 | 🔧 Chờ fix Sprint 4 |

### Chi tiết Bug #01: Số dư đặt cược không giới hạn (Unbounded Betting Amount)
* **Liên quan:** `FR-BET-01`, `NFR-REL-01`
* **Mô tả:** Cả Backend (`Bet.java`) và Frontend chưa validate hạn mức `minBet = $1` và `maxBet = $5,000`.
* **Giải pháp:** Thêm `@Min(1)` `@Max(5000)` vào `BetDTO.java` + validate ở Frontend form input.

### Chi tiết Bug #02: Tuổi ngựa không giới hạn (Unbounded Horse Age)
* **Liên quan:** `FR-HORSE-01`
* **Mô tả:** `Horse.java` và `HorseDTO.java` chưa có `@Min(2)` `@Max(15)`, giao diện chưa chặn giá trị.
* **Giải pháp:** Thêm annotation validation Backend + ràng buộc `<input min="2" max="15">` ở Frontend.

### Chi tiết Bug #03: Lỗi hiển thị ngày tháng (Invalid Date Display)
* **Liên quan:** `NFR-USE-02`
* **Mô tả:** Frontend gọi `new Date(item.date)` với giá trị `null`/`undefined` hoặc sai tên thuộc tính DTO.
* **Giải pháp:** Tạo helper `formatDateTime()` an toàn + Backend thêm `@JsonFormat` chuẩn ISO 8601.

### Chi tiết Bug #04: Sai HTTP Status Code khi từ chối quyền (Authorization Mismatch)
* **Liên quan:** `TC-SEC-01`, `TC-SEC-02`, `NFR-SEC-03`
* **Mô tả:** `SecurityConfig.java` chỉ cấu hình `AuthenticationEntryPoint` (401) mà thiếu `AccessDeniedHandler` (403).
* **Giải pháp:**
  ```java
  .exceptionHandling(exception -> exception
      .authenticationEntryPoint((request, response, authException) -> 
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Authentication token is missing or invalid"))
      .accessDeniedHandler((request, response, accessDeniedException) -> 
          response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: You do not have permission to access this resource"))
  )
  ```

---

## VII. DỰ KIẾN CÔNG VIỆC SPRINT 4 & SPRINT 5 (UPCOMING SPRINT PLAN)

### Sprint 4 — Mục tiêu: Fix Bug + Mở rộng kiểm thử + Docker

| Mã Jira | Công việc | Phụ trách | Yêu cầu liên quan |
|:---:|:---|:---|:---:|
| K4-1 | Fix BUG-04: Phân biệt 401 vs 403 trong `SecurityConfig.java` | Lê Đức Thiện | NFR-SEC-03 |
| K4-2 | Fix BUG-01 (Giới hạn cược $1-$5,000) & BUG-02 (Tuổi ngựa 2-15) | Lê Đức Thiện | FR-BET-01, FR-HORSE-01 |
| K4-3 | Unit Test: `HorseService`, `JockeyService`, `TournamentService` | Lê Đức Thiện | FR-HORSE-*, FR-JOCKEY-*, FR-TRN-* |
| K4-4 | Cài đặt SonarQube & Scan Backend | Lê Đức Thiện | NFR-SEC-04 |
| K4-5 | Postman Assertions tự động cho 35 endpoints | Nguyễn Thanh Phú | Toàn bộ FR |
| K4-6 | Integration Test: `HorseController`, `JockeyController`, `TournamentController` | Nguyễn Thanh Phú | FR-HORSE-*, FR-JOCKEY-*, FR-TRN-* |
| K4-7 | Chuẩn hóa JSON Date DTO (hỗ trợ BUG-03) | Nguyễn Thanh Phú | NFR-USE-02 |
| K4-8 | Viết Dockerfile Backend Spring Boot | Nguyễn Thanh Phú | — |
| K4-9 | Fix BUG-03 (`formatDateTime` helper) & ràng buộc form UI (BUG-01, 02) | Đỗ Cao Nhất | NFR-USE-02, FR-BET-01, FR-HORSE-01 |
| K4-10 | Tối ưu UI/UX Responsive (Mobile/Tablet/Desktop) | Đỗ Cao Nhất | NFR-USE-01 |
| K4-11 | CodeceptJS + Playwright: E2E Test Login/Register | Đỗ Cao Nhất | FR-AUTH-01, FR-AUTH-02 |
| K4-12 | Viết Dockerfile Frontend (Multi-stage Nginx) | Đỗ Cao Nhất | — |
| K4-13 | Mở rộng Security Regression Suite `@WithMockUser` | Chu Trần Duy Hoàng | NFR-SEC-02, NFR-SEC-03 |
| K4-14 | Thiết kế Test Case BVA cho hạn mức cược & tuổi ngựa | Chu Trần Duy Hoàng | FR-BET-01, FR-HORSE-01 |
| K4-15 | Phân tích Security Hotspots trên SonarQube | Chu Trần Duy Hoàng | NFR-SEC-04 |
| K4-16 | Quản lý vòng đời Bug trên Jira Board | Chu Trần Duy Hoàng | — |

### Sprint 5 — Mục tiêu: Final Testing + Docker Compose + Báo cáo tổng kết

| Phụ trách | Công việc dự kiến |
|:---|:---|
| **Lê Đức Thiện** | Fix lỗi từ SonarQube report, Unit Test bổ sung, tạo `docker-compose.yml`. |
| **Nguyễn Thanh Phú** | Test kịch bản lỗi biên Postman, Integration Test quy trình trả thưởng, xuất báo cáo Newman. |
| **Đỗ Cao Nhất** | E2E Test luồng Owner & Admin, kiểm thử hộp đen xuất file Excel. |
| **Chu Trần Duy Hoàng** | Final Security Audit, Load Test hiệu năng, tổng kết slide báo cáo Tuần 8. |
