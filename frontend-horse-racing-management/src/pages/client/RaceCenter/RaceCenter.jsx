import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import axiosClient from '../../../services/axiosClient';
import { showToast } from '../../../utils/alertUtils';
import { FaCalendarAlt, FaRoute, FaCoins, FaPlayCircle, FaTrophy } from 'react-icons/fa';
import { FiX } from 'react-icons/fi';
import './RaceCenter.css';

const RaceCenter = () => {
    const { user, isAuthenticated } = useSelector((state) => state.auth || {});
    const navigate = useNavigate();
    const [races, setRaces] = useState([]);
    const [loading, setLoading] = useState(false);
    const [balance, setBalance] = useState(0);
    const [activeTab, setActiveTab] = useState('ALL'); // ALL, SCHEDULED, IN_PROGRESS, COMPLETED
    
    // Modal states
    const [isBetModalOpen, setIsBetModalOpen] = useState(false);
    const [selectedRace, setSelectedRace] = useState(null);
    const [raceHorses, setRaceHorses] = useState([]);
    const [userBets, setUserBets] = useState([]);
    
    const [betForm, setBetForm] = useState({
        horseId: '',
        amount: 10000,
        predictedPosition: 1
    });

    useEffect(() => {
        fetchAllRaces();
    }, []);

    const fetchAllRaces = async () => {
        setLoading(true);
        try {
            const response = await axiosClient.get('/v1/spectator/races');
            // Backend might return empty or full list
            setRaces(response || []);
            
            // Fetch user wallet balance
            if (isAuthenticated && user?.id) {
                try {
                    const walletRes = await axiosClient.get(`/v1/spectator/wallet/${user.id}`);
                    if (walletRes) setBalance(walletRes.balance || 0);
                } catch (e) {
                    console.error("Failed to fetch balance", e);
                }
            }
        } catch (error) {
            console.error(error);
            showToast('Không thể tải danh sách vòng đua.', 'error');
        } finally {
            setLoading(false);
        }
    };

    const fetchHorsesForRace = async (raceId) => {
        try {
            const response = await axiosClient.get(`/referee/race/${raceId}/horses`);
            setRaceHorses(response || []);
        } catch (error) {
            console.error(error);
            showToast('Không thể tải danh sách ngựa đua.', 'error');
        }
    };

    const openBetModal = async (race) => {
        if (!isAuthenticated) {
            showToast('Vui lòng đăng nhập để đặt cược!', 'warning');
            navigate('/login');
            return;
        }
        setSelectedRace(race);
        setIsBetModalOpen(true);
        setBetForm({
            horseId: '',
            amount: 10000,
            predictedPosition: 1
        });
        await fetchHorsesForRace(race.id);
        
        try {
            const betsRes = await axiosClient.get(`/v1/spectator/bets/history/${user?.id}`);
            const betsForThisRace = betsRes.filter(b => b.raceId === race.id);
            setUserBets(betsForThisRace);
        } catch (e) {
            console.error("Lỗi lấy lịch sử cược", e);
        }
    };

    const handleBetSubmit = async (e) => {
        e.preventDefault();
        if (!betForm.horseId) {
            showToast('Vui lòng chọn ngựa để cược', 'warning');
            return;
        }

        if (betForm.amount <= 0) {
            showToast('Số tiền cược phải lớn hơn 0!', 'warning');
            return;
        }
        
        if (balance < betForm.amount) {
            showToast('Số dư không đủ! Vui lòng nạp thêm tiền.', 'error');
            return;
        }

        try {
            await axiosClient.post('/v1/spectator/bets', {
                spectatorId: user?.id, 
                raceId: selectedRace.id,
                horseId: betForm.horseId,
                amount: betForm.amount,
                predictedPosition: betForm.predictedPosition
            });
            showToast('Đặt cược thành công!', 'success');
            setBalance(prev => prev - betForm.amount);
            setIsBetModalOpen(false);
        } catch (error) {
            console.error(error);
            showToast('Lỗi khi đặt cược: ' + (error.response?.data?.error || error.message), 'error');
        }
    };

    const filteredRaces = races.filter(r => {
        if (activeTab === 'ALL') return true;
        return r.status === activeTab;
    });

    const getStatusText = (status) => {
        if (status === 'SCHEDULED') return 'Sắp diễn ra';
        if (status === 'IN_PROGRESS') return 'Đang trực tiếp';
        if (status === 'COMPLETED') return 'Đã kết thúc';
        return status;
    };

    return (
        <div className="rc-container">
            <div className="rc-header">
                <h1>Trường Đua EquineElite</h1>
                <p>Theo dõi lịch trình, xem trực tiếp và dự đoán kết quả các vòng đua</p>
                {isAuthenticated && (
                    <div className="rc-wallet-balance">
                        <span>Số dư ví: </span>
                        <span>{balance.toLocaleString('vi-VN')} VND</span>
                    </div>
                )}
            </div>

            <div className="rc-tabs-container">
                <button className={`rc-tab-btn ${activeTab === 'ALL' ? 'rc-active' : ''}`} onClick={() => setActiveTab('ALL')}>Tất Cả</button>
                <button className={`rc-tab-btn ${activeTab === 'SCHEDULED' ? 'rc-active' : ''}`} onClick={() => setActiveTab('SCHEDULED')}>Sắp Diễn Ra</button>
                <button className={`rc-tab-btn ${activeTab === 'IN_PROGRESS' ? 'rc-active' : ''}`} onClick={() => setActiveTab('IN_PROGRESS')}>Đang Trực Tiếp</button>
                <button className={`rc-tab-btn ${activeTab === 'COMPLETED' ? 'rc-active' : ''}`} onClick={() => setActiveTab('COMPLETED')}>Đã Kết Thúc</button>
            </div>

            {loading ? (
                <p style={{textAlign: 'center', color: '#94a3b8'}}>Đang tải dữ liệu trường đua...</p>
            ) : (
                <div className="rc-races-grid">
                    {filteredRaces.map(race => (
                        <div className="rc-race-card" key={race.id}>
                            <div className="rc-race-card-header">
                                <h3>{race.name}</h3>
                                <span className={`rc-status-badge ${race.status === 'SCHEDULED' ? 'rc-scheduled' : race.status === 'IN_PROGRESS' ? 'rc-in-progress' : 'rc-completed'}`}>
                                    {getStatusText(race.status)}
                                </span>
                            </div>
                            <div className="rc-race-info">
                                <p><FaCalendarAlt color="#38bdf8" /> Bắt đầu: {new Date(race.startTime).toLocaleString('vi-VN')}</p>
                                <p><FaRoute color="#10b981" /> Cự ly: {race.distance} mét</p>
                            </div>
                            
                            {race.status === 'SCHEDULED' && (
                                <button className="rc-action-btn rc-bet" onClick={() => openBetModal(race)}>
                                    <FaCoins /> 💰 Vào Tiền Ngay
                                </button>
                            )}
                            
                            {(race.status === 'SCHEDULED' || race.status === 'UPCOMING' || race.status === 'IN_PROGRESS') && (
                                <button className="rc-action-btn rc-live" onClick={() => navigate(`/races/${race.id}/live`)} style={{marginTop: '10px', background: '#3b82f6', color: 'white'}}>
                                    <FaPlayCircle /> Vào Trường Đua (Xem Live)
                                </button>
                            )}
                            
                            {race.status === 'COMPLETED' && (
                                <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                                    <button className="rc-action-btn rc-results" onClick={() => navigate(`/results`)} style={{ flex: 1 }}>
                                        <FaTrophy color="#fbbf24" /> Kết Quả
                                    </button>
                                    <button className="rc-action-btn rc-live" onClick={() => navigate(`/races/${race.id}/live`)} style={{ flex: 1, background: '#f59e0b', color: 'white' }}>
                                        <FaPlayCircle /> Xem Lại
                                    </button>
                                </div>
                            )}
                        </div>
                    ))}
                    
                    {filteredRaces.length === 0 && (
                        <div className="rc-empty-state">
                            <p>Không có vòng đua nào trong danh mục này.</p>
                        </div>
                    )}
                </div>
            )}

            {isBetModalOpen && (
                <div className="rc-bet-modal-backdrop" onClick={() => setIsBetModalOpen(false)}>
                    <div className="rc-bet-modal" onClick={e => e.stopPropagation()}>
                        <div className="rc-bet-modal-header">
                            <h2>Phiếu Đặt Cược - {selectedRace?.name}</h2>
                            <button className="rc-close-btn" onClick={() => setIsBetModalOpen(false)}><FiX size={24} /></button>
                        </div>
                        <form onSubmit={handleBetSubmit}>
                            <div className="rc-form-group">
                                <label>Chọn Ngựa Đua (Mã lực & Nài ngựa)</label>
                                <select 
                                    value={betForm.horseId} 
                                    onChange={(e) => setBetForm({...betForm, horseId: e.target.value})}
                                    required
                                >
                                    <option value="">-- Click để chọn ngựa --</option>
                                    {raceHorses.map(h => {
                                        const hasBet = userBets.some(b => b.horseId === h.horseId);
                                        return (
                                            <option key={h.horseId} value={h.horseId} disabled={hasBet}>
                                                🐎 {h.horseName} (Nài: {h.jockeyName || 'Đang cập nhật'}) {hasBet ? ' - [ BẠN ĐÃ CƯỢC CON NÀY ]' : ''}
                                            </option>
                                        );
                                    })}
                                </select>
                            </div>
                            <div className="rc-form-group">
                                <label>Dự đoán thứ hạng (Tỉ lệ ăn)</label>
                                <select
                                    value={betForm.predictedPosition}
                                    onChange={(e) => setBetForm({...betForm, predictedPosition: parseInt(e.target.value)})}
                                >
                                    <option value={1}>Về Nhất (Top 1) - Ăn x3.0</option>
                                    <option value={2}>Về Nhì (Top 2) - Ăn x2.0</option>
                                    <option value={3}>Về Ba (Top 3) - Ăn x1.5</option>
                                </select>
                            </div>
                            <div className="rc-form-group">
                                <label>Số tiền cược (VNĐ)</label>
                                <input 
                                    type="number" 
                                    min="10000" 
                                    step="10000"
                                    value={betForm.amount}
                                    onChange={(e) => setBetForm({...betForm, amount: parseInt(e.target.value)})}
                                    required
                                />
                            </div>
                            <button type="submit" className="rc-submit-btn">Chốt Kèo!</button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RaceCenter;
