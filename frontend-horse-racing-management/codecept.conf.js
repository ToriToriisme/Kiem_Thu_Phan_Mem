import { setHeadlessWhen } from '@codeceptjs/configure';

// Chạy ẩn (headless) khi có biến môi trường HEADLESS=true
// Mặc định chạy có giao diện để dễ quan sát khi debug
setHeadlessWhen(process.env.HEADLESS);

export const config = {
  tests: './tests/**/*_test.js',
  output: './output',
  helpers: {
    Playwright: {
      // Dev server của Vite (npm run dev) - đổi lại nếu bạn chạy port khác
      url: 'http://localhost:5173',
      show: true,
      browser: 'chromium',
      windowSize: '1280x800',
      waitForTimeout: 10000,
      waitForAction: 300,
    },
  },
  include: {
    I: './steps_file.js',
  },
  name: 'frontend-horse-racing-management',
};