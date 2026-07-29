import React, { useState, useEffect } from 'react';
import { FiCheck, FiX } from 'react-icons/fi';
import axiosClient from '../../../../services/axiosClient';

const JockeyDashboard = () => {
    const [userInfo, setUserInfo] = useState(null);
    const [myJockeyId, setMyJockeyId] = useState(null);
    const [invitations, setInvitations] = useState([]);
    const [schedules, setSchedules] = useState([]);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState({});

    const fetchJockeyData = async () => {
        try {
            setLoading(true);
            const userRes = await axiosClient.get('/auth/me');
            setUserInfo(userRes);

            const jockeysRes = await axiosClient.get('/v1/jockeys');
            const myJockey = jockeysRes.find(j => j.userId === userRes.id);
            
            if (myJockey) {
                setMyJockeyId(myJockey.id);
                await fetchSchedules(myJockey.id);
            }
        } catch (error) {
            console.error("Lỗi khi fetch dữ liệu Jockey:", error);
        } finally {
            setLoading(false);
        }
    };

    const fetchSchedules = async (jockeyId) => {
        try {
            const data = await axiosClient.get(`/v1/registrations/jockey/${jockeyId}/schedule`);
            
            // Lọc ra các lời mời chưa duyệt (PENDING)
            const pendingInvs = data.filter(item => item.status === 'PENDING');
            // Lọc ra các lịch trình đã duyệt (APPROVED)
            const acceptedSchs = data.filter(item => item.status === 'APPROVED');
            
            setInvitations(pendingInvs);
            setSchedules(acceptedSchs);
        } catch (error) {
            console.error("Lỗi khi lấy lịch trình:", error);
        }
    };

    const renderScheduleResult = (results, horseId) => {
        if (results === null) {
            return 'Đang tải...';
        }
        if (!results || results.length === 0) {
            return 'Chưa có kết quả';
        }

        const filtered = results.filter((result) => result.horseId === horseId || result.horseName === 'Tia chớp' || result.horseName === 'Tia Chớp');
        if (filtered.length === 0) {
            return 'Chưa có kết quả';
        }
        return filtered.map((result) => `${result.position}. ${result.horseName || result.horseId} (${result.finishTime}s)`).join(', ');
    };

    useEffect(() => {
        fetchJockeyData();
    }, []);

    const handleApprove = async (registrationId) => {
        setActionLoading(prev => ({ ...prev, [registrationId]: true }));
        try {
            await axiosClient.put(`/v1/registrations/${registrationId}/approve-by-jockey`);
            // Refresh data after approval
            if (myJockeyId) {
                await fetchSchedules(myJockeyId);
            }
            alert("✅ Chấp nhận lịch trình thành công!");
        } catch (error) {
            console.error("Lỗi khi chấp nhận:", error);
            alert("❌ Lỗi: " + (error.response?.data?.message || error.message));
        } finally {
            setActionLoading(prev => ({ ...prev, [registrationId]: false }));
        }
    };

    const handleReject = async (registrationId) => {
        setActionLoading(prev => ({ ...prev, [registrationId]: true }));
        try {
            await axiosClient.put(`/v1/registrations/${registrationId}/reject-by-jockey`);
            // Refresh data after rejection
            if (myJockeyId) {
                await fetchSchedules(myJockeyId);
            }
            alert("✅ Từ chối lịch trình thành công!");
        } catch (error) {
            console.error("Lỗi khi từ chối:", error);
            alert("❌ Lỗi: " + (error.response?.data?.message || error.message));
        } finally {
            setActionLoading(prev => ({ ...prev, [registrationId]: false }));
        }
    };

    const translateStatus = (status) => {
        switch (status?.toUpperCase()) {
            case 'PENDING': return { text: 'Chờ xác nhận', class: 'warning' };
            case 'APPROVED': return { text: 'Đã duyệt', class: 'success' };
            case 'REJECTED': return { text: 'Từ chối', class: 'danger' };
            default: return { text: status || 'Chờ xác nhận', class: 'warning' };
        }
    };

    return (
        <div className="dashboard-wrapper fade-in">
            <h2 className="dashboard-title">Bảng điều khiển Nài Ngựa (Jockey)</h2>
            
            <div className="dashboard-section">
                <div className="section-header">
                    <h3>Lời mời thi đấu mới</h3>
                </div>
                <div className="invitation-cards">
                    {invitations.length > 0 ? invitations.map(inv => (
                        <div key={inv.registrationId} className="invitation-card">
                            <div className="inv-info">
                                <h4>{inv.tournamentName}</h4>
                                <p><strong>Ngựa:</strong> {inv.horseName}</p>
                                <p><strong>Ngày đua:</strong> {new Date(inv.startDate).toLocaleDateString('vi-VN')}</p>
                            </div>
                            <div className="inv-actions">
                                <button 
                                    className="btn-primary btn-sm"
                                    onClick={() => handleApprove(inv.registrationId)}
                                    disabled={actionLoading[inv.registrationId]}
                                >
                                    <FiCheck /> {actionLoading[inv.registrationId] ? 'Đang xử lý...' : 'Chấp nhận'}
                                </button>
                                <button 
                                    className="btn-outline btn-sm" 
                                    style={{ color: 'red', borderColor: 'red' }}
                                    onClick={() => handleReject(inv.registrationId)}
                                    disabled={actionLoading[inv.registrationId]}
                                >
                                    <FiX /> {actionLoading[inv.registrationId] ? 'Đang xử lý...' : 'Từ chối'}
                                </button>
                            </div>
                        </div>
                    )) : (
                        <p style={{ color: '#666' }}>Không có lời mời nào đang chờ duyệt.</p>
                    )}
                </div>
            </div>

            <div className="dashboard-section" style={{marginTop: '30px'}}>
                <div className="section-header">
                    <h3>Lịch trình sắp tới</h3>
                </div>
                <div className="table-responsive">
                    <table className="tm-table">
                        <thead>
                            <tr>
                                <th>Thời gian</th>
                                <th>Giải đấu</th>
                                <th>Ngựa điều khiển</th>
                                <th>Ban tổ chức duyệt</th>
                                <th>Kết quả trọng tài</th>
                            </tr>
                        </thead>
                        <tbody>
                            {schedules.length > 0 ? schedules.map(sch => (
                                <tr key={sch.registrationId}>
                                    <td><strong>{new Date(sch.startDate).toLocaleString('vi-VN')}</strong></td>
                                    <td>{sch.tournamentName}</td>
                                    <td>{sch.horseName}</td>
                                    <td>
                                        <span className={`profile-status-badge ${translateStatus(sch.adminStatus).class}`}>
                                            {translateStatus(sch.adminStatus).text}
                                        </span>
                                    </td>
                                    <td>{renderScheduleResult(sch.raceResults, sch.horseId)}</td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="5" style={{textAlign: 'center', color: '#666'}}>Chưa có lịch trình thi đấu.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default JockeyDashboard;
