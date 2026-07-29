import Swal from 'sweetalert2';

/**
 * Hiển thị Modal thông báo xác nhận (thay thế cho window.confirm)
 * @param {string} title Tiêu đề của modal
 * @param {string} text Nội dung chi tiết (có thể để trống)
 * @param {string} confirmText Chữ trên nút xác nhận
 * @returns {Promise<boolean>} Trả về true nếu người dùng ấn xác nhận
 */
export const showConfirmModal = async (title, text = '', confirmText = 'Xác nhận') => {
    const result = await Swal.fire({
        title: title,
        text: text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3b82f6', // Màu xanh dương um-btn-primary
        cancelButtonColor: '#ef4444', // Màu đỏ um-btn-danger
        confirmButtonText: confirmText,
        cancelButtonText: 'Hủy bỏ',
        customClass: {
            container: 'um-swal-container',
            popup: 'um-swal-popup'
        }
    });

    return result.isConfirmed;
};

/**
 * Hiển thị thông báo (toast) ở góc màn hình, tự động biến mất (thay thế cho alert thông thường)
 * @param {string} title Nội dung thông báo
 * @param {string} icon Loại thông báo: 'success', 'error', 'warning', 'info'
 */
export const showToast = (title, icon = 'success') => {
    const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        didOpen: (toast) => {
            toast.onmouseenter = Swal.stopTimer;
            toast.onmouseleave = Swal.resumeTimer;
        }
    });

    Toast.fire({
        icon: icon,
        title: title
    });
};

/**
 * Hiển thị thông báo dạng pop-up lỗi (thay thế cho alert khi có lỗi lớn)
 * @param {string} title Tiêu đề
 * @param {string} text Chi tiết lỗi
 */
export const showErrorAlert = (title, text = '') => {
    Swal.fire({
        icon: 'error',
        title: title,
        text: text,
        confirmButtonColor: '#3b82f6'
    });
};
