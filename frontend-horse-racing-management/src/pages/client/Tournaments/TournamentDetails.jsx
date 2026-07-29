import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosClient from '../../../services/axiosClient';
import { FiCalendar, FiClock, FiMapPin, FiActivity, FiArrowLeft, FiAward } from 'react-icons/fi';
import './TournamentDetails.css';

const TournamentDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [tournament, setTournament] = useState(null);
    const [races, setRaces] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchTournamentData();
    }, [id]);

    const fetchTournamentData = async () => {
        setLoading(true);
        try {
            const tournamentData = await axiosClient.get(`/admin/tournaments/${id}`);
            setTournament(tournamentData);

            try {
                const racesData = await axiosClient.get(`/admin/races/tournament/${id}`);
                setRaces(racesData || []);
            } catch (raceErr) {
                console.error('Không tìm thấy cuộc đua nào cho giải này hoặc lỗi API', raceErr);
                setRaces([]);
            }

        } catch (err) {
            console.error('Lỗi khi lấy thông tin giải đấu:', err);
            setError('Không thể tải thông tin giải đấu. Có thể giải đấu không tồn tại.');
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'Chưa xác định';
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    };

    const getStatusText = (status) => {
        switch (status) {
            case 'SCHEDULED': return 'Đã Lên Lịch';
            case 'UPCOMING': return 'Sắp Khởi Tranh';
            case 'ONGOING': return 'Đang Tranh Tài';
            case 'COMPLETED': return 'Đã Khép Lại';
            case 'CANCELLED': return 'Đã Hủy';
            case 'POSTPONED': return 'Đã Hoãn';
            default: return status;
        }
    };

    if (loading) {
        return (
            <div className="td-container">
                <div className="td-loading">
                    <div className="td-spinner"></div>
                    <p>Đang lấy dữ liệu giải đấu từ hệ thống...</p>
                </div>
            </div>
        );
    }

    if (error || !tournament) {
        return (
            <div className="td-container">
                <button className="td-btn-ghost" onClick={() => navigate('/tournaments')}>
                    <FiArrowLeft /> Quay lại danh sách
                </button>
                <div className="td-error" style={{color: '#ef4444'}}>
                    <p>{error || 'Không tìm thấy giải đấu.'}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="td-container">
            <button className="td-btn-ghost" onClick={() => navigate('/tournaments')}>
                <FiArrowLeft /> Quay lại Danh Sách
            </button>

            <div className="td-header-banner">
                <div className="td-header-content">
                    <div className="td-status-badge">
                        {getStatusText(tournament.status)}
                    </div>
                    <h1 className="td-title">{tournament.name}</h1>
                    <p className="td-description">{tournament.description || 'Giải đua chuyên nghiệp quy mô lớn quy tụ những đối thủ xứng tầm nhất.'}</p>
                    
                    <div className="td-meta-bar">
                        <div className="td-meta-item">
                            <FiCalendar /> Bắt đầu: <strong>{formatDate(tournament.startDate)}</strong>
                        </div>
                        <div className="td-meta-item">
                            <FiClock /> Bế mạc: <strong>{formatDate(tournament.endDate)}</strong>
                        </div>
                    </div>
                </div>
            </div>

            <div className="td-section">
                <h2 className="td-section-title">
                    <FiAward className="title-icon" style={{color: '#f59e0b'}} /> 
                    Danh Sách Vòng Đua ({races.length})
                </h2>
                
                {races.length === 0 ? (
                    <div className="td-empty-races">
                        <FiActivity size={40} style={{marginBottom: '10px', color: '#9ca3af'}} />
                        <p>Ban tổ chức chưa công bố lịch trình các vòng đua cho giải này.</p>
                    </div>
                ) : (
                    <div className="td-races-grid">
                        {races.map(race => (
                            <div key={race.id} className="td-race-card">
                                <div className="td-race-time">
                                    <span className="td-race-time-big">
                                        {new Date(race.startTime).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })}
                                    </span>
                                    <span className="td-race-time-small">
                                        {new Date(race.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                                    </span>
                                </div>
                                <div className="td-race-info">
                                    <h3 className="td-race-name">{race.name}</h3>
                                    <div className="td-race-details">
                                        <span><FiMapPin /> Cự ly: <strong>{race.distance ? race.distance + 'm' : 'Chưa rõ'}</strong></span>
                                        <span>Trạng thái: <strong>{getStatusText(race.status)}</strong></span>
                                    </div>
                                </div>
                                <div className="td-race-action">
                                    <button 
                                        className="td-btn-outline" 
                                        onClick={() => navigate('/races')}
                                    >
                                        Xem & Đặt Cược
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default TournamentDetails;
