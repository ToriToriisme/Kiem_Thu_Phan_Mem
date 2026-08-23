// ============================================================
// K2-38: Tự động hoá lại kịch bản E2E Sprint 3 bằng CodeceptJS + Playwright
//
// Luồng: Owner đăng ký ngựa+jockey vào giải → Jockey duyệt →
// Admin duyệt đơn + gán Referee → Spectator đặt cược →
// Referee lập kết quả + biên bản + vi phạm + Chốt & Chia Thưởng →
// kiểm tra ví Spectator tăng lên.
//
// TRƯỚC KHI CHẠY:
// 1. Backend (IntelliJ, port 8080) VÀ frontend (npm run dev, port 5173)
//    phải đang chạy song song.
// 2. Sửa DATA bên dưới cho khớp với dữ liệu thật đang có trên hệ thống
//    (tên giải đấu phải đang ở trạng thái nhận đăng ký, có ít nhất
//    1 chặng đua ở trạng thái SCHEDULED để đặt cược được).
//
// CÁCH CHẠY: mở terminal tại frontend-horse-racing-management, gõ:
//   npx codeceptjs run --steps
// ============================================================

Feature('Sprint 3 - Kịch bản E2E đầy đủ 5 vai trò');

// Tài khoản test đã tạo sẵn (dùng lại từ Sprint 1-3)
const ACCOUNTS = {
  owner: { email: 'owner_test1@gmail.com', password: 'hai12345' },
  jockey: { email: 'jockey_test1@gmail.com', password: 'jockey12345' },
  admin: { email: 'admin_test1@gmail.com', password: 'admin12345' },
  referee: { email: 'referee_test1@gmail.com', password: 'referee12345' },
  spectator: { email: 'ba_test1@gmail.com', password: 'ba12345' },
};

// Dữ liệu test - ĐỔI LẠI cho khớp với dữ liệu thật trên hệ thống của bạn
// nếu tên giải/ngựa/jockey/trọng tài hiện tại khác đi
const DATA = {
  tournamentName: 'Giải Cuối Kỳ',
  horseName: 'Test Horse',
  jockeyName: 'Trần Ba',
  refereeName: 'Phạm Tư',
  betAmount: '10000',
  resultPosition: '1',
  resultFinishTime: '45.25',
  violationDesc: 'Ngựa xuất phát trước hiệu lệnh',
  reportText: 'Cuộc đua diễn ra công bằng, không có tranh chấp lớn.',
};

// Chuyển sang tài khoản khác: xoá token cũ trong localStorage trước
// (an toàn hơn bấm nút Đăng xuất vì không phụ thuộc UI có đổi hay không)
const loginAs = (I, account) => {
  I.amOnPage('/login');
  I.executeScript(() => localStorage.clear());
  I.amOnPage('/login');
  I.fillField('input[name="email"]', account.email);
  I.fillField('input[name="password"]', account.password);
  I.click('Đăng Nhập');
  I.wait(2);
};

// Chuyển text "123.456 VND" -> số nguyên 123456, để so sánh số dư ví
const parseVND = (text) => parseInt(String(text).replace(/[^\d]/g, ''), 10);

