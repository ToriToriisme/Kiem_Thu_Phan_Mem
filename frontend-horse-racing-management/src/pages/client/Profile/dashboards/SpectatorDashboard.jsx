import React, { useEffect, useState } from 'react';
import { FiTrendingUp, FiGift } from 'react-icons/fi';
import { useSelector } from 'react-redux';
import axiosClient from '../../../../services/axiosClient';

const SpectatorDashboard = () => {
    const { user } = useSelector((state) => state.auth || {});
    const [betHistory, setBetHistory] = useState([]);
    const [races, setRaces] = useState([]);
    const [horses, setHorses] = useState([]);
    const [balance, setBalance] = useState(0);
    const [isToppingUp, setIsToppingUp] = useState(false);
    
    useEffect(() => {
        if (user && user.id) {
            fetchData();
        }
    }, [user]);

    const fetchData = async () => {
        try {
            const [betsRes, racesRes, horsesRes, walletRes] = await Promise.all([
                axiosClient.get(`/v1/spectator/bets/history/${user.id}`),
                axiosClient.get(`/v1/spectator/races`),
                axiosClient.get(`/v1/horses`),
                axiosClient.get(`/v1/spectator/wallet/${user.id}`)
            ]);
            
            setRaces(racesRes || []);
            setHorses(horsesRes || []);
            setBalance(walletRes?.balance || 0);
            
            // Format data
            const formattedBets = (betsRes || []).map(bet => {
                const race = (racesRes || []).find(r => r.id === bet.raceId);
                const horse = (horsesRes || []).find(h => h.id === bet.horseId);
                
                let result = 'Đang chờ';
                let prize = 'Chưa có';
                if (bet.status === 'WON') {
                    result = 'Thắng';
                    prize = (bet.amount * 2).toLocaleString('vi-VN') + ' VND'; // Mock x2 for now
                } else if (bet.status === 'LOST') {
                    result = 'Thua';
                    prize = '0 VND';
                }
                
                return {
                    id: bet.id,
                    race: race ? race.name : 'Unknown Race',
                    horse: horse ? horse.name : 'Unknown Horse',
                    amount: bet.amount.toLocaleString('vi-VN') + ' VND',
                    result: result,
                    prize: prize,
                    date: new Date().toLocaleDateString('vi-VN') // Mock date
                };
            });
            
            setBetHistory(formattedBets);
        } catch (error) {
            console.error("Lỗi khi tải lịch sử cược", error);
        }
    };
        
    const handleTopUp = async () => {
        setIsToppingUp(true);
        try {
            await axiosClient.post(`/v1/spectator/wallet/top-up/${user.id}`, { amount: 1000000 });
            alert("Nạp tiền thành công 1,000,000 VND!");
            fetchData(); // Refresh balance
        } catch (error) {
            alert("Nạp tiền thất bại");
        } finally {
            setIsToppingUp(false);
        }
    };

    const totalBetAmount = betHistory.reduce((acc, curr) => acc + parseInt(curr.amount.replace(/\D/g, '') || 0), 0);
    const totalPrizeAmount = betHistory.reduce((acc, curr) => acc + parseInt(curr.prize.replace(/\D/g, '') || 0), 0);

    return (
        <div className="dashboard-wrapper fade-in">
            <h2 className="dashboard-title">Bảng điều khiển Khán Giả</h2>
            
            <div className="profile-side-column" style={{display: 'flex', gap: '20px', marginBottom: '30px'}}>
                <div className="profile-card side-card" style={{flex: 1, backgroundColor: '#f0f9ff', border: '1px solid #bae6fd'}}>
                    <h4 className="side-card-title">Số dư Ví (Wallet)</h4>
                    <div className="balance-display" style={{color: '#0284c7', display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                        <span className="balance-amount">{balance.toLocaleString('vi-VN')} <small>VND</small></span>
                        <button 
                            className="btn-primary btn-sm" 
                            onClick={handleTopUp} 
                            disabled={isToppingUp}
                        >
                            {isToppingUp ? 'Đang nạp...' : '+ Nạp 1M'}
                        </button>
                    </div>
                </div>
                <div className="profile-card side-card" style={{flex: 1}}>
                    <h4 className="side-card-title">Tổng tiền cược</h4>
                    <div className="balance-display">
                        <span className="balance-amount">{totalBetAmount.toLocaleString('vi-VN')} <small>VND</small></span>
                    </div>
                </div>
                <div className="profile-card side-card" style={{flex: 1}}>
                    <h4 className="side-card-title">Tổng tiền thắng</h4>
                    <div className="balance-display" style={{color: '#10b981'}}>
                        <span className="balance-amount">{totalPrizeAmount.toLocaleString('vi-VN')} <small>VND</small></span>
                    </div>
                </div>
            </div>

            <div className="dashboard-section">
                <div className="section-header">
                    <h3>Lịch sử dự đoán (Betting)</h3>
                    <button className="btn-outline btn-sm"><FiTrendingUp /> Thống kê</button>
                </div>
                <div className="table-responsive">
                    <table className="tm-table">
                        <thead>
                            <tr>
                                <th>Ngày</th>
                                <th>Cuộc đua</th>
                                <th>Ngựa dự đoán</th>
                                <th>Số tiền cược</th>
                                <th>Kết quả</th>
                                <th>Tiền thưởng</th>
                            </tr>
                        </thead>
                        <tbody>
                            {betHistory.map(bet => (
                                <tr key={bet.id}>
                                    <td>{bet.date}</td>
                                    <td><strong>{bet.race}</strong></td>
                                    <td>{bet.horse}</td>
                                    <td>{bet.amount}</td>
                                    <td>
                                        <span className={`profile-status-badge ${bet.result === 'Thắng' ? 'success' : bet.result === 'Thua' ? 'danger' : 'warning'}`}>
                                            {bet.result}
                                        </span>
                                    </td>
                                    <td style={{color: bet.result === 'Thắng' ? '#10b981' : 'inherit', fontWeight: 'bold'}}>
                                        {bet.prize}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default SpectatorDashboard;
