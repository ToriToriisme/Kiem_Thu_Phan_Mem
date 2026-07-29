import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../../../services/axiosClient';
import { FiCalendar, FiClock, FiActivity } from 'react-icons/fi';
import './Tournaments.css';

const Tournaments = () => {
    const [tournaments, setTournaments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchTournaments();
    }, []);

    const fetchTournaments = async () => {
        try {
            // Cẩn thận: Backend có thể đang chia role. Spectator cũng xem được tournaments
            const data = await axiosClient.get('/admin/tournaments');
            setTournaments(data || []);
        } catch (err) {
            console.error('Lỗi khi lấy danh sách giải đấu:', err);
            setError('Không thể tải danh sách giải đấu. Vui lòng thử lại sau.');
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
            case 'UPCOMING': return 'Sắp Mở Màn';
            case 'ONGOING': return 'Đang Tranh Tài';
            case 'COMPLETED': return 'Đã Khép Lại';
            default: return status;
        }
    };

    const getStatusClass = (status) => {
        switch (status) {
            case 'UPCOMING': return 'tm-status-upcoming';
            case 'ONGOING': return 'tm-status-ongoing';
            case 'COMPLETED': return 'tm-status-completed';
            default: return '';
        }
    };

    if (loading) {
        return (
            <div className="tm-container">
                <div className="tm-loading">
                    <div className="tm-spinner"></div>
                    <p>Đang tải dữ liệu giải đấu...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="tm-container">
                <div className="tm-empty" style={{borderColor: '#ef4444', color: '#ef4444'}}>
                    <p>{error}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="tm-container">
            <div className="tm-header">
                <h1 className="tm-title">Giải Đấu Chuyên Nghiệp</h1>
                <p className="tm-subtitle">Nơi quy tụ những chiến mã huyền thoại và những nài ngựa đẳng cấp nhất. Chọn một giải đấu để xem lịch trình các vòng đua.</p>
            </div>

            {tournaments.length === 0 ? (
                <div className="tm-empty">
                    <FiActivity size={48} />
                    <p>Hiện tại chưa có giải đấu nào trong hệ thống.</p>
                </div>
            ) : (
                <div className="tm-grid">
                    {tournaments.map((tournament) => (
                        <div key={tournament.id} className="tm-card">
                            <div className="tm-card-header">
                                <span className={`tm-status ${getStatusClass(tournament.status)}`}>
                                    {getStatusText(tournament.status)}
                                </span>
                            </div>
                            
                            <h3 className="tm-name">{tournament.name}</h3>
                            <p className="tm-desc">
                                {tournament.description || 'Giải đấu đang chờ cập nhật thêm thông tin chi tiết từ ban tổ chức.'}
                            </p>
                            
                            <div className="tm-meta">
                                <div className="tm-meta-item">
                                    <FiCalendar className="tm-icon" />
                                    <span>Khởi tranh: <strong>{formatDate(tournament.startDate)}</strong></span>
                                </div>
                                <div className="tm-meta-item">
                                    <FiClock className="tm-icon" />
                                    <span>Bế mạc: <strong>{formatDate(tournament.endDate)}</strong></span>
                                </div>
                            </div>
                            
                            <button className="tm-btn" onClick={() => navigate(`/tournaments/${tournament.id}`)}>
                                Xem Chi Tiết Giải
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default Tournaments;
