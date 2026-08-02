# Horse Racing Management System (Hệ Thống Quản Lý Đua Ngựa)

Dự án Hệ thống Quản lý Đua ngựa là một nền tảng toàn diện giúp số hóa và tự động hóa các quy trình quản lý giải đua, ngựa đua, nài ngựa và tổ chức cá cược. Hệ thống được thiết kế với nhiều phân quyền rõ ràng, hỗ trợ toàn bộ vòng đời của một giải đua từ khâu đăng ký đến khi trả thưởng.

---

## Chức năng cốt lõi theo Phân quyền (Roles)

Hệ thống được thiết kế với 4 vai trò chính, mỗi vai trò có giao diện và quyền hạn riêng biệt:

### 1. Admin (Quản trị viên)
- Quản lý toàn bộ hệ thống, người dùng và phân quyền (RBAC).
- Quản lý các Giải đấu (Tournaments) và các Vòng đua (Races).
- Duyệt đơn đăng ký tham gia đua của các Chủ ngựa.

### 2. Horse Owner (Chủ Ngựa)
- Quản lý danh sách Ngựa đua (Horses) và Nài ngựa (Jockeys) của riêng mình.
- Đăng ký cho Ngựa tham gia vào các Giải đấu/Vòng đua.
- Theo dõi lịch thi đấu và nhận tiền thưởng tự động nếu ngựa chiến thắng.

### 3. Referee (Trọng tài)
- Điều hành trực tiếp vòng đua (Bắt đầu, Tạm dừng, Kết thúc).
- Cập nhật kết quả đua theo thời gian thực.
- Đánh dấu các sự cố (Loại ngựa do phạm quy, chấn thương).

### 4. Spectator (Khán giả)
- Theo dõi lịch đua, thông tin ngựa và kết quả đua.
- Nạp tiền vào ví điện tử (Wallet).
- Đặt cược (Betting) vào các con ngựa yêu thích và nhận thưởng tự động nếu đoán trúng.

---

## Tính năng nổi bật
- **Hệ thống cá cược tự động (Auto Reward Calculation):** Tự động đối chiếu vé cược và cộng tiền thưởng vào ví người thắng cược ngay khi cuộc đua kết thúc.
- **Tự động chia tiền thưởng (Prize Pool Distribution):** Tự động chia tiền quỹ giải thưởng cho các Chủ ngựa dựa trên thứ hạng ngựa đua.
- **Bảo mật chặt chẽ:** Tích hợp JSON Web Token (JWT) và bảo mật phân quyền đa lớp (Role-Based Access Control) đảm bảo an toàn dữ liệu tuyệt đối.

---

## Công nghệ sử dụng
- **Backend:** Java Spring Boot, Spring Security (JWT), Spring Data MongoDB.
- **Database:** MongoDB (NoSQL) cho khả năng lưu trữ linh hoạt.
- **Frontend:** ReactJS, Vite.

---

# Hướng dẫn cài đặt dự án

## 1. Quy tắc làm việc
* Khi bắt đầu làm việc, hãy nhớ tạo nhánh (branch) riêng theo định dạng: `hoten_mssv`

## 2. Cài đặt Frontend
* Bước 1: `cd` vào thư mục chứa code Frontend (`frontend-horse-racing-management`).
* Bước 2: Chạy lệnh `npm install` để cài đặt các thư viện cần thiết.
* Bước 3: Sau khi cài xong, chạy lệnh `npm start` hoặc `npm run dev` để khởi chạy ứng dụng.

## 3. Cài đặt Backend (Spring Boot)
* Bước 1: Mở thư mục `backend-horse-racing-management` bằng IntelliJ IDEA hoặc Eclipse.
* Bước 2: IDE sẽ tự động nhận diện file `pom.xml` (Maven) và tải các dependencies cần thiết.
* Bước 3: Đảm bảo MongoDB đang chạy ở Localhost (Cổng mặc định `27017`).
* Bước 4: Khởi chạy file Application để bật máy chủ.
