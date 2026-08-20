// Tiện ích định dạng ngày/giờ an toàn dùng chung cho toàn app.
// Mục tiêu: không bao giờ hiển thị chuỗi "Invalid Date" ra UI (K2-24, K2-30).

const FALLBACK_TEXT = 'Chưa xác định';

/**
 * Parse an toàn: trả về đối tượng Date hợp lệ hoặc null.
 * Không bao giờ throw lỗi, kể cả khi value null/undefined/rỗng/sai định dạng.
 */
const safeParseDate = (value) => {
    if (value === null || value === undefined || value === '') return null;
    const date = new Date(value);
    return isNaN(date.getTime()) ? null : date;
};

/**
 * Định dạng chỉ ngày, ví dụ: 21/08/2026
 * @param {string|number|Date} value - giá trị ngày thô từ backend
 * @param {string} fallback - text hiển thị khi value rỗng hoặc không hợp lệ
 */
export const formatDate = (value, fallback = FALLBACK_TEXT) => {
    const date = safeParseDate(value);
    if (!date) return fallback;
    return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    });
};

/**
 * Định dạng ngày kèm giờ, ví dụ: 21/08/2026 14:30
 */
export const formatDateTime = (value, fallback = FALLBACK_TEXT) => {
    const date = safeParseDate(value);
    if (!date) return fallback;
    return date.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
};

/**
 * true nếu value parse được thành một ngày hợp lệ.
 */
export const isValidDate = (value) => safeParseDate(value) !== null;

export default { formatDate, formatDateTime, isValidDate };