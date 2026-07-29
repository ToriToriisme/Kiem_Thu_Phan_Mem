import React, { useState, useEffect } from 'react';
import axiosClient from '../../../services/axiosClient';
import { showConfirmModal, showErrorAlert, showToast } from '../../../utils/alertUtils';
import './PermissionManagement.css';
import { FiEdit2, FiTrash2, FiPlus, FiX } from 'react-icons/fi';

const PermissionManagement = () => {
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({ id: '', title: '', key: '' });
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    fetchPermissions();
  }, []);

  const fetchPermissions = async () => {
    try {
      setLoading(true);
      const res = await axiosClient.get('/admin/permissions');
      setPermissions(res || []);
    } catch (error) {
      showToast('Lỗi khi tải danh sách quyền', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setFormData({ id: '', title: '', key: '' });
    setIsEditing(false);
    setShowModal(true);
  };

  const handleOpenEdit = (permission) => {
    setFormData({ id: permission.id, title: permission.title, key: permission.key });
    setIsEditing(true);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title || !formData.key) {
      showToast('Vui lòng nhập đầy đủ thông tin', 'error');
      return;
    }

    try {
      if (isEditing) {
        await axiosClient.put(`/admin/permissions/${formData.id}`, formData);
        showToast('Cập nhật quyền thành công', 'success');
      } else {
        await axiosClient.post('/admin/permissions', formData);
        showToast('Thêm quyền mới thành công', 'success');
      }
      setShowModal(false);
      fetchPermissions();
    } catch (error) {
      showToast(error.response?.data?.message || 'Có lỗi xảy ra', 'error');
    }
  };

  const handleDelete = async (id) => {
    const isConfirmed = await showConfirmModal('Xác nhận xóa', 'Bạn có chắc chắn muốn xóa quyền này?');
    if (isConfirmed) {
      try {
        await axiosClient.delete(`/admin/permissions/${id}`);
        showToast('Xóa quyền thành công', 'success');
        fetchPermissions();
      } catch (error) {
        showErrorAlert('Lỗi', error.response?.data?.message || 'Không thể xóa quyền này');
      }
    }
  };

  if (loading) {
    return <div className="loading-state">Đang tải dữ liệu...</div>;
  }

  return (
    <div className="permission-management">
      <div className="page-header">
        <h2>Quản lý Quyền (Permissions)</h2>
        <button className="btn-add" onClick={handleOpenAdd}>
          <FiPlus /> Thêm Quyền
        </button>
      </div>

      <div className="table-container">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tiêu đề (Title)</th>
              <th>Khóa (Key)</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {permissions.length === 0 ? (
              <tr>
                <td colSpan="4" className="text-center">Chưa có quyền nào</td>
              </tr>
            ) : (
              permissions.map((p) => (
                <tr key={p.id}>
                  <td>{p.id.substring(p.id.length - 6)}</td>
                  <td>{p.title}</td>
                  <td><span className="badge-key">{p.key}</span></td>
                  <td>
                    <div className="action-buttons">
                      <button className="btn-edit" onClick={() => handleOpenEdit(p)}>
                        <FiEdit2 />
                      </button>
                      <button className="btn-delete" onClick={() => handleDelete(p.id)}>
                        <FiTrash2 />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>{isEditing ? 'Sửa Quyền' : 'Thêm Quyền Mới'}</h3>
              <button className="btn-close" onClick={handleCloseModal}><FiX /></button>
            </div>
            <div className="modal-body">
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label>Tiêu đề</label>
                  <input 
                    type="text" 
                    name="title" 
                    value={formData.title} 
                    onChange={handleChange} 
                    placeholder="VD: Quản lý Bài viết" 
                  />
                </div>
                <div className="form-group">
                  <label>Khóa (Key)</label>
                  <input 
                    type="text" 
                    name="key" 
                    value={formData.key} 
                    onChange={handleChange} 
                    placeholder="VD: MANAGE_POSTS" 
                    style={{ textTransform: 'uppercase' }}
                  />
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn-cancel" onClick={handleCloseModal}>Hủy</button>
                  <button type="submit" className="btn-save">Lưu lại</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PermissionManagement;
