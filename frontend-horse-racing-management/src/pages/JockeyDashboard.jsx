import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import axiosClient from '../services/axiosClient';

const JockeyDashboard = () => {
    const user = useSelector(state => state.auth.user);
    const jockeyId = user?.id; // hoặc user.jockeyId tùy cấu trúc

    const [schedules, setSchedules] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState({});

    useEffect(() => {
        if (!jockeyId) {
            setError("Không tìm thấy thông tin Jockey.");
            setLoading(false);
            return;
        }

        const fetchSchedule = async () => {
            try {
                const data = await axiosClient.get(`/v1/registrations/jockey/${jockeyId}/schedule`);
                setSchedules(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchSchedule();
    }, [jockeyId]);

    // Hàm xử lý chấp nhận lịch trình
    const handleApprove = async (registrationId) => {
        setActionLoading(prev => ({ ...prev, [registrationId]: true }));
        try {
            await axiosClient.put(`/v1/registrations/${registrationId}/approve-by-jockey`);
            // Cập nhật lại danh sách sau khi chấp nhận
            const data = await axiosClient.get(`/v1/registrations/jockey/${jockeyId}/schedule`);
            setSchedules(data);
            alert("✅ Chấp nhận lịch trình thành công!");
        } catch (err) {
            alert("❌ Lỗi: " + (err.response?.data?.message || err.message));
        } finally {
            setActionLoading(prev => ({ ...prev, [registrationId]: false }));
        }
    };

    // Hàm xử lý từ chối lịch trình
    const handleReject = async (registrationId) => {
        setActionLoading(prev => ({ ...prev, [registrationId]: true }));
        try {
            await axiosClient.put(`/v1/registrations/${registrationId}/reject-by-jockey`);
            // Cập nhật lại danh sách sau khi từ chối
            const data = await axiosClient.get(`/v1/registrations/jockey/${jockeyId}/schedule`);
            setSchedules(data);
            alert("✅ Từ chối lịch trình thành công!");
        } catch (err) {
            alert("❌ Lỗi: " + (err.response?.data?.message || err.message));
        } finally {
            setActionLoading(prev => ({ ...prev, [registrationId]: false }));
        }
    };

    if (loading) return <div>Đang tải...</div>;
    if (error) return <div style={{ color: 'red' }}>Lỗi: {error}</div>;

    return (
        <div style={{ padding: 20 }}>
            <h2>📅 Lịch trình & Phân công của Jockey</h2>
            {schedules.length === 0 ? (
                <p>Chưa có lịch trình nào.</p>
            ) : (
                <table border="1" cellPadding="8" style={{ width: '100%', borderCollapse: 'collapse', marginTop: 20 }}>
                    <thead style={{ backgroundColor: '#f2f2f2' }}>
                    <tr>
                        <th>Giải đấu</th>
                        <th>Ngày bắt đầu</th>
                        <th>Ngày kết thúc</th>
                        <th>Ngựa</th>
                        <th>Trạng thái</th>
                        <th>Hành động</th>
                    </tr>
                    </thead>
                    <tbody>
                    {schedules.map(item => (
                        <tr key={item.registrationId}>
                            <td>{item.tournamentName}</td>
                            <td>{new Date(item.startDate).toLocaleDateString('vi-VN')}</td>
                            <td>{new Date(item.endDate).toLocaleDateString('vi-VN')}</td>
                            <td>{item.horseName}</td>
                            <td>
                                <span style={{
                                    padding: '4px 8px',
                                    borderRadius: 4,
                                    backgroundColor: item.status === 'APPROVED' ? '#d4edda' : item.status === 'REJECTED' ? '#f8d7da' : '#fff3cd',
                                    color: item.status === 'APPROVED' ? '#155724' : item.status === 'REJECTED' ? '#721c24' : '#856404'
                                }}>
                                    {item.status === 'APPROVED' ? '✓ Đã chấp nhận' : item.status === 'REJECTED' ? '✗ Đã từ chối' : '⏳ Chờ xử lý'}
                                </span>
                            </td>
                            <td style={{ textAlign: 'center' }}>
                                {item.status !== 'APPROVED' && item.status !== 'REJECTED' ? (
                                    <>
                                        <button
                                            onClick={() => handleApprove(item.registrationId)}
                                            disabled={actionLoading[item.registrationId]}
                                            style={{
                                                padding: '5px 12px',
                                                marginRight: '5px',
                                                backgroundColor: '#28a745',
                                                color: 'white',
                                                border: 'none',
                                                borderRadius: '4px',
                                                cursor: actionLoading[item.registrationId] ? 'not-allowed' : 'pointer',
                                                opacity: actionLoading[item.registrationId] ? 0.6 : 1
                                            }}
                                        >
                                            {actionLoading[item.registrationId] ? '⏳ ...' : '✓ Chấp nhận'}
                                        </button>
                                        <button
                                            onClick={() => handleReject(item.registrationId)}
                                            disabled={actionLoading[item.registrationId]}
                                            style={{
                                                padding: '5px 12px',
                                                backgroundColor: '#dc3545',
                                                color: 'white',
                                                border: 'none',
                                                borderRadius: '4px',
                                                cursor: actionLoading[item.registrationId] ? 'not-allowed' : 'pointer',
                                                opacity: actionLoading[item.registrationId] ? 0.6 : 1
                                            }}
                                        >
                                            {actionLoading[item.registrationId] ? '⏳ ...' : '✗ Từ chối'}
                                        </button>
                                    </>
                                ) : (
                                    <span style={{ color: '#999' }}>-</span>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default JockeyDashboard;