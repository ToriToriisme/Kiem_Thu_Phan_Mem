import React, { useState, useEffect } from 'react';
import apiClient from '../../utils/axiosConfig';
import { showConfirmModal, showToast, showErrorAlert } from '../../utils/alertUtils';
import './AdminApprovalDashboard.css';

const AdminApprovalDashboard = () => {
  const [activeTab, setActiveTab] = useState('APPROVAL'); 

  // Dữ liệu thực từ API
  const [pendingRegistrations, setPendingRegistrations] = useState([]);
  const [races, setRaces] = useState([]);
  const [referees, setReferees] = useState([]);
  
  const [loading, setLoading] = useState(false);
  // Quản lý việc chọn trọng tài cho từng chặng đua
  const [selectedReferees, setSelectedReferees] = useState({});

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      // 1. Lấy đơn chờ duyệt
      const regRes = await apiClient.get('/admin/management/registrations/pending');
      setPendingRegistrations(regRes.data);

      // 2. Lấy chặng đua (Dùng API lấy toàn bộ Race đã có sẵn ở RaceController)
      const raceRes = await apiClient.get('/admin/races');
      setRaces(raceRes.data);

      // Khởi tạo giá trị ban đầu cho dropdown nếu chặng đua đã có trọng tài
      const initialAssignments = {};
      raceRes.data.forEach(race => {
        if (race.refereeId) {
          initialAssignments[race.id] = race.refereeId;
        }
      });
      setSelectedReferees(initialAssignments);

      // 3. Lấy danh sách trọng tài
      const refRes = await apiClient.get('/admin/management/referees');
      setReferees(refRes.data);

    } catch (error) {
      console.error("Lỗi tải dữ liệu Admin:", error);
      showErrorAlert("Lỗi tải dữ liệu", "Không thể đồng bộ dữ liệu từ hệ thống.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleApprove = async (id) => {
    const isConfirmed = await showConfirmModal("Xác nhận duyệt?", "Bạn có chắc chắn muốn duyệt đơn đăng ký này?");
    if (!isConfirmed) return;
    try {
      await apiClient.put(`/admin/management/registrations/${id}/approve`);
      showToast("Đã duyệt đơn đăng ký thành công!", "success");
      setPendingRegistrations(prev => prev.filter(reg => reg.id !== id));
    } catch (error) {
      showErrorAlert("Lỗi khi duyệt đơn", error.response?.data?.message || "Vui lòng thử lại sau.");
    }
  };

  const handleReject = async (id) => {
    const isConfirmed = await showConfirmModal("Xác nhận từ chối?", "Bạn có chắc chắn muốn từ chối đơn đăng ký này?", "Từ chối");
    if (!isConfirmed) return;
    try {
      await apiClient.put(`/admin/management/registrations/${id}/reject`);
      showToast("Đã từ chối đơn đăng ký!", "warning");
      setPendingRegistrations(prev => prev.filter(reg => reg.id !== id));
    } catch (error) {
      showErrorAlert("Lỗi khi từ chối đơn", error.response?.data?.message || "Vui lòng thử lại sau.");
    }
  };

  // Phân công trọng tài
  const handleAssignReferee = async (raceId) => {
    const refereeId = selectedReferees[raceId];
    if (!refereeId) {
      showToast("Vui lòng chọn một trọng tài từ danh sách!", "warning");
      return;
    }
    try {
      await apiClient.put(`/admin/management/races/${raceId}/assign-referee/${refereeId}`);
      showToast("Phân công trọng tài thành công!", "success");
      fetchDashboardData();
    } catch (error) {
      showErrorAlert("Lỗi phân công trọng tài", error.response?.data?.message || "Vui lòng thử lại sau.");
    }
  };

  return (
    <div className="ad-container">
      <div className="ad-card">
        
        {/* Header Tabs */}
        <div className="ad-header-tabs">
          <h2 className="ad-header-title">Quản Lý Đăng Ký & Trọng Tài</h2>
          <div className="ad-tab-nav">
            <button 
              className={`ad-tab-btn ${activeTab === 'APPROVAL' ? 'active' : ''}`}
              onClick={() => setActiveTab('APPROVAL')}
            >
              1. Duyệt đơn đăng ký
            </button>
            <button 
              className={`ad-tab-btn ${activeTab === 'REFEREE' ? 'active' : ''}`}
              onClick={() => setActiveTab('REFEREE')}
            >
              2. Phân công Trọng tài
            </button>
          </div>
        </div>

        {loading && <div className="ad-loading">Đang đồng bộ dữ liệu hệ thống...</div>}

        {/* TAB 1: DUYỆT ĐƠN */}
        {!loading && activeTab === 'APPROVAL' && (
          <div className="ad-content">
            <div className="ad-table-wrapper">
              <table className="ad-table">
                <thead>
                  <tr>
                    <th>Mã đơn</th>
                    <th>Ngựa đăng ký</th>
                    <th>Giải đấu</th>
                    <th>Trạng thái</th>
                    <th style={{ textAlign: 'center' }}>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingRegistrations.length > 0 ? pendingRegistrations.map((reg) => (
                    <tr key={reg.id}>
                      <td style={{ fontWeight: 600 }}>#{reg.id}</td>
                      <td>{reg.horse?.name || 'Đang cập nhật'}</td>
                      <td>{reg.tournament?.name || 'Đang cập nhật'}</td>
                      <td>
                        <span className="ad-badge badge-pending">Chờ duyệt</span>
                      </td>
                      <td>
                        <div className="ad-btn-group">
                          <button onClick={() => handleApprove(reg.id)} className="ad-btn ad-btn-approve">
                            Duyệt
                          </button>
                          <button onClick={() => handleReject(reg.id)} className="ad-btn ad-btn-reject">
                            Từ chối
                          </button>
                        </div>
                      </td>
                    </tr>
                  )) : (
                    <tr>
                      <td colSpan="4" className="ad-empty">
                        Tuyệt vời! Đã duyệt hết tất cả đơn đăng ký.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 2: PHÂN CÔNG TRỌNG TÀI */}
        {!loading && activeTab === 'REFEREE' && (
          <div className="ad-content">
            <div className="ad-table-wrapper">
              <table className="ad-table">
                <thead>
                  <tr>
                    <th>Chặng đua</th>
                    <th>Trạng thái</th>
                    <th>Chọn Trọng tài</th>
                    <th style={{ textAlign: 'center' }}>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {races.length > 0 ? races.map((race) => (
                    <tr key={race.id}>
                      <td style={{ fontWeight: 600 }}>{race.name || `Chặng #${race.id}`}</td>
                      <td>
                        {race.refereeName ? (
                          <span className="ad-badge badge-approved">
                            Trọng tài: {race.refereeName}
                          </span>
                        ) : (
                          <span className="ad-badge badge-pending">
                            Chờ phân công
                          </span>
                        )}
                      </td>
                      <td>
                        <select 
                          className="ad-select"
                          value={selectedReferees[race.id] || ''}
                          onChange={(e) => setSelectedReferees({ 
                            ...selectedReferees, 
                            [race.id]: e.target.value 
                          })}
                        >
                          <option value="">-- Chọn trọng tài --</option>
                          {referees.map(ref => (
                            <option key={ref.id} value={ref.id}>{ref.fullName}</option>
                          ))}
                        </select>
                      </td>
                      <td>
                        <div className="ad-btn-group">
                          <button 
                            onClick={() => handleAssignReferee(race.id)} 
                            className="ad-btn ad-btn-assign"
                          >
                            Phân công
                          </button>
                        </div>
                      </td>
                    </tr>
                  )) : (
                    <tr>
                      <td colSpan="4" className="ad-empty">
                        Chưa có dữ liệu chặng đua.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default AdminApprovalDashboard;