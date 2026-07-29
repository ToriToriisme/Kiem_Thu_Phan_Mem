import React, { useEffect, useState } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { FiArrowLeft } from 'react-icons/fi';
import axiosClient from '../../../services/axiosClient';

const HorseDetails = () => {
  const { horseId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const searchParams = new URLSearchParams(location.search);
  const raceId = searchParams.get('raceId');

  useEffect(() => {
    if (!horseId || !raceId) return;

    const fetchHealth = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await axiosClient.get(`/referee/horse/${horseId}/health/race/${raceId}`);
        setHealth(data);
      } catch (err) {
        setError(err.response?.data?.error || 'Không thể tải thông tin sức khỏe ngựa.');
      } finally {
        setLoading(false);
      }
    };

    fetchHealth();
  }, [horseId, raceId]);

  return (
    <div className="dashboard-wrapper referee-dashboard">
      <div className="referee-dashboard-card">
        <button className="btn-ghost" onClick={() => navigate(-1)}>
          <FiArrowLeft style={{ marginRight: 6 }} /> Quay lại
        </button>

        <div className="referee-dashboard-header" style={{ marginTop: 16 }}>
          <div>
            <h2 className="dashboard-title">Chi tiết ngựa</h2>
            <p className="dashboard-subtitle">Mã ngựa: {horseId}</p>
            {raceId && <p className="dashboard-subtitle">Cuộc đua: {raceId}</p>}
          </div>
        </div>

        {raceId ? (
          <div>
            {loading ? (
              <div className="dashboard-loading">Đang tải thông tin sức khỏe...</div>
            ) : error ? (
              <div className="profile-status-badge danger">{error}</div>
            ) : health ? (
              <div className="table-responsive">
                <table className="tm-table referee-table">
                  <tbody>
                    <tr>
                      <th>Hoạt động</th>
                      <td>{health.activityStatus || 'N/A'}</td>
                    </tr>
                    <tr>
                      <th>Sức khỏe</th>
                      <td>{health.healthStatus || 'N/A'}</td>
                    </tr>
                    <tr>
                      <th>Nhiệt độ</th>
                      <td>{health.temperature ?? 'N/A'}</td>
                    </tr>
                    <tr>
                      <th>Nhịp tim</th>
                      <td>{health.heartRate ?? 'N/A'}</td>
                    </tr>
                    <tr>
                      <th>Thời gian kiểm tra</th>
                      <td>{health.checkedAt || 'N/A'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="empty-row">Không có dữ liệu sức khỏe cho ngựa này.</div>
            )}
          </div>
        ) : (
          <div className="empty-row">Không có mã cuộc đua. Không thể tải thông tin chi tiết ngựa.</div>
        )}
      </div>
    </div>
  );
};

export default HorseDetails;
