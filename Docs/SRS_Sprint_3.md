# SRS Sprint 3 — Horse Racing Management System

> **Software Requirements Specification — Báo cáo Sprint 3**  
> **Nhóm thực hiện:** ToriToriisme  
> **Cập nhật:** 2026-08-19  

---

## 👥 1. Thành Viên & Phân Vai (Team Roles)

| STT | Họ và Tên | MSSV | Vai trò chính | Branch |
|:---:|:---|:---:|:---|:---|
| 1 | **Lê Đức Thiện** | — | **Backend & Unit Test** | `thien` |
| 2 | **Nguyễn Thanh Phú** | `075205013708` | **API & Integration Test** | `Nguyen-Thanh-Phu` |
| 3 | **Đỗ Cao Nhất** | `082205001294` | **Frontend & Manual Test** | `Do-Cao-Nhat` |
| 4 | **Chu Trần Duy Hoàng** | `066206012384` | **Security & QA Workflow** | `Hoang` |

---

## 2. Tổng Kết Sprint 1 & Sprint 2

* **Sprint 1**: Setup kiến trúc Backend Spring Boot + MongoDB Atlas, Frontend ReactJS + Vite, JWT stateless security, Postman Collection 35 endpoints, quy trình Jira Kanban.
* **Sprint 2**:
  * **Lê Đức Thiện**: Unit Test AuthController & UserServiceImpl.
  * **Nguyễn Thanh Phú**: Integration Test + Postman Automated Assertions.
  * **Đỗ Cao Nhất**: Manual Blackbox Testing & Kịch bản Test Case Excel.
  * **Chu Trần Duy Hoàng**: Security Test `@WithMockUser`, Performance Report (4 PDF), thiết lập CI/CD Pipeline.

---

## 3. Sprint 3 — Chi Tiết Công Việc Đã Thực Hiện

* **Nguyễn Thanh Phú**:
  * Viết 3 bộ Integration Test Workflow: `RefereeWorkflowIntegrationTest.java` (~500 dòng), `RewardWorkflowIntegrationTest.java` (~765 dòng), `TournamentRegistrationWorkflowIntegrationTest.java` (~674 dòng).
  * Cải thiện giao diện `RefereeDashboard.jsx` (+192 dòng code).
* **Chu Trần Duy Hoàng**:
  * Cập nhật `SecurityRegressionSuiteTest.java`.
  * Tích hợp và merge các kịch bản kiểm thử bảo mật vào `KCPM-Sprint_2`.

---

## 4. Yêu Cầu Chức Năng Sprint 3 (Functional Requirements)

Các yêu cầu chức năng được kiểm thử và phát triển trong Sprint 3:

| Mã FR (SRS tổng) | Mô tả yêu cầu | Loại kiểm thử Sprint 3 | Người phụ trách |
|:--|:--|:--|:--|
| **FR-REF-01** | Trọng tài ghi nhận báo cáo trận đấu | Integration Test (`RefereeWorkflowIntegrationTest`) | Nguyễn Thanh Phú |
| **FR-REF-02** | Trọng tài kiểm tra sức khỏe ngựa | Integration Test (`RefereeWorkflowIntegrationTest`) | Nguyễn Thanh Phú |
| **FR-REF-03** | Cập nhật kết quả chặng đua | Integration Test (`RefereeWorkflowIntegrationTest`) | Nguyễn Thanh Phú |
| **FR-REF-04** | Ghi nhận vi phạm | Integration Test (`RefereeWorkflowIntegrationTest`) | Nguyễn Thanh Phú |
| **FR-RWD-01** | Trả thưởng cá cược tự động | Integration Test (`RewardWorkflowIntegrationTest`) | Nguyễn Thanh Phú |
| **FR-RWD-02** | Chia tiền thưởng giải đấu | Integration Test (`RewardWorkflowIntegrationTest`) | Nguyễn Thanh Phú |
| **FR-REG-01** | Đăng ký tham gia thi đấu | Integration Test (`TournamentRegistrationWorkflowIntegrationTest`) | Nguyễn Thanh Phú |
| **FR-AUTH-03** | Phân quyền RBAC | Security Regression Test (`SecurityRegressionSuiteTest`) | Chu Trần Duy Hoàng |

---

## 5. Yêu Cầu Phi Chức Năng Sprint 3 (Non-Functional Requirements)

Các yêu cầu phi chức năng được kiểm thử trong Sprint 3:

| Mã NFR (SRS tổng) | Mô tả yêu cầu | Phương pháp kiểm thử | Kết quả |
|:--|:--|:--|:--:|
| **NFR-SEC-02** | Xác thực JWT Stateless — Token hết hạn/sửa đổi bị từ chối | `SecurityRegressionSuiteTest` với `@WithMockUser` | ✅ PASS |
| **NFR-SEC-03** | Phân quyền chính xác 401 vs 403 | Security Regression Test | 🔴 FAIL → BUG-04 |
| **NFR-REL-01** | Tính toàn vẹn dữ liệu cược | `RewardWorkflowIntegrationTest` — kiểm tra tính toán trả thưởng | ✅ PASS |

---

## 6. Các Lỗi Nghiệp Vụ & Kỹ Thuật Đã Ghi Nhận (Bug Report Sprint 3)

| Mã Bug | Mô tả lỗi | Mức độ | Người phụ trách | NFR/FR liên quan |
|:---:|:---|:---:|:---|:---|
| **BUG-01** | Số dư đặt cược không giới hạn (đánh cược $10M làm nhà cái phá sản) | 🔴 Critical | **Lê Đức Thiện** (BE) + **Đỗ Cao Nhất** (FE) | FR-BET-01, NFR-REL-01 |
| **BUG-02** | Tuổi của ngựa không có giới hạn logic (chưa chặn 2 - 15 tuổi) | 🟡 Major | **Lê Đức Thiện** (BE) + **Đỗ Cao Nhất** (FE) | FR-HORSE-01 |
| **BUG-03** | Lỗi hiển thị ngày tháng: Hiện `Invalid day` / `Invalid Date` | 🟡 Major | **Đỗ Cao Nhất** (FE) + **Nguyễn Thanh Phú** (BE) | NFR-USE-02 |
| **BUG-04** | API trả `401 Unauthorized` thay vì `403 Forbidden` khi thiếu quyền | 🔴 Critical | **Lê Đức Thiện** (BE) + **Chu Trần Duy Hoàng** (QA) | NFR-SEC-03, FR-AUTH-03 |

---

## 7. Định Hướng & Tiền Đề Cho Sprint 4

1. **Kiểm thử toàn diện**: Bổ sung Unit Test (Thiện), Integration Test (Phú), E2E Test CodeceptJS (Nhất), Security Regression (Hoàng).
2. **Khắc phục triệt để 4 Bugs**: Đảm bảo toàn bộ test case kiểm thử giá trị biên và bảo mật đều PASS.
3. **Chuẩn bị Dockerization**: Tạo Dockerfile cho Backend và Frontend để sẵn sàng cho tuần 7.
