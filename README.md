# Horse Racing Management System – Kiểm Thử Phần Mềm

> Tài liệu dành cho môn **Kiểm Thử Phần Mềm** — bao gồm link Postman để kiểm thử API và bản tóm tắt SRS (Software Requirements Specification) của dự án.

---

## 📮 Postman – Kiểm thử API

### Tham gia Team Postman (dành cho 5 thành viên trong nhóm)

> Nhấn vào link bên dưới để tham gia vào team Postman của nhóm. Link này dành cho **5 thành viên** được cấp quyền tham gia event kiểm thử:

🔗 **[Tham gia Team Postman](https://app.getpostman.com/join-team?invite_code=a9f54de6a230b02c97be7b3e67d5e49f60f6ef96204f3833021f4aab3962083a&target_code=a91c4f68e354d9c1a25ae93af4e1538f)**

### Xem Postman Workspace (dành cho giảng viên)

> Link bên dưới cho phép giảng viên xem toàn bộ Workspace Postman của nhóm (bao gồm Collections, Environments, API Tests, ...):

🔗 **[Xem Postman Workspace](https://toritoriisme-5288079.postman.co/workspace/80df1546-c720-43b8-84a5-b957db87f7f2)**

---

## 📋 Tóm tắt SRS (Software Requirements Specification)

### 1. Giới thiệu

| Mục | Mô tả |
|-----|-------|
| **Tên dự án** | Horse Racing Management System |
| **Mục đích** | Xây dựng nền tảng web quản lý giải đua ngựa toàn diện, hỗ trợ tổ chức giải đấu, đăng ký thi đấu, điều hành cuộc đua, cá cược và trả thưởng tự động |
| **Phạm vi** | Hệ thống bao gồm Backend REST API (Spring Boot) và Frontend SPA (ReactJS + Vite), kết nối với MongoDB |
| **Đối tượng sử dụng** | Admin, Horse Owner (Chủ ngựa), Referee (Trọng tài), Spectator (Khán giả) |

### 2. Yêu cầu chức năng (Functional Requirements)

#### 2.1. Module Xác thực & Phân quyền (Authentication & Authorization)

| ID | Chức năng | Mô tả |
|----|-----------|-------|
| FR-01 | Đăng ký tài khoản | Người dùng đăng ký với username, email, password, fullName và chọn vai trò (Horse Owner / Referee / Spectator). Không cho phép đăng ký vai trò Admin |
| FR-02 | Đăng nhập | Xác thực bằng email + password, trả về JWT Token cùng thông tin user (id, role, permissions, balance) |
| FR-03 | Xem thông tin cá nhân | API `/api/auth/me` trả về profile của user đang đăng nhập |
| FR-04 | Cập nhật hồ sơ | Cho phép user đổi họ tên, email (kiểm tra trùng lặp) |
| FR-05 | Đổi mật khẩu | Yêu cầu nhập mật khẩu cũ chính xác trước khi đổi mật khẩu mới |
| FR-06 | Phân quyền RBAC | Hệ thống Role → Permission. Mỗi Role có tập Permission riêng, được kiểm tra bằng `@PreAuthorize` |

#### 2.2. Module Quản lý Giải đấu (Tournament Management) – *Admin*

| ID | Chức năng | Mô tả |
|----|-----------|-------|
| FR-07 | CRUD Giải đấu | Tạo, xem, sửa, xóa giải đấu với thông tin: tên, mô tả, ngày bắt đầu, ngày kết thúc, trạng thái (`UPCOMING`, `ONGOING`, `COMPLETED`) |
| FR-08 | Đăng ký ngựa vào giải đấu | Horse Owner đăng ký ngựa vào giải đấu thông qua API `/api/admin/tournaments/{id}/register` |
| FR-09 | Vòng loại tự động | API `/api/admin/tournaments/advance/{raceId}` tự động chọn các ngựa đạt thành tích tốt nhất để tiến vào vòng tiếp theo |

#### 2.3. Module Quản lý Vòng đua (Race Management) – *Admin*

| ID | Chức năng | Mô tả |
|----|-----------|-------|
| FR-10 | CRUD Vòng đua | Tạo, xem, sửa, xóa vòng đua thuộc một giải đấu, bao gồm: tên, giờ bắt đầu, cự ly, trạng thái (`SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), số ngựa vượt qua vòng (`advancingCount`) |
| FR-11 | Phân công trọng tài | Admin phân công Referee cho từng vòng đua cụ thể qua API `/api/admin/management/races/{raceId}/assign-referee/{refereeId}` |
| FR-12 | Duyệt đơn đăng ký | Admin duyệt hoặc từ chối đơn đăng ký tham gia đua (đã được Jockey/Owner APPROVED) — trạng thái `adminStatus` |

#### 2.4. Module Quản lý Ngựa & Nài (Horse & Jockey Management) – *Horse Owner*

| ID | Chức năng | Mô tả |
|----|-----------|-------|
| FR-13 | Quản lý ngựa đua | CRUD ngựa (tên, tuổi, giống) — mỗi ngựa thuộc về 1 Owner (`ownerId`) |
| FR-14 | Quản lý nài ngựa | CRUD nài (tên, số giấy phép, năm kinh nghiệm, đánh giá) — mỗi Jockey liên kết với 1 User |
| FR-15 | Đăng ký thi đấu | Owner đăng ký ngựa + nài vào vòng đua cụ thể, tạo bản ghi `Registration` với trạng thái duyệt 2 lớp (Jockey → Admin) |

#### 2.5. Module Trọng tài (Referee Operations)

| ID | Chức năng | Mô tả |
|----|-----------|-------|
| FR-16 | Xem vòng đua được phân công | Referee xem danh sách các vòng đua mà mình được giao nhiệm vụ |
| FR-17 | Xem chi tiết & danh sách ngựa | Xem thông tin chi tiết vòng đua và danh sách ngựa tham gia kèm jockey |
| FR-18 | Kiểm tra sức khỏe ngựa | Ghi nhận tình trạng sức khỏe ngựa trước cuộc đua (`HEALTHY`, `INJURED`, `EXHAUSTED`, `UNFIT`) |
| FR-19 | Ghi nhận kết quả đua | Tạo và cập nhật kết quả đua cho từng cặp ngựa-jockey (vị trí, thời gian, tiền thưởng) |
| FR-20 | Ghi nhận vi phạm | Lập biên bản vi phạm (loại vi phạm, mô tả, mức phạt, mức nghiêm trọng: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) |
| FR-21 | Viết báo cáo trọng tài | Tạo và cập nhật báo cáo sau mỗi vòng đua, hỗ trợ Optimistic Locking (`@Version`) |

#### 2.6. Module Khán giả & Cá cược (Spectator & Betting)

| ID | Chức năng | Mô tả |
|----|-----------|-------|
| FR-22 | Xem danh sách cuộc đua | Khán giả xem tất cả cuộc đua hoặc chỉ các cuộc đua đang diễn ra/sắp diễn ra |
| FR-23 | Nạp tiền ví điện tử | Nạp tiền vào wallet (cộng trực tiếp vào `balance` của User) |
| FR-24 | Đặt cược | Đặt cược vào con ngựa yêu thích với số tiền và vị trí dự đoán. Hệ thống trừ tiền từ ví ngay lập tức |
| FR-25 | Lịch sử cá cược | Xem toàn bộ lịch sử cược (trạng thái: `PENDING`, `WON`, `LOST`) |
| FR-26 | Xem số dư ví | Kiểm tra số dư hiện tại trong ví điện tử |

#### 2.7. Module Trả thưởng tự động (Auto Reward System)

| ID | Chức năng | Mô tả |
|----|-----------|-------|
| FR-27 | Tính thưởng cuộc đua | Khi cuộc đua kết thúc, gọi API `/api/v1/rewards/calculate/{raceId}` để tự động tính tiền thưởng cho Owner (dựa trên vị trí ngựa) và trả thưởng cho Spectator (đối chiếu vé cược) |

### 3. Yêu cầu phi chức năng (Non-Functional Requirements)

| ID | Loại | Yêu cầu |
|----|------|---------|
| NFR-01 | **Bảo mật** | Xác thực Stateless bằng JWT, mã hóa mật khẩu BCrypt, phân quyền đa lớp RBAC với `@PreAuthorize` và `@EnableMethodSecurity` |
| NFR-02 | **Hiệu năng** | Sử dụng MongoDB NoSQL cho khả năng mở rộng linh hoạt, Optimistic Locking (`@Version`) chống xung đột dữ liệu đồng thời |
| NFR-03 | **Khả dụng** | CORS mở cho tất cả origin (cấu hình cho development), Session Stateless không lưu trạng thái trên server |
| NFR-04 | **Giao diện** | Frontend SPA sử dụng ReactJS 19 + Vite 8, quản lý state bằng Redux Toolkit, routing bằng React Router v7 |
| NFR-05 | **Tương thích** | Backend chạy trên Java 26, Spring Boot 4.0.6. Frontend tương thích các trình duyệt hiện đại |

### 4. Mô hình dữ liệu (Data Entities)

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│     User     │────▶│     Role     │────▶│  Permission  │
│  (users)     │ @DBRef (roles)    │ @DBRef (permissions)│
├──────────────┤     ├──────────────┤     ├──────────────┤
│ id           │     │ id           │     │ id           │
│ username     │     │ title        │     │ title        │
│ password     │     │ key          │     │ key          │
│ email        │     │ permissions[]│     └──────────────┘
│ fullName     │     └──────────────┘
│ balance      │
│ role         │
│ status       │
└──────────────┘

┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Tournament  │────▶│    Race      │────▶│ Registration │
│ (tournaments)│     │   (races)    │     │(registrations│
├──────────────┤     ├──────────────┤     ├──────────────┤
│ id           │     │ id           │     │ id           │
│ name         │     │ tournamentId │     │ raceId       │
│ description  │     │ name         │     │ horseId      │
│ startDate    │     │ startTime    │     │ jockeyId     │
│ endDate      │     │ distance     │     │ regDate      │
│ status       │     │ status       │     │ status       │
└──────────────┘     │ refereeId    │     │ adminStatus  │
                     │ advancingCnt │     └──────────────┘
                     └──────────────┘

┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│    Horse     │     │   Jockey     │     │  RaceResult  │
│  (horses)    │     │  (jockeys)   │     │(race_results)│
├──────────────┤     ├──────────────┤     ├──────────────┤
│ id           │     │ id           │     │ id           │
│ name         │     │ name         │     │ raceId       │
│ age          │     │ licenseNum   │     │ horseId      │
│ breed        │     │ expYears     │     │ jockeyId     │
│ ownerId      │     │ rating       │     │ position     │
└──────────────┘     │ userId       │     │ finishTime   │
                     └──────────────┘     │ prizeMoney   │
                                          └──────────────┘

┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│     Bet      │     │  Violation   │     │RefereeReport │
│   (bets)     │     │ (violations) │     │(ref_reports) │
├──────────────┤     ├──────────────┤     ├──────────────┤
│ id           │     │ id           │     │ id           │
│ spectatorId  │     │ raceId       │     │ raceId       │
│ raceId       │     │ horseId      │     │ refereeId    │
│ horseId      │     │ jockeyId     │     │ reportText   │
│ amount       │     │ violationType│     │ createdAt    │
│ predictedPos │     │ description  │     └──────────────┘
│ status       │     │ penalty      │
│ payout       │     │ severity     │
└──────────────┘     │ refereeId    │
                     └──────────────┘

┌──────────────────┐
│ HorseHealthCheck │
│(horse_health_chk)│
├──────────────────┤
│ id               │
│ horseId          │
│ raceId           │
│ status           │
│ notes            │
│ checkedBy        │
│ approved         │
└──────────────────┘
```

### 5. Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                        │
│  ReactJS 19 + Vite 8 + Redux Toolkit + React Router v7 │
│  (SPA - Single Page Application)                        │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP (Axios)
                       ▼
┌─────────────────────────────────────────────────────────┐
│                   API GATEWAY LAYER                     │
│            Spring Boot 4.0.6 REST API                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ AuthController│  │TournamentCtrl│  │ RaceController│  │
│  │ UserController│  │ RefereeCtrl  │  │SpectatorCtrl │  │
│  │ RewardCtrl   │  │ AdminCtrl    │  │ HorseCtrl    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
├─────────────────────────────────────────────────────────┤
│                  SECURITY LAYER                         │
│  JWT Filter → Token Provider → UserDetailsService       │
│  BCrypt Password Encoding + RBAC (Role-Permission)      │
├─────────────────────────────────────────────────────────┤
│                  SERVICE LAYER                          │
│  Business Logic (Reward Calculation, Bet Settlement,    │
│  Tournament Advancement, Health Check, Violation Mgmt)  │
├─────────────────────────────────────────────────────────┤
│                 REPOSITORY LAYER                        │
│           Spring Data MongoDB Repositories              │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│                  DATABASE LAYER                         │
│               MongoDB (NoSQL)                           │
│  Collections: users, roles, permissions, horses,        │
│  jockeys, tournaments, races, registrations,            │
│  race_results, bets, violations, referee_reports,       │
│  horse_health_checks                                    │
└─────────────────────────────────────────────────────────┘
```

### 6. API Endpoints tổng hợp

| Nhóm | Method | Endpoint | Mô tả |
|------|--------|----------|-------|
| **Auth** | POST | `/api/auth/login` | Đăng nhập |
| | POST | `/api/auth/register` | Đăng ký |
| | GET | `/api/auth/me` | Lấy thông tin user hiện tại |
| | PUT | `/api/auth/me` | Cập nhật hồ sơ |
| | PUT | `/api/auth/me/password` | Đổi mật khẩu |
| **Tournament** | GET/POST | `/api/admin/tournaments` | Lấy DS / Tạo giải đấu |
| | GET/PUT/DELETE | `/api/admin/tournaments/{id}` | Xem / Sửa / Xóa |
| | POST | `/api/admin/tournaments/{id}/register` | Đăng ký ngựa vào giải |
| | POST | `/api/admin/tournaments/advance/{raceId}` | Vòng loại tự động |
| **Race** | GET/POST | `/api/admin/races` | Lấy DS / Tạo vòng đua |
| | GET/PUT/DELETE | `/api/admin/races/{id}` | Xem / Sửa / Xóa |
| | GET | `/api/admin/races/tournament/{tournamentId}` | Lấy vòng đua theo giải |
| **Admin** | GET | `/api/admin/management/registrations/pending` | DS đơn chờ duyệt |
| | PUT | `/api/admin/management/registrations/{id}/approve` | Duyệt đơn |
| | PUT | `/api/admin/management/registrations/{id}/reject` | Từ chối đơn |
| | GET | `/api/admin/management/referees` | DS trọng tài |
| | PUT | `/api/admin/management/races/{raceId}/assign-referee/{refereeId}` | Phân công trọng tài |
| **Referee** | GET | `/api/referee/{refereeId}/assigned-races` | Vòng đua được phân công |
| | GET | `/api/referee/race/{raceId}/details` | Chi tiết vòng đua |
| | GET | `/api/referee/race/{raceId}/horses` | DS ngựa trong vòng đua |
| | POST | `/api/referee/{refereeId}/report` | Tạo báo cáo |
| | PUT | `/api/referee/report/{reportId}` | Sửa báo cáo |
| | GET | `/api/referee/race/{raceId}/reports` | Báo cáo theo vòng đua |
| | POST | `/api/referee/race/{raceId}/result` | Ghi kết quả đua |
| | PUT | `/api/referee/result/{resultId}` | Sửa kết quả |
| | GET | `/api/referee/race/{raceId}/results` | Kết quả theo vòng đua |
| | POST | `/api/referee/race/{raceId}/violation` | Ghi vi phạm |
| | PUT | `/api/referee/violation/{violationId}` | Sửa vi phạm |
| | GET | `/api/referee/race/{raceId}/violations` | Vi phạm theo vòng đua |
| | GET | `/api/referee/horse/{horseId}/health/race/{raceId}` | Kiểm tra sức khỏe ngựa |
| | GET | `/api/referee/race/{raceId}/health-check/all` | Sức khỏe toàn bộ ngựa |
| **Spectator** | GET | `/api/v1/spectator/races` | Tất cả cuộc đua |
| | GET | `/api/v1/spectator/races/live` | Cuộc đua đang diễn ra |
| | POST | `/api/v1/spectator/bets` | Đặt cược |
| | GET | `/api/v1/spectator/bets/history/{spectatorId}` | Lịch sử cược |
| | POST | `/api/v1/spectator/wallet/top-up/{spectatorId}` | Nạp tiền |
| | GET | `/api/v1/spectator/wallet/{spectatorId}` | Xem số dư |
| **Reward** | POST | `/api/v1/rewards/calculate/{raceId}` | Tính thưởng tự động |
