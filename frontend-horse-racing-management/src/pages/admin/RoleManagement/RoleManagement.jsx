import React, { useState, useEffect } from 'react';
import axiosClient from '../../../services/axiosClient';
import { showConfirmModal, showErrorAlert, showToast } from '../../../utils/alertUtils';
import './RoleManagement.css';
import { FiPlus, FiX } from 'react-icons/fi';

const RoleManagement = () => {
  const [roles, setRoles] = useState([]);
  const [allPermissions, setAllPermissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  
  const [formData, setFormData] = useState({
    id: '',
    title: '',
    key: '',
    permissionIds: []
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [rolesRes, permsRes] = await Promise.all([
        axiosClient.get('/admin/roles'),
        axiosClient.get('/admin/permissions')
      ]);
      setRoles(rolesRes || []);
      setAllPermissions(permsRes || []);
    } catch (error) {
      showToast('Lỗi khi tải dữ liệu', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setFormData({ id: '', title: '', key: '', permissionIds: [] });
    setIsEditing(false);
    setShowModal(true);
  };

  const handleOpenEdit = (role) => {
    setFormData({ 
      id: role.id, 
      title: role.title, 
      key: role.key,
      permissionIds: role.permissions ? role.permissions.map(p => p.id) : []
    });
    setIsEditing(true);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleCheckboxChange = (permissionId) => {
    const newPermissionIds = formData.permissionIds.includes(permissionId)
      ? formData.permissionIds.filter(id => id !== permissionId)
      : [...formData.permissionIds, permissionId];
      
    setFormData({ ...formData, permissionIds: newPermissionIds });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title || !formData.key) {
      showToast('Vui lòng nhập đầy đủ tiêu đề và khóa', 'error');
      return;
    }

    try {
      if (isEditing) {
        await axiosClient.put(`/admin/roles/${formData.id}`, formData);
        showToast('Cập nhật vai trò thành công', 'success');
      } else {
        await axiosClient.post('/admin/roles', formData);
        showToast('Thêm vai trò mới thành công', 'success');
      }
      setShowModal(false);
      fetchData(); // refresh
    } catch (error) {
      showToast(error.response?.data?.message || 'Có lỗi xảy ra', 'error');
    }
  };

  const handleDelete = async (id) => {
    const isConfirmed = await showConfirmModal('Xác nhận xóa', 'Bạn có chắc chắn muốn xóa vai trò này?');
    if (isConfirmed) {
      try {
        await axiosClient.delete(`/admin/roles/${id}`);
        showToast('Xóa vai trò thành công', 'success');
        fetchData();
      } catch (error) {
        showErrorAlert('Lỗi', error.response?.data?.message || 'Không thể xóa vai trò này');
      }
    }
  };

  if (loading) {
    return <div style={{padding: '2rem', textAlign: 'center'}}>Đang tải dữ liệu...</div>;
  }

  return (
    <div className="rm-container">
      <div className="rm-header">
        <h2 className="rm-title">Quản lý Vai trò (Roles)</h2>
        <button className="rm-btn rm-btn-primary" onClick={handleOpenAdd}>
          <FiPlus /> Thêm Vai trò
        </button>
      </div>

      <div className="rm-card">
        <div className="rm-table-wrapper">
          <table className="rm-table">
            <thead>
              <tr>
                <th>Vai trò</th>
                <th>Khóa (Key)</th>
                <th>Quyền hạn (Permissions)</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {roles.length === 0 ? (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                    Chưa có vai trò nào
                  </td>
                </tr>
              ) : (
                roles.map((r) => (
                  <tr key={r.id}>
                    <td style={{ fontWeight: '500' }}>{r.title}</td>
                    <td><span className="badge-key">{r.key}</span></td>
                    <td>
                      <div className="permissions-list">
                        {r.permissions && r.permissions.length > 0 ? (
                          r.permissions.map(p => (
                            <span key={p.id} className="permission-badge" title={p.title}>{p.key}</span>
                          ))
                        ) : (
                          <span className="text-gray">Chưa có quyền</span>
                        )}
                      </div>
                    </td>
                    <td>
                      <div className="rm-actions">
                        <button className="rm-action-btn btn-blue" onClick={() => handleOpenEdit(r)}>
                          Sửa
                        </button>
                        <button className="rm-action-btn btn-red" onClick={() => handleDelete(r.id)}>
                          Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <div className="rm-modal-overlay" onClick={handleCloseModal}>
          <div className="rm-modal" onClick={e => e.stopPropagation()}>
            <div className="rm-modal-header">
              <h3>{isEditing ? 'Sửa Vai trò' : 'Thêm Vai trò Mới'}</h3>
              <button className="rm-modal-close" onClick={handleCloseModal}><FiX /></button>
            </div>
            <div className="rm-modal-body">
              <form onSubmit={handleSubmit}>
                <div className="rm-form-row">
                  <div className="rm-form-group">
                    <label>Tiêu đề</label>
                    <input 
                      type="text" 
                      name="title" 
                      value={formData.title} 
                      onChange={handleChange} 
                      placeholder="VD: Quản lý Hệ thống" 
                      required
                    />
                  </div>
                  <div className="rm-form-group">
                    <label>Khóa (Key)</label>
                    <input 
                      type="text" 
                      name="key" 
                      value={formData.key} 
                      onChange={handleChange} 
                      placeholder="VD: ROLE_MANAGER" 
                      style={{ textTransform: 'uppercase' }}
                      required
                    />
                  </div>
                </div>
                
                <div className="rm-form-group mt-4">
                  <label>Chọn quyền hạn (Permissions)</label>
                  <div className="permissions-grid">
                    {allPermissions.map(p => (
                      <label key={p.id} className="checkbox-label">
                        <input
                          type="checkbox"
                          checked={formData.permissionIds.includes(p.id)}
                          onChange={() => handleCheckboxChange(p.id)}
                        />
                        <span className="checkbox-text">
                          <strong>{p.title}</strong> ({p.key})
                        </span>
                      </label>
                    ))}
                  </div>
                </div>

                <div className="rm-modal-footer">
                  <button type="button" className="rm-btn rm-btn-secondary" onClick={handleCloseModal}>Hủy bỏ</button>
                  <button type="submit" className="rm-btn rm-btn-primary">Lưu lại</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RoleManagement;
