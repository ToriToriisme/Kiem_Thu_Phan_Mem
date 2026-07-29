import React, { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { fetchCurrentUser } from '../../../../redux/slices/authSlice';
import axiosClient from '../../../../services/axiosClient';
import { showToast, showErrorAlert } from '../../../../utils/alertUtils';
import { FiUser, FiMail, FiSave } from 'react-icons/fi';

const GeneralInfo = () => {
    const { user } = useSelector(state => state.auth);
    const dispatch = useDispatch();

    const [formData, setFormData] = useState({
        fullName: '',
        email: ''
    });
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (user) {
            setFormData({
                fullName: user.fullName || '',
                email: user.email || ''
            });
        }
    }, [user]);

    const handleFormChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await axiosClient.put('/auth/me', {
                fullName: formData.fullName,
                email: formData.email
            });

            showToast('Cập nhật hồ sơ thành công!', 'success');
            dispatch(fetchCurrentUser());
        } catch (error) {
            console.error('Lỗi cập nhật hồ sơ:', error);
            showErrorAlert('Lỗi', error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật hồ sơ.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="profile-card main-info-card fade-in">
            <div className="card-header">
                <h3 className="card-title">Thông Tin Cơ Bản</h3>
                <p className="card-subtitle">Cập nhật thông tin cá nhân của bạn tại đây</p>
            </div>

            <form onSubmit={handleProfileSubmit} className="profile-form">
                <div className="form-group">
                    <label>Họ và Tên</label>
                    <div className="input-group">
                        <span className="input-icon"><FiUser /></span>
                        <input 
                            type="text" 
                            name="fullName" 
                            value={formData.fullName} 
                            onChange={handleFormChange} 
                            required
                            className="premium-input"
                        />
                    </div>
                </div>

                <div className="form-group">
                    <label>Email liên hệ</label>
                    <div className="input-group disabled-group">
                        <span className="input-icon"><FiMail /></span>
                        <input 
                            type="email" 
                            name="email" 
                            value={formData.email} 
                            disabled
                            className="premium-input disabled"
                        />
                        <span className="disabled-hint">Không thể thay đổi email</span>
                    </div>
                </div>

                <div className="form-actions" style={{ marginTop: '20px' }}>
                    <button type="submit" className="btn-primary" disabled={loading}>
                        {loading ? 'Đang lưu...' : <><FiSave style={{marginRight: '8px'}}/> Lưu thay đổi</>}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default GeneralInfo;
