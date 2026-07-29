import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../../../services/axiosClient';
import { FiCalendar, FiClock, FiMapPin, FiActivity, FiFilter } from 'react-icons/fi';
import './Races.css';

const Races = () => {
    const [races, setRaces] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [filterStatus, setFilterStatus] = useState('ALL');
    const navigate = useNavigate();

    useEffect(() => {
        fetchRaces();
    }, []);

    const fetchRaces = async () => {
        try {
            const data = await axiosClient.get('/admin/races');
            setRaces(data);
        } catch (err) {
            console.error('Lỗi khi lấy danh sách cuộc đua:', err);
            setError('Không thể tải danh sách cuộc đua. Vui lòng thử lại sau.');
        } finally {
            setLoading(false);
        }
    };

    const getStatusText = (status) => {
        switch (status) {
            case 'SCHEDULED': return 'Đã lên lịch';
            case 'UPCOMING': return 'Sắp diễn ra';
            case 'ONGOING': return 'Đang diễn ra';
            case 'COMPLETED': return 'Đã kết thúc';
            case 'CANCELLED': return 'Đã hủy';
            case 'POSTPONED': return 'Đã hoãn';
            default: return status;
        }
    };

    const getStatusClass = (status) => {
        switch (status) {
            case 'SCHEDULED': 
            case 'UPCOMING': 
            case 'POSTPONED': 
                return 'race-status-upcoming';
            case 'ONGOING': return 'race-status-ongoing';
            case 'COMPLETED': 
            case 'CANCELLED': 
                return 'race-status-completed';
            default: return '';
        }
    };

    const filteredRaces = useMemo(() => {
        if (filterStatus === 'ALL') return races;
        if (filterStatus === 'UPCOMING') {
            return races.filter(r => ['SCHEDULED', 'UPCOMING', 'POSTPONED'].includes(r.status));
        }
        return races.filter(r => r.status === filterStatus);
    }, [races, filterStatus]);

    if (loading) {
        return (
            <div className="races-container">
                <div className="races-loading">
                    <div className="spinner"></div>
                    <p>Đang tải lịch đua...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="races-container">
                <div className="races-error">
                    <FiActivity size={32} style={{marginBottom: '16px'}}/>
                    <p>{error}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="races-container fade-in">
            <div className="races-header glass-header">
                <h1 className="races-title">Lịch Thi Đấu & Vòng Đua</h1>
                <p className="races-subtitle">Theo dõi toàn bộ lịch trình, cập nhật trạng thái trực tiếp của các vòng đua.</p>
            </div>

            <div className="races-controls glass-panel">
                <div className="filter-group">
                    <span className="filter-label"><FiFilter /> Lọc theo trạng thái:</span>
                    <button className={`filter-btn ${filterStatus === 'ALL' ? 'active' : ''}`} onClick={() => setFilterStatus('ALL')}>Tất cả</button>
                    <button className={`filter-btn ${filterStatus === 'UPCOMING' ? 'active' : ''}`} onClick={() => setFilterStatus('UPCOMING')}>Sắp diễn ra</button>
                    <button className={`filter-btn ${filterStatus === 'ONGOING' ? 'active' : ''}`} onClick={() => setFilterStatus('ONGOING')}>Đang diễn ra</button>
                    <button className={`filter-btn ${filterStatus === 'COMPLETED' ? 'active' : ''}`} onClick={() => setFilterStatus('COMPLETED')}>Đã kết thúc</button>
                </div>
            </div>

            {filteredRaces.length === 0 ? (
                <div className="no-races glass-panel">
                    <FiActivity size={48} className="no-races-icon" />
                    <p>Hiện chưa có cuộc đua nào trong danh mục này.</p>
                </div>
            ) : (
                <div className="races-timeline">
                    {filteredRaces.map((race, index) => {
                        const raceDate = new Date(race.startTime);
                        const isEven = index % 2 === 0;
                        return (
                            <div key={race.id} className={`race-timeline-item ${isEven ? 'left' : 'right'} ${getStatusClass(race.status)}`}>
                                <div className="timeline-dot"></div>
                                <div className="race-card glass-card">
                                    <div className="race-card-header">
                                        <span className={`race-status-badge ${getStatusClass(race.status)}`}>
                                            {getStatusText(race.status)}
                                        </span>
                                        <div className="race-datetime">
                                            <span className="date"><FiCalendar /> {raceDate.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })}</span>
                                            <span className="time"><FiClock /> {raceDate.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}</span>
                                        </div>
                                    </div>
                                    <h3 className="race-card-title">{race.name}</h3>
                                    <div className="race-card-meta">
                                        <span className="meta-item"><FiMapPin /> Cự ly: {race.distance ? race.distance + 'm' : 'Chưa cập nhật'}</span>
                                        {race.tournamentName && (
                                            <span className="meta-item tournament-meta">Giải: <strong>{race.tournamentName}</strong></span>
                                        )}
                                    </div>
                                    <div className="race-card-footer">
                                        <button className="btn-details glow-on-hover" onClick={() => navigate(`/races/${race.id}`)}>
                                            Chi Tiết & Kết Quả
                                        </button>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};

export default Races;