Scenario('E2E: Owner đăng ký → Jockey duyệt → Admin duyệt+gán Referee → Spectator đặt cược → Referee lập kết quả+chia thưởng', async ({ I }) => {

  // ================= BƯỚC 1: OWNER đăng ký ngựa + jockey vào giải =================
  loginAs(I, ACCOUNTS.owner);
  I.amOnPage('/profile/owner-dashboard');
  I.waitForText('Tham gia giải đấu', 10);
  I.click('Tham gia giải đấu');

  I.waitForElement('//label[contains(text(),"Chọn Giải Đấu")]/following-sibling::select', 5);
  I.selectOption('//label[contains(text(),"Chọn Giải Đấu")]/following-sibling::select', new RegExp(DATA.tournamentName));
  I.selectOption('//label[contains(text(),"Chọn Ngựa")]/following-sibling::select', new RegExp(DATA.horseName));
  I.selectOption('//label[contains(text(),"Chọn Nài Ngựa")]/following-sibling::select', new RegExp(DATA.jockeyName));
  I.click('Gửi lời mời');
  I.wait(1);

  // ================= BƯỚC 2: JOCKEY duyệt lời mời =================
  loginAs(I, ACCOUNTS.jockey);
  I.amOnPage('/profile/jockey-dashboard');
  I.waitForText('Lời mời thi đấu mới', 10);
  I.click('Chấp nhận');
  I.wait(1);

  // ================= BƯỚC 3: ADMIN duyệt đơn + gán Referee =================
  loginAs(I, ACCOUNTS.admin);
  I.amOnPage('/admin/approval-dashboard');
  I.waitForText('Duyệt đơn đăng ký', 10);
  I.click('Duyệt');
  // Popup xác nhận ở đây là SweetAlert2 (1 khối DOM thật trong trang),
  // KHÁC với popup gốc trình duyệt ở bước 5d bên dưới - phải click nút, không dùng acceptPopup().
  I.waitForElement('.swal2-confirm', 5);
  I.click('.swal2-confirm');
  I.wait(1);

  I.click('2. Phân công Trọng tài');
  I.waitForElement('.ad-select', 5);
  I.selectOption('.ad-select', new RegExp(DATA.refereeName));
  I.click('Phân công');
  I.wait(1);

  // ================= BƯỚC 4: SPECTATOR đặt cược =================
  loginAs(I, ACCOUNTS.spectator);
  I.amOnPage('/races');
  I.waitForElement('.rc-wallet-balance', 10);
  const balanceTruoc = parseVND(await I.grabTextFrom('.rc-wallet-balance'));
  I.say(`Số dư ví TRƯỚC khi cược: ${balanceTruoc.toLocaleString('vi-VN')}đ`);

  I.waitForText('Vào Tiền Ngay', 10);
  I.click('Vào Tiền Ngay');
  I.waitForElement('//label[contains(text(),"Chọn Ngựa Đua")]/following-sibling::select', 5);
  I.selectOption('//label[contains(text(),"Chọn Ngựa Đua")]/following-sibling::select', new RegExp(DATA.horseName));
  // "Về Nhất (Top 1) - Ăn x3.0" đã là lựa chọn mặc định đầu tiên nên không cần chọn lại
  I.fillField('//label[contains(text(),"Số tiền cược")]/following-sibling::input', DATA.betAmount);
  I.click('Chốt Kèo!');
  I.wait(1);

  // ================= BƯỚC 5: REFEREE lập kết quả + biên bản + vi phạm + chốt thưởng =================
  loginAs(I, ACCOUNTS.referee);
  I.amOnPage('/profile/referee-dashboard');
  I.waitForText('Lập kết quả', 10);

  // -- 5a. Lập kết quả --
  I.click('Lập kết quả');
  I.waitForElement('//label[contains(text(),"Chọn ngựa")]/following-sibling::div//select', 5);
  I.selectOption('//label[contains(text(),"Chọn ngựa")]/following-sibling::div//select', new RegExp(DATA.horseName));
  I.fillField('//label[contains(text(),"Vị trí")]/following-sibling::input', DATA.resultPosition);
  I.fillField('//label[contains(text(),"Thời gian (giây)")]/following-sibling::input', DATA.resultFinishTime);
  I.click('Lưu kết quả');
  I.wait(1);
  I.click('✖');

  // -- 5b. Lập biên bản --
  I.click('Lập biên bản');
  I.waitForElement('#reportText', 5);
  I.fillField('#reportText', DATA.reportText);
  I.click('Lưu biên bản');
  I.wait(1);
  I.click('✖');

  // -- 5c. Ghi nhận vi phạm (Khởi đầu sai / FALSE_START) --
  I.click('Ghi nhận vi phạm');
  I.waitForElement('//label[contains(text(),"Chọn ngựa")]/following-sibling::div//select', 5);
  I.selectOption('//label[contains(text(),"Chọn ngựa")]/following-sibling::div//select', new RegExp(DATA.horseName));
  I.selectOption('//label[contains(text(),"Loại vi phạm")]/following-sibling::select', 'Khởi đầu sai');
  I.fillField('//label[contains(text(),"Mô tả vi phạm")]/following-sibling::textarea', DATA.violationDesc);
  I.click('Ghi nhận');
  I.wait(1);
  I.click('✖');

  // -- 5d. Chốt & Chia Thưởng --
  // Nút này dùng window.confirm() gốc của trình duyệt (KHÁC SweetAlert2 ở bước 3)
  // nên phải dùng acceptPopup(), không click DOM.
  I.click('Chốt & Chia Thưởng');
  I.acceptPopup();
  I.wait(2);
  I.see('Đã chốt kết quả, cộng tiền thưởng thành công');

  // ================= KIỂM TRA CUỐI: ví Spectator phải tăng lên sau khi thắng cược =================
  loginAs(I, ACCOUNTS.spectator);
  I.amOnPage('/races');
  I.waitForElement('.rc-wallet-balance', 10);
  const balanceSau = parseVND(await I.grabTextFrom('.rc-wallet-balance'));
  I.say(`Số dư ví SAU khi thắng cược: ${balanceSau.toLocaleString('vi-VN')}đ`);

  if (balanceSau <= balanceTruoc) {
    throw new Error(
      `FAIL: Số dư ví KHÔNG tăng sau khi thắng cược! ` +
      `Trước: ${balanceTruoc.toLocaleString('vi-VN')}đ, Sau: ${balanceSau.toLocaleString('vi-VN')}đ`
    );
  }
});