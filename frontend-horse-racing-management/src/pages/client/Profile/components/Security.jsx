import React, { useState } from 'react';
import axiosClient from '../../../../services/axiosClient';
import { showToast, showErrorAlert } from '../../../../utils/alertUtils';
import { FiLock, FiSave } from 'react-icons/fi';

const Security = () => {
    const [passwordData, setPasswordData] = useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [loading, setLoading] = useState(false);

    const handlePasswordChange = (e) => {
        const { name, value } = e.target;
        setPasswordData(prev => ({ ...prev, [name]: value }));
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        
        if (passwordData.newPassword !== passwordData.confirmPassword) {
            showErrorAlert('Lỗi', 'Mật khẩu xác nhận không khớp!');
            return;
        }

        setLoading(true);
        try {
            await axiosClient.put('/auth/me/password', {
                oldPassword: passwordData.oldPassword,
                newPassword: passwordData.newPassword
            });
            showToast('Đổi mật khẩu thành công!', 'success');
            setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
        } catch (error) {
            showErrorAlert('Lỗi', error.response?.data?.message || 'Không thể đổi mật khẩu.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="profile-card main-info-card fade-in">
            <div className="card-header">
                <h3 className="card-title">Bảo Mật Tài Khoản</h3>
                <p className="card-subtitle">Đổi mật khẩu định kỳ để bảo vệ tài khoản của bạn.</p>
            </div>

            <form onSubmit={handlePasswordSubmit} className="profile-form">
                <div className="form-group">
                    <label>Mật khẩu hiện tại</label>
                    <div className="input-group">
                        <span className="input-icon"><FiLock /></span>
                        <input 
                            type="password" 
                            name="oldPassword"
                            value={passwordData.oldPassword}
                            onChange={handlePasswordChange}
                            required
                            className="premium-input"
                            placeholder="Nhập mật khẩu hiện tại..."
                        />
                    </div>
                </div>
                <div className="form-group">
                    <label>Mật khẩu mới</label>
                    <div className="input-group">
                        <span className="input-icon"><FiLock /></span>
                        <input 
                            type="password" 
                            name="newPassword"
                            value={passwordData.newPassword}
                            onChange={handlePasswordChange}
                            required
                            className="premium-input"
                            placeholder="Nhập mật khẩu mới..."
                        />
                    </div>
                </div>
                <div className="form-group">
                    <label>Xác nhận mật khẩu mới</label>
                    <div className="input-group">
                        <span className="input-icon"><FiLock /></span>
                        <input 
                            type="password" 
                            name="confirmPassword"
                            value={passwordData.confirmPassword}
                            onChange={handlePasswordChange}
                            required
                            className="premium-input"
                            placeholder="Nhập lại mật khẩu mới..."
                        />
                    </div>
                </div>
                
                <div className="form-actions" style={{ marginTop: '20px' }}>
                    <button type="submit" className="btn-primary" disabled={loading}>
                        {loading ? 'Đang lưu...' : <><FiSave style={{marginRight: '8px'}}/> Xác nhận đổi</>}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default Security;
