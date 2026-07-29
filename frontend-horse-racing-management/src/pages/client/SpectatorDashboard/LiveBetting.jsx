import React, { useEffect, useState } from 'react';
import axiosClient from '../../../services/axiosClient';
import { showToast } from '../../../utils/alertUtils';
import { FaCalendarAlt, FaRoute, FaCoins, FaHorse } from 'react-icons/fa';
import { FiX } from 'react-icons/fi';
import './LiveBetting.css';
import { useSelector } from 'react-redux';

const LiveBetting = () => {
    const { user } = useSelector((state) => state.auth || {});
    const [races, setRaces] = useState([]);
    const [loading, setLoading] = useState(false);
    const [balance, setBalance] = useState(0);
    
    // Modal states
    const [isBetModalOpen, setIsBetModalOpen] = useState(false);
    const [selectedRace, setSelectedRace] = useState(null);
    const [raceHorses, setRaceHorses] = useState([]);
    
    const [betForm, setBetForm] = useState({
        horseId: '',
        amount: 10000,
        predictedPosition: 1
    });

    useEffect(() => {
        fetchLiveRaces();
    }, []);

    const fetchLiveRaces = async () => {
        setLoading(true);
        try {
            const response = await axiosClient.get('/v1/spectator/races/live');
            const liveRaces = (response || []).filter(r => r.status === 'IN_PROGRESS' || r.status === 'SCHEDULED');
            setRaces(liveRaces);
            
            // Fetch user wallet balance
            if (user && user.id) {
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
        setSelectedRace(race);
        setIsBetModalOpen(true);
        setBetForm({
            horseId: '',
            amount: 10000,
            predictedPosition: 1
        });
        await fetchHorsesForRace(race.id);
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
                spectatorId: user?.id || 'spectator-1', 
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

    return (
        <div className="spectator-dashboard">
            <div className="spectator-header">
                <h1>Trường Đua EquineElite</h1>
                <p>Theo dõi các vòng đua hấp dẫn và thử tài dự đoán của bạn</p>
                <div className="betting-balance" style={{ marginTop: '10px', fontSize: '1.1rem', fontWeight: 'bold' }}>
                    <span>Số dư hiện tại: </span>
                    <span style={{ color: '#2563eb' }}>{balance.toLocaleString('vi-VN')} VND</span>
                </div>
            </div>

            {loading ? (
                <p>Đang tải dữ liệu...</p>
            ) : (
                <div className="races-grid">
                    {races.map(race => (
                        <div className="race-card" key={race.id}>
                            <div className="race-card-header">
                                <h3>{race.name}</h3>
                                <span className={`status-badge ${String(race.status).toLowerCase()}`}>
                                    {race.status === 'SCHEDULED' ? 'Sắp diễn ra' : 'Đang đua'}
                                </span>
                            </div>
                            <div className="race-info">
                                <p><FaCalendarAlt /> Bắt đầu: {new Date(race.startTime).toLocaleString('vi-VN')}</p>
                                <p><FaRoute /> Cự ly: {race.distance} mét</p>
                            </div>
                            <button 
                                className="bet-btn" 
                                onClick={() => openBetModal(race)}
                                disabled={race.status !== 'SCHEDULED'}
                            >
                                <FaCoins /> 
                                {race.status === 'SCHEDULED' ? 'Đặt Cược Ngay' : 'Đã Đóng Cược'}
                            </button>
                        </div>
                    ))}
                    {races.length === 0 && (
                        <div style={{gridColumn: '1 / -1', textAlign: 'center', padding: '40px'}}>
                            <p style={{fontSize: '1.2rem', color: '#64748b'}}>Hiện tại không có vòng đua nào đang mở.</p>
                        </div>
                    )}
                </div>
            )}

            {isBetModalOpen && (
                <div className="spec-modal-backdrop" onClick={() => setIsBetModalOpen(false)}>
                    <div className="spec-modal" onClick={e => e.stopPropagation()}>
                        <div className="spec-modal-header">
                            <h2>Đặt Cược - {selectedRace?.name}</h2>
                            <button className="close-btn" onClick={() => setIsBetModalOpen(false)}><FiX /></button>
                        </div>
                        <form onSubmit={handleBetSubmit}>
                            <div className="form-group">
                                <label>Chọn Ngựa Đua</label>
                                <select 
                                    value={betForm.horseId} 
                                    onChange={(e) => setBetForm({...betForm, horseId: e.target.value})}
                                    required
                                >
                                    <option value="">-- Vui lòng chọn ngựa --</option>
                                    {raceHorses.map(h => (
                                        <option key={h.horse.id} value={h.horse.id}>
                                            {h.horse.name} (Nài: {h.jockey?.name || 'Chưa rõ'})
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Dự đoán thứ hạng (1 = Về Nhất)</label>
                                <select
                                    value={betForm.predictedPosition}
                                    onChange={(e) => setBetForm({...betForm, predictedPosition: parseInt(e.target.value)})}
                                >
                                    <option value={1}>Về Nhất (Top 1)</option>
                                    <option value={2}>Về Nhì (Top 2)</option>
                                    <option value={3}>Về Ba (Top 3)</option>
                                </select>
                            </div>
                            <div className="form-group">
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
                            <button type="submit" className="submit-btn">Xác nhận cược</button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LiveBetting;
