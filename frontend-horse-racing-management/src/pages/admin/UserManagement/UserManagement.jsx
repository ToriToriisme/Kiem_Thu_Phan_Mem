import React, { useState, useEffect, useMemo } from 'react';
import axiosClient from '../../../services/axiosClient';
import { showConfirmModal, showToast, showErrorAlert } from '../../../utils/alertUtils';
import './UserManagement.css'; 

const ROLES = ['ROLE_ADMIN', 'ROLE_HORSE_OWNER', 'ROLE_JOCKEY', 'ROLE_RACE_REFEREE', 'ROLE_SPECTATOR'];

const emptyForm = {
    username: '',
    password: '',
    email: '',
    fullName: '',
    role: 'ROLE_SPECTATOR',
    balance: 0,
};

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [isModalOpen, setModalOpen] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);
    const [formData, setFormData] = useState(emptyForm);
    const [loading, setLoading] = useState(false);
    
    // States for Bulk Actions, Filtering & Sorting
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterStatus, setFilterStatus] = useState('ALL'); // ALL, ACTIVE, BLOCKED
    const [filterRole, setFilterRole] = useState('ALL'); // ALL, ROLE_ADMIN, etc.
    const [sortOption, setSortOption] = useState('');
    const [bulkAction, setBulkAction] = useState('');

    const fetchUsers = async () => {
        try {
            const response = await axiosClient.get('/admin/users');
            setUsers(response);
            setSelectedUsers([]);
        } catch (error) {
            console.error('Lỗi khi lấy danh sách user:', error);
            showErrorAlert('Lỗi', 'Không thể tải danh sách người dùng.');
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    // --- Computed Stats ---
    const totalUsers = users.length;
    const activeUsers = users.filter(u => u.status !== false).length;
    const blockedUsers = users.filter(u => u.status === false).length;

    // --- Filter & Sort Logic ---
    const processedUsers = useMemo(() => {
        let result = [...users];

        // 1. Search filter
        if (searchTerm) {
            const lowerSearch = searchTerm.toLowerCase();
            result = result.filter(u => 
                (u.fullName && u.fullName.toLowerCase().includes(lowerSearch)) ||
                (u.email && u.email.toLowerCase().includes(lowerSearch)) ||
                (u.username && u.username.toLowerCase().includes(lowerSearch))
            );
        }

        // 2. Status filter
        if (filterStatus === 'ACTIVE') {
            result = result.filter(u => u.status !== false);
        } else if (filterStatus === 'BLOCKED') {
            result = result.filter(u => u.status === false);
        }

        // 3. Role filter
        if (filterRole !== 'ALL') {
            result = result.filter(u => u.role === filterRole);
        }

        // 4. Sorting
        if (sortOption === 'name_asc') {
            result.sort((a, b) => (a.fullName || a.username).localeCompare(b.fullName || b.username));
        } else if (sortOption === 'name_desc') {
            result.sort((a, b) => (b.fullName || b.username).localeCompare(a.fullName || a.username));
        } else if (sortOption === 'balance_desc') {
            result.sort((a, b) => (b.balance || 0) - (a.balance || 0));
        } else if (sortOption === 'balance_asc') {
            result.sort((a, b) => (a.balance || 0) - (b.balance || 0));
        } else if (sortOption === 'role_asc') {
            result.sort((a, b) => (a.role || '').localeCompare(b.role || ''));
        } else if (sortOption === 'role_desc') {
            result.sort((a, b) => (b.role || '').localeCompare(a.role || ''));
        }

        return result;
    }, [users, searchTerm, filterStatus, filterRole, sortOption]);

    const clearFilters = () => {
        setSearchTerm('');
        setFilterStatus('ALL');
        setFilterRole('ALL');
        setSortOption('');
    };

    // --- Selection Logic ---
    const handleSelectAll = (e) => {
        if (e.target.checked) {
            setSelectedUsers(processedUsers.map(u => u.id));
        } else {
            setSelectedUsers([]);
        }
    };

    const handleSelectUser = (id) => {
        if (selectedUsers.includes(id)) {
            setSelectedUsers(selectedUsers.filter(uId => uId !== id));
        } else {
            setSelectedUsers([...selectedUsers, id]);
        }
    };

    const applyBulkAction = async () => {
        if (!bulkAction || selectedUsers.length === 0) return;
        
        const isUnlocking = bulkAction === 'UNLOCK';
        const actionText = isUnlocking ? 'mở khóa' : 'khóa';

        const isConfirmed = await showConfirmModal(
            `Xác nhận ${actionText}`, 
            `Bạn có chắc chắn muốn ${actionText} ${selectedUsers.length} tài khoản này không?`
        );

        if (isConfirmed) {
            try {
                await axiosClient.put('/admin/users/bulk-status', {
                    ids: selectedUsers,
                    status: isUnlocking
                });
                showToast(`Đã ${actionText} thành công!`, 'success');
                fetchUsers();
                setBulkAction('');
            } catch (error) {
                console.error('Lỗi khi thay đổi trạng thái hàng loạt:', error);
                showToast('Có lỗi xảy ra!', 'error');
            }
        }
    };

    // --- Single Actions ---
    const handleToggleStatus = async (user) => {
        const newStatus = user.status === false ? true : false;
        const actionText = newStatus ? 'mở khóa' : 'khóa';
        
        const isConfirmed = await showConfirmModal(
            `Xác nhận ${actionText}`, 
            `Bạn có muốn ${actionText} tài khoản ${user.username}?`
        );

        if (isConfirmed) {
            try {
                await axiosClient.put('/admin/users/bulk-status', {
                    ids: [user.id],
                    status: newStatus
                });
                showToast(`Đã ${actionText} tài khoản thành công!`, 'success');
                fetchUsers();
            } catch (error) {
                showToast('Có lỗi xảy ra!', 'error');
            }
        }
    };

    const handleDelete = async (id) => {
        const isConfirmed = await showConfirmModal(
            'Xác nhận xóa', 
            'Bạn có chắc chắn muốn xóa user này không? Hành động này không thể hoàn tác.',
            'Xóa'
        );

        if (isConfirmed) {
            try {
                await axiosClient.delete(`/admin/users/${id}`);
                showToast('Xóa thành công!', 'success');
                fetchUsers();
            } catch (error) {
                console.error('Lỗi khi xóa:', error);
                showToast('Có lỗi xảy ra khi xóa!', 'error');
            }
        }
    };

    // --- Modal & Form Logic ---
    const openCreateModal = () => {
        setCurrentUser(null);
        setFormData(emptyForm);
        setModalOpen(true);
    };

    const openEditModal = (user) => {
        setCurrentUser(user);
        setFormData({
            username: user.username || '',
            password: '',
            email: user.email || '',
            fullName: user.fullName || '',
            role: user.role || 'ROLE_SPECTATOR',
            balance: user.balance ?? 0,
        });
        setModalOpen(true);
    };

    const handleFormChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: name === 'balance' ? parseFloat(value) || 0 : value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            if (currentUser) {
                await axiosClient.put(`/admin/users/${currentUser.id}`, {
                    email: formData.email,
                    fullName: formData.fullName,
                    role: formData.role,
                    balance: formData.balance,
                });
                showToast('Cập nhật thành công!', 'success');
            } else {
                await axiosClient.post('/admin/users', {
                    user: {
                        username: formData.username,
                        email: formData.email,
                        fullName: formData.fullName,
                        role: formData.role,
                        balance: formData.balance,
                    },
                    password: formData.password,
                });
                showToast('Tạo user thành công!', 'success');
            }
            setModalOpen(false);
            fetchUsers();
        } catch (error) {
            console.error('Lỗi khi lưu user:', error);
            showErrorAlert('Lỗi', 'Có lỗi xảy ra khi lưu thông tin user.');
        } finally {
            setLoading(false);
        }
    };

    // --- UI Helpers ---
    const getRoleBadgeClass = (role) => {
        switch (role) {
            case 'ROLE_ADMIN': return 'badge-admin';
            case 'ROLE_HORSE_OWNER': return 'badge-owner';
            case 'ROLE_JOCKEY': return 'badge-jockey';
            case 'ROLE_RACE_REFEREE': return 'badge-referee';
            default: return 'badge-spectator';
        }
    };

    const formatRoleName = (role) => {
        return role ? role.replace('ROLE_', '').replace('_', ' ') : 'N/A';
    };

    return (
        <div className="um-container">
            <div className="um-header">
                <h2 className="um-title">Quản Lý Người Dùng</h2>
                <button className="um-btn um-btn-primary" onClick={openCreateModal}>
                    + Thêm người dùng
                </button>
            </div>

            {/* Stats Cards */}
            <div className="um-stats-row">
                <div className="um-stat-card">
                    <div className="um-stat-header">
                        <svg className="um-stat-icon text-blue" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                        <span>Total Users</span>
                    </div>
                    <div className="um-stat-value">{totalUsers}</div>
                </div>
                <div className="um-stat-card">
                    <div className="um-stat-header">
                        <svg className="um-stat-icon text-green" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                        <span>Active Users</span>
                    </div>
                    <div className="um-stat-value">{activeUsers}</div>
                </div>
                <div className="um-stat-card">
                    <div className="um-stat-header">
                        <svg className="um-stat-icon text-red" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>
                        <span>Blocked Users</span>
                    </div>
                    <div className="um-stat-value">{blockedUsers}</div>
                </div>
            </div>

            {/* Filter & Action Bar */}
            <div className="um-filter-card">
                <div className="um-filter-row">
                    <div className="um-search-box">
                        <svg className="um-search-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                        <input 
                            type="text" 
                            placeholder="Tìm tên hoặc email..." 
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    <select className="um-select-filter" value={filterRole} onChange={(e) => setFilterRole(e.target.value)}>
                        <option value="ALL">Quyền hạn: Tất cả</option>
                        {ROLES.map(r => <option key={r} value={r}>{formatRoleName(r)}</option>)}
                    </select>
                    <select className="um-select-filter" value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                        <option value="ALL">Trạng thái: Tất cả</option>
                        <option value="ACTIVE">Hoạt động</option>
                        <option value="BLOCKED">Bị khóa</option>
                    </select>
                    <select className="um-select-filter" value={sortOption} onChange={(e) => setSortOption(e.target.value)}>
                        <option value="">-- Sắp xếp theo --</option>
                        <option value="name_asc">Tên (A-Z)</option>
                        <option value="name_desc">Tên (Z-A)</option>
                        <option value="balance_desc">Số dư (Giảm dần)</option>
                        <option value="balance_asc">Số dư (Tăng dần)</option>
                        <option value="role_asc">Quyền hạn (A-Z)</option>
                        <option value="role_desc">Quyền hạn (Z-A)</option>
                    </select>
                    <button className="um-btn um-btn-danger" onClick={clearFilters}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                        Xóa lọc
                    </button>
                </div>
                
                <div className="um-bulk-row">
                    <select className="um-select-filter" value={bulkAction} onChange={(e) => setBulkAction(e.target.value)}>
                        <option value="">-- Chọn hành động --</option>
                        <option value="LOCK">Khóa tài khoản</option>
                        <option value="UNLOCK">Mở khóa tài khoản</option>
                    </select>
                    <button 
                        className="um-btn um-btn-success" 
                        onClick={applyBulkAction}
                        disabled={!bulkAction || selectedUsers.length === 0}
                    >
                        Áp dụng {selectedUsers.length > 0 ? `(${selectedUsers.length})` : ''}
                    </button>
                </div>
            </div>

            {/* Table */}
            <div className="um-card">
                <div className="um-table-wrapper">
                    <table className="um-table">
                        <thead>
                            <tr>
                                <th style={{ width: '40px' }}>
                                    <input 
                                        type="checkbox" 
                                        className="um-checkbox"
                                        checked={selectedUsers.length === processedUsers.length && processedUsers.length > 0}
                                        onChange={handleSelectAll}
                                    />
                                </th>
                                <th>Tài khoản</th>
                                <th>Quyền hạn (Role)</th>
                                <th>Số dư</th>
                                <th>Trạng thái</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            {processedUsers.length > 0 ? (
                                processedUsers.map((user) => (
                                    <tr key={user.id} className={selectedUsers.includes(user.id) ? 'um-row-selected' : ''}>
                                        <td>
                                            <input 
                                                type="checkbox"
                                                className="um-checkbox"
                                                checked={selectedUsers.includes(user.id)}
                                                onChange={() => handleSelectUser(user.id)}
                                            />
                                        </td>
                                        <td>
                                            <div className="um-user-info">
                                                <div className="um-avatar">
                                                    {user.username.charAt(0).toUpperCase()}
                                                </div>
                                                <div className="um-user-details">
                                                    <span className="um-user-name">{user.fullName || user.username}</span>
                                                    <span className="um-user-username">{user.email || '@' + user.username}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span className={`um-badge ${getRoleBadgeClass(user.role)}`}>
                                                {formatRoleName(user.role)}
                                            </span>
                                        </td>
                                        <td>
                                            <span className="um-text-dark">${user.balance?.toLocaleString() ?? 0}</span>
                                        </td>
                                        <td>
                                            <span className={`um-status-badge ${user.status !== false ? 'status-active' : 'status-blocked'}`}>
                                                {user.status !== false ? 'Hoạt động' : 'Bị khóa'}
                                            </span>
                                        </td>
                                        <td>
                                            <div className="um-actions">
                                                <button className="um-action-btn btn-blue" onClick={() => openEditModal(user)}>
                                                    Sửa
                                                </button>
                                                <button className="um-action-btn btn-orange" onClick={() => handleDelete(user.id)}>
                                                    Delete
                                                </button>
                                                <button 
                                                    className={`um-action-btn ${user.status !== false ? 'btn-red' : 'btn-green'}`} 
                                                    onClick={() => handleToggleStatus(user)}
                                                >
                                                    {user.status !== false ? 'Ban' : 'Unban'}
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="6" className="um-empty-state">
                                        <div className="um-empty-content">
                                            <p>Không tìm thấy dữ liệu phù hợp.</p>
                                        </div>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Modal */}
            {isModalOpen && (
                <div className="um-modal-overlay" onClick={() => setModalOpen(false)}>
                    <div className="um-modal" onClick={e => e.stopPropagation()}>
                        <div className="um-modal-header">
                            <h3>{currentUser ? 'Cập nhật tài khoản' : 'Thêm tài khoản mới'}</h3>
                            <button className="um-modal-close" onClick={() => setModalOpen(false)}>
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                            </button>
                        </div>
                        <form className="um-modal-body" onSubmit={handleSubmit}>
                            <div className="um-form-row">
                                <div className="um-form-group">
                                    <label>Username</label>
                                    <input name="username" value={formData.username} onChange={handleFormChange} required disabled={!!currentUser} className={currentUser ? 'um-input-disabled' : ''} />
                                </div>
                                {!currentUser && (
                                    <div className="um-form-group">
                                        <label>Mật khẩu</label>
                                        <input type="password" name="password" value={formData.password} onChange={handleFormChange} required />
                                    </div>
                                )}
                            </div>
                            <div className="um-form-row">
                                <div className="um-form-group">
                                    <label>Họ và tên</label>
                                    <input name="fullName" value={formData.fullName} onChange={handleFormChange} />
                                </div>
                                <div className="um-form-group">
                                    <label>Email</label>
                                    <input type="email" name="email" value={formData.email} onChange={handleFormChange} />
                                </div>
                            </div>
                            <div className="um-form-row">
                                <div className="um-form-group">
                                    <label>Phân quyền (Role)</label>
                                    <select name="role" value={formData.role} onChange={handleFormChange} className="um-select">
                                        {ROLES.map(r => <option key={r} value={r}>{formatRoleName(r)}</option>)}
                                    </select>
                                </div>
                                <div className="um-form-group">
                                    <label>Số dư ($)</label>
                                    <input type="number" name="balance" value={formData.balance} onChange={handleFormChange} min="0" step="0.01" />
                                </div>
                            </div>
                            <div className="um-modal-footer">
                                <button type="button" className="um-btn um-btn-secondary" onClick={() => setModalOpen(false)}>Hủy bỏ</button>
                                <button type="submit" className="um-btn um-btn-primary" disabled={loading}>{loading ? 'Đang xử lý...' : 'Lưu'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserManagement;
