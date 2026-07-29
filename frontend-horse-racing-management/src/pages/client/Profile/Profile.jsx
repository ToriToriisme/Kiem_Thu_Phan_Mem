import React, { useEffect } from 'react';
import { useNavigate, NavLink, Outlet } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { FiUser, FiLock, FiShield, FiBriefcase, FiTarget, FiActivity, FiStar } from 'react-icons/fi';
import './Profile.css';

const Profile = () => {
    const { user, isAuthenticated, isInitializing } = useSelector(state => state.auth);
    const navigate = useNavigate();

    useEffect(() => {
        if (!isInitializing && !isAuthenticated) {
            navigate('/login');
        }
    }, [isAuthenticated, isInitializing, navigate]);

    const getRoleName = (role) => {
        if (!role) return 'N/A';
        const roleMap = {
            'ROLE_HORSE_OWNER': 'Chủ Ngựa',
            'ROLE_JOCKEY': 'Nài Ngựa',
            'ROLE_RACE_REFEREE': 'Trọng Tài',
            'ROLE_SPECTATOR': 'Khán Giả',
            'ROLE_ADMIN': 'Quản Trị Viên'
        };
        return roleMap[role] || role.replace('ROLE_', '').replace('_', ' ');
    };

    if (!user) return <div className="pf-container"><div className="pf-loading">Đang tải hồ sơ...</div></div>;

    const renderRoleLinks = () => {
        switch (user.role) {
            case 'ROLE_HORSE_OWNER':
                return (
                    <>
                        <h4 className="pf-sidebar-heading">Khu vực Chủ Ngựa</h4>
                        <NavLink to="owner-dashboard" className={({isActive}) => `pf-sidebar-link ${isActive ? 'pf-active' : ''}`}>
                            <FiBriefcase className="pf-sidebar-icon" /> Quản lý chiến mã
                        </NavLink>
                    </>
                );
            case 'ROLE_JOCKEY':
                return (
                    <>
                        <h4 className="pf-sidebar-heading">Khu vực Nài Ngựa</h4>
                        <NavLink to="jockey-dashboard" className={({isActive}) => `pf-sidebar-link ${isActive ? 'pf-active' : ''}`}>
                            <FiTarget className="pf-sidebar-icon" /> Lịch thi đấu
                        </NavLink>
                    </>
                );
            case 'ROLE_RACE_REFEREE':
                return (
                    <>
                        <h4 className="pf-sidebar-heading">Khu vực Trọng Tài</h4>
                        <NavLink to="referee-dashboard" className={({isActive}) => `pf-sidebar-link ${isActive ? 'pf-active' : ''}`}>
                            <FiActivity className="pf-sidebar-icon" /> Cập nhật kết quả
                        </NavLink>
                    </>
                );
            case 'ROLE_SPECTATOR':
                return null; // Spectator links moved to general account section
            default:
                return null;
        }
    };

    return (
        <div className="pf-page-wrapper" style={{background: '#f8fafc', minHeight: '100vh', width: '100vw', padding: '1px 0'}}>
            <div className="pf-container">
                <div className="pf-header-banner">
                    <div className="pf-header-content">
                        <div className="pf-avatar-wrapper">
                            <div className="pf-avatar">
                                {user.fullName ? user.fullName.charAt(0).toUpperCase() : <FiUser />}
                            </div>
                        </div>
                        <div className="pf-header-info">
                            <h1 className="pf-name">{user.fullName || user.username}</h1>
                            <p className="pf-username">@{user.username}</p>
                            <div className="pf-tags">
                                <span className="pf-tag pf-tag-role">
                                    <FiShield size={14} /> {getRoleName(user.role)}
                                </span>
                                {user.balance !== undefined && (
                                    <span className="pf-tag pf-tag-balance">
                                        💰 Số dư ví: {user.balance.toLocaleString('vi-VN')} ₫
                                    </span>
                                )}
                            </div>
                        </div>
                    </div>
                </div>

                <div className="pf-layout-wrapper">
                    {/* Sidebar Navigation */}
                    <aside className="pf-sidebar">
                        <div className="pf-sidebar-section">
                            <h4 className="pf-sidebar-heading">Tài khoản</h4>
                            <NavLink to="." end className={({isActive}) => `pf-sidebar-link ${isActive ? 'pf-active' : ''}`}>
                                <FiUser className="pf-sidebar-icon" /> <span style={{position: 'relative', zIndex: 2}}>Thông tin cá nhân</span>
                            </NavLink>
                            <NavLink to="security" className={({isActive}) => `pf-sidebar-link ${isActive ? 'pf-active' : ''}`}>
                                <FiLock className="pf-sidebar-icon" /> <span style={{position: 'relative', zIndex: 2}}>Bảo mật & Mật khẩu</span>
                            </NavLink>
                            <NavLink to="spectator-dashboard" className={({isActive}) => `pf-sidebar-link ${isActive ? 'pf-active' : ''}`}>
                                <FiStar className="pf-sidebar-icon" /> <span style={{position: 'relative', zIndex: 2}}>Lịch sử cá cược & Số dư</span>
                            </NavLink>
                        </div>

                        <div className="pf-sidebar-section">
                            {renderRoleLinks()}
                        </div>
                    </aside>

                    {/* Main Content */}
                    <main className="pf-main-content">
                        <Outlet />
                    </main>
                </div>
            </div>
        </div>
    );
};

export default Profile;
