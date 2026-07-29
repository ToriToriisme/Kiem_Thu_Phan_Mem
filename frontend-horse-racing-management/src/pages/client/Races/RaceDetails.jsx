import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosClient from '../../../services/axiosClient';
import { FiArrowLeft, FiCalendar, FiClock, FiMapPin, FiActivity, FiList } from 'react-icons/fi';
import './RaceDetails.css';

const RaceDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [race, setRace] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchRace = async () => {
      try {
        const raceData = await axiosClient.get(`/admin/races/${id}`);
        setRace(raceData);
      } catch (err) {
        console.error('Lỗi khi tải chi tiết vòng đua:', err);
        setError('Không thể tải chi tiết vòng đua. Vui lòng thử lại sau.');
      } finally {
        setLoading(false);
      }
    };

    fetchRace();
  }, [id]);

  const formatDateTime = (value) => {
    if (!value) return 'Chưa xác định';
    const date = new Date(value);
    return `${date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })} - ${date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}`;
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'SCHEDULED':
        return 'Đã lên lịch';
      case 'UPCOMING':
        return 'Sắp diễn ra';
      case 'ONGOING':
        return 'Đang diễn ra';
      case 'COMPLETED':
        return 'Đã kết thúc';
      case 'CANCELLED':
        return 'Đã hủy';
      case 'POSTPONED':
        return 'Đã hoãn';
      default:
        return status || 'Chưa xác định';
    }
  };

  if (loading) {
    return (
      <div className="race-details-container">
        <div className="race-details-loading">
          <div className="spinner" />
          <p>Đang tải chi tiết vòng đua...</p>
        </div>
      </div>
    );
  }

  if (error || !race) {
    return (
      <div className="race-details-container">
        <button className="btn-back" onClick={() => navigate('/races')}>
          <FiArrowLeft /> Quay lại
        </button>
        <div className="race-details-error">{error || 'Không tìm thấy vòng đua.'}</div>
      </div>
    );
  }

  return (
    <div className="race-details-container fade-in">
      <button className="btn-back" onClick={() => navigate('/races')}>
        <FiArrowLeft /> Quay lại
      </button>

      <div className="race-details-card glass-card">
        <div className="race-details-header">
          <div>
            <span className="race-status-badge">{getStatusText(race.status)}</span>
            <h1>{race.name || 'Vòng đua không xác định'}</h1>
            <p className="race-details-subtitle">{race.description || 'Chi tiết vòng đua sẽ hiển thị ở đây.'}</p>
            <button 
                className="btn-primary" 
                style={{marginTop: '15px', padding: '10px 20px', background: '#3b82f6', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold'}}
                onClick={() => navigate(`/races/${id}/live`)}>
                📺 Xem Mô Phỏng Đua
            </button>
          </div>
        </div>

        <div className="race-details-meta">
          <div className="meta-item">
            <FiCalendar /> Ngày bắt đầu
            <strong>{formatDateTime(race.startTime)}</strong>
          </div>
          <div className="meta-item">
            <FiClock /> Thời gian
            <strong>{formatDateTime(race.startTime)}</strong>
          </div>
          <div className="meta-item">
            <FiMapPin /> Cự ly
            <strong>{race.distance ? `${race.distance} m` : 'Chưa cập nhật'}</strong>
          </div>
          <div className="meta-item">
            <FiList /> Giải đấu
            <strong>{race.tournamentName || 'Không có thông tin'}</strong>
          </div>
        </div>

        {race.participants && race.participants.length > 0 ? (
          <div className="race-details-section">
            <h2>Danh sách tham dự</h2>
            <div className="participants-list">
              {race.participants.map((participant) => (
                <div key={participant.id || participant.name} className="participant-item">
                  <span>{participant.name || participant.horseName || 'Không tên'}</span>
                  <small>{participant.role || participant.type || 'Thành viên'}</small>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="race-details-section empty-state">
            <FiActivity size={40} />
            <p>Chưa có thông tin tham dự cho vòng đua này.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default RaceDetails;
