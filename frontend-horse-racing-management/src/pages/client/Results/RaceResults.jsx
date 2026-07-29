import React, { useEffect, useState } from 'react';
import axiosClient from '../../../services/axiosClient';
import { FaTrophy, FaCalendarCheck, FaArrowDown, FaRegClock } from 'react-icons/fa';
import './RaceResults.css';

const RaceResults = () => {
    const [groupedData, setGroupedData] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchResults();
    }, []);

    const fetchResults = async () => {
        setLoading(true);
        try {
            // Lấy danh sách các giải đấu
            const tournamentsRes = await axiosClient.get('/admin/tournaments');
            // Lấy danh sách TOÀN BỘ các cuộc đua (không filter COMPLETED nữa)
            const racesRes = await axiosClient.get('/v1/spectator/races');
            
            let finalGroups = [];

            if (!racesRes || racesRes.length === 0 || !tournamentsRes || tournamentsRes.length === 0) {
                // MOCK DATA ĐỂ DEMO (nếu DB rỗng)
                finalGroups = buildMockData();
            } else {
                // Có dữ liệu thực
                const mapTournaments = {};
                
                for (const t of tournamentsRes) {
                    mapTournaments[t.id] = {
                        id: t.id,
                        name: t.name,
                        races: []
                    };
                }

                // Chạy Promise.all để fetch danh sách ngựa và kết quả song song cho nhanh
                await Promise.all(racesRes.map(async (r) => {
                    const tId = r.tournamentId;
                    if (!mapTournaments[tId]) return;

                    let participants = [];
                    
                    // 1. Fetch danh sách ngựa đã đăng ký/phân bổ cho race này
                    try {
                        const horsesRes = await axiosClient.get(`/referee/race/${r.id}/horses`);
                        if (horsesRes && horsesRes.length > 0) {
                            participants = horsesRes.map((h, index) => ({
                                horseName: h.horseName || h.name,
                                jockeyName: h.jockeyName || 'Chưa xếp nài',
                                position: null,
                                finishTime: null,
                                prizeMoney: 0,
                                index: index + 1 // Dùng tạm làm STT nếu chưa đua
                            }));
                        }
                    } catch (e) {
                        // Bỏ qua lỗi nếu chưa có ngựa
                    }

                    // 2. Nếu race đã hoàn thành, fetch kết quả
                    if (r.status === 'COMPLETED') {
                        try {
                            const resultsRes = await axiosClient.get(`/referee/race/${r.id}/results`);
                            if (resultsRes && resultsRes.length > 0) {
                                participants = resultsRes.map(res => ({
                                    horseName: res.horseName,
                                    jockeyName: res.jockeyName,
                                    position: res.position,
                                    finishTime: res.finishTime,
                                    prizeMoney: res.prizeMoney || 0,
                                    index: res.position
                                }));
                            }
                        } catch (e) {
                            console.error('Lỗi lấy kết quả:', e);
                        }
                    }

                    // Sắp xếp: nếu có position thì xếp theo position, nếu không thì xếp theo index (random)
                    participants.sort((a, b) => (a.position || a.index) - (b.position || b.index));

                    mapTournaments[tId].races.push({
                        id: r.id,
                        name: r.name,
                        status: r.status,
                        startTime: r.startTime,
                        advancingCount: r.advancingCount || 3, // Default top 3 nếu không có
                        participants: participants
                    });
                }));

                // Sắp xếp các races trong tournament theo thứ tự thời gian (để ra Vòng 1 -> Vòng 2 -> Chung kết)
                Object.values(mapTournaments).forEach(t => {
                    t.races.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
                });

                finalGroups = Object.values(mapTournaments).filter(t => t.races.length > 0);
            }

            setGroupedData(finalGroups);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    const buildMockData = () => {
        return [
            {
                id: 'mock-tour',
                name: 'Giải Vô Địch Mùa Hè 2026',
                races: [
                    {
                        id: 'mock-race-1',
                        name: 'Vòng Loại Bảng A',
                        status: 'COMPLETED',
                        advancingCount: 5,
                        participants: [
                            { horseName: 'Độc Cô Cầu Bại', jockeyName: 'Phạm Đức', position: 1, finishTime: 45.21, prizeMoney: 50000000 },
                            { horseName: 'Bạch Long', jockeyName: 'Hoàng Yến', position: 2, finishTime: 45.89, prizeMoney: 20000000 },
                            { horseName: 'Xích Thố', jockeyName: 'Vũ Nam', position: 3, finishTime: 46.12, prizeMoney: 10000000 },
                            { horseName: 'Hỏa Tiễn', jockeyName: 'Trần B', position: 4, finishTime: 46.55, prizeMoney: 0 },
                            { horseName: 'Cuồng Phong', jockeyName: 'Lê C', position: 5, finishTime: 46.90, prizeMoney: 0 },
                            { horseName: 'Phi Yến', jockeyName: 'Ngô D', position: 6, finishTime: 47.20, prizeMoney: 0 },
                            { horseName: 'Hắc Điểu', jockeyName: 'Đinh E', position: 7, finishTime: 47.80, prizeMoney: 0 },
                            { horseName: 'Lôi Thần', jockeyName: 'Lý G', position: 8, finishTime: 48.10, prizeMoney: 0 },
                            { horseName: 'Bão Cát', jockeyName: 'Trương H', position: 9, finishTime: 48.95, prizeMoney: 0 },
                            { horseName: 'Bóng Đêm', jockeyName: 'Phan K', position: 10, finishTime: 50.11, prizeMoney: 0 },
                        ]
                    },
                    {
                        id: 'mock-race-2',
                        name: 'Vòng Chung Kết (Chưa diễn ra)',
                        status: 'UPCOMING',
                        advancingCount: 3,
                        participants: [
                            { horseName: 'Độc Cô Cầu Bại', jockeyName: 'Phạm Đức', position: null, finishTime: null, prizeMoney: 0, index: 1 },
                            { horseName: 'Bạch Long', jockeyName: 'Hoàng Yến', position: null, finishTime: null, prizeMoney: 0, index: 2 },
                            { horseName: 'Xích Thố', jockeyName: 'Vũ Nam', position: null, finishTime: null, prizeMoney: 0, index: 3 },
                            { horseName: 'Hỏa Tiễn', jockeyName: 'Trần B', position: null, finishTime: null, prizeMoney: 0, index: 4 },
                            { horseName: 'Cuồng Phong', jockeyName: 'Lê C', position: null, finishTime: null, prizeMoney: 0, index: 5 },
                        ]
                    }
                ]
            }
        ];
    };

    if (loading) {
        return (
            <div className="rr-container">
                <div className="rr-loading">Đang tổng hợp Bảng Phong Thần...</div>
            </div>
        );
    }

    const formatMoney = (amount) => {
        if (!amount) return '0 ₫';
        return '+' + amount.toLocaleString('vi-VN') + ' ₫';
    };

    const getStatusText = (status) => {
        switch (status) {
            case 'SCHEDULED': return 'Đã lên lịch';
            case 'UPCOMING': return 'Sắp khởi tranh';
            case 'ONGOING': return 'Đang diễn ra';
            case 'COMPLETED': return 'Đã kết thúc';
            default: return status;
        }
    };

    const getStatusClass = (status) => {
        switch (status) {
            case 'SCHEDULED': return 'rr-status-scheduled';
            case 'UPCOMING': return 'rr-status-upcoming';
            case 'ONGOING': return 'rr-status-ongoing';
            case 'COMPLETED': return 'rr-status-completed';
            default: return '';
        }
    };

    return (
        <div className="rr-container fade-in">
            <div className="rr-header">
                <h1 className="rr-title">
                    <FaTrophy style={{color: '#f59e0b'}}/> Sơ Đồ & Kết Quả Giải Đấu
                </h1>
                <p className="rr-subtitle">Theo dõi luồng thi đấu từ Vòng bảng đến Chung kết. Những chiến mã xuất sắc nhất sẽ được tiến vào vòng trong.</p>
            </div>

            {groupedData.length === 0 ? (
                <div className="rr-loading">Hệ thống chưa có dữ liệu giải đấu nào.</div>
            ) : (
                groupedData.map(tournament => (
                    <div key={tournament.id} className="rr-tournament-group">
                        <div className="rr-tournament-header">
                            <h2 className="rr-tournament-title">
                                <FaCalendarCheck className="rr-tournament-title-icon" />
                                {tournament.name}
                            </h2>
                        </div>
                        
                        {tournament.races.map((race, rIndex) => {
                            const isCompleted = race.status === 'COMPLETED';
                            // Nếu race chưa có ngựa nào (ví dụ vòng sau chưa bắt đầu), ta vẽ table trống để giữ khung
                            // Số row trống = số ngựa nhận (hoặc x2 để ước chừng số lượng)
                            const displayRows = race.participants.length > 0 
                                ? race.participants 
                                : Array.from({ length: race.advancingCount * 2 || 6 }).map((_, i) => ({
                                    horseName: '', jockeyName: '', position: null, index: i + 1, isEmpty: true
                                }));

                            return (
                                <React.Fragment key={race.id}>
                                    <div className="rr-race-wrapper">
                                        <div className="rr-race-section">
                                            <div className="rr-race-header">
                                                <h3 className="rr-race-title">
                                                    {race.name}
                                                </h3>
                                                <div className="rr-meta-info">
                                                    <span className={`rr-status-badge ${getStatusClass(race.status)}`}>
                                                        {getStatusText(race.status)}
                                                    </span>
                                                    <span className="rr-status-badge rr-status-scheduled">
                                                        Chỉ Tiêu Đi Tiếp: Top {race.advancingCount}
                                                    </span>
                                                </div>
                                            </div>
                                            
                                            <div className="rr-table-wrapper">
                                                <table className="rr-table">
                                                    <thead>
                                                        <tr>
                                                            <th style={{width: '80px'}}>Hạng</th>
                                                            <th>Chiến Mã</th>
                                                            <th>Nài Ngựa</th>
                                                            <th>Thành Tích</th>
                                                            <th>Tiền Thưởng</th>
                                                            <th>Trạng Thái</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {displayRows.map((p, pIndex) => {
                                                            if (p.isEmpty) {
                                                                return (
                                                                    <tr key={`empty-${p.index}`} className="rr-row-empty">
                                                                        <td><div className="rr-rank-box rr-rank-pending">-</div></td>
                                                                        <td><span className="rr-placeholder">Chờ xác định...</span></td>
                                                                        <td><span className="rr-placeholder">Chờ xác định...</span></td>
                                                                        <td><span className="rr-prize-empty">--</span></td>
                                                                        <td><span className="rr-prize-empty">--</span></td>
                                                                        <td><span className="rr-badge-pending">Chờ đua</span></td>
                                                                    </tr>
                                                                );
                                                            }

                                                            // Đã có danh sách nhưng CHƯA CÓ KẾT QUẢ (chưa đua)
                                                            if (!isCompleted) {
                                                                return (
                                                                    <tr key={`pending-${p.horseName}-${p.index}`} className="rr-row-empty">
                                                                        <td><div className="rr-rank-box rr-rank-pending">-</div></td>
                                                                        <td><span className="rr-horse-name">{p.horseName}</span></td>
                                                                        <td><span className="rr-jockey-name">{p.jockeyName}</span></td>
                                                                        <td><span className="rr-prize-empty">--</span></td>
                                                                        <td><span className="rr-prize-empty">--</span></td>
                                                                        <td><span className="rr-badge-pending"><FaRegClock style={{marginRight: '5px'}}/> Chuẩn bị</span></td>
                                                                    </tr>
                                                                );
                                                            }

                                                            // ĐÃ CÓ KẾT QUẢ
                                                            const isSuccess = p.position != null && p.position !== 99 && p.position <= race.advancingCount;
                                                            const rowClass = isSuccess ? 'rr-row-success' : 'rr-row-error';
                                                            
                                                            let rankClass = 'rr-rank-other-error';
                                                            if (p.position === 99) rankClass = 'rr-rank-disqualified';
                                                            else if (p.position === 1) rankClass = 'rr-rank-1';
                                                            else if (p.position === 2) rankClass = 'rr-rank-2';
                                                            else if (p.position === 3) rankClass = 'rr-rank-3';
                                                            else if (isSuccess) rankClass = 'rr-rank-other-success';

                                                            return (
                                                                <tr key={`result-${p.horseName}-${p.position}`} className={rowClass}>
                                                                    <td>
                                                                        <div className={`rr-rank-box ${rankClass}`}>
                                                                            {p.position === 99 ? 'Loại' : p.position}
                                                                        </div>
                                                                    </td>
                                                                    <td>
                                                                        <span className="rr-horse-name">{p.horseName}</span>
                                                                    </td>
                                                                    <td>
                                                                        <span className="rr-jockey-name">{p.jockeyName}</span>
                                                                    </td>
                                                                    <td>
                                                                        <span className="rr-time">{p.finishTime}s</span>
                                                                    </td>
                                                                    <td>
                                                                        {p.prizeMoney > 0 ? (
                                                                            <span className="rr-prize-success">{formatMoney(p.prizeMoney)}</span>
                                                                        ) : (
                                                                            <span className="rr-prize-empty">---</span>
                                                                        )}
                                                                    </td>
                                                                    <td>
                                                                        {isSuccess ? (
                                                                            <span className="rr-badge-success">Vào Vòng Trong</span>
                                                                        ) : (
                                                                            <span className="rr-badge-error">Bị Loại</span>
                                                                        )}
                                                                    </td>
                                                                </tr>
                                                            );
                                                        })}
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </div>
                                    
                                    {/* Mũi tên chỉ xuống vòng tiếp theo (nếu không phải race cuối cùng) */}
                                    {rIndex < tournament.races.length - 1 && (
                                        <FaArrowDown className="rr-flow-arrow" />
                                    )}
                                </React.Fragment>
                            );
                        })}
                    </div>
                ))
            )}
        </div>
    );
};

export default RaceResults;
