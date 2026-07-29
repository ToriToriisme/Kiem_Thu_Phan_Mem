import React, { useState, useEffect } from 'react';
import { FiPlus, FiList } from 'react-icons/fi';
import axiosClient from '../../../../services/axiosClient';

const HorseOwnerDashboard = () => {
    const [userInfo, setUserInfo] = useState(null);
    const [myHorses, setMyHorses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [newHorse, setNewHorse] = useState({ name: '', age: '' });
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [jockeyRequests, setJockeyRequests] = useState([]);

    // For Registration Modal
    const [isRegModalOpen, setIsRegModalOpen] = useState(false);
    const [tournaments, setTournaments] = useState([]);
    const [jockeys, setJockeys] = useState([]);
    const [regForm, setRegForm] = useState({ tournamentId: '', horseId: '', jockeyId: '' });
    const [isRegSubmitting, setIsRegSubmitting] = useState(false);

    // Fetch dữ liệu từ API
    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            
            // SỬA: Thay apiClient bằng axiosClient và bỏ .data vì interceptor đã xử lý
            const userRes = await axiosClient.get('/auth/me');
            setUserInfo(userRes);
            
            // SỬA: Truyền trực tiếp userRes.id
            const horsesRes = await axiosClient.get(`/v1/horses/owner/${userRes.id}`);
            setMyHorses(horsesRes);
            
            // THÊM MỚI: Lấy danh sách yêu cầu của Chủ Ngựa
            const requestsRes = await axiosClient.get(`/v1/registrations/owner/${userRes.id}/requests`);
            setJockeyRequests(requestsRes);
        } catch (err) {
            console.error("Lỗi tải dữ liệu:", err);
            // Giữ mock data nếu lỗi
        } finally {
            setLoading(false);
        }
    };

    const fetchDropdownData = async () => {
        try {
            const [tRes, jRes] = await Promise.all([
                axiosClient.get('/admin/tournaments'),
                axiosClient.get('/v1/jockeys')
            ]);
            setTournaments(tRes);
            setJockeys(jRes);
        } catch (error) {
            console.error("Lỗi tải data cho modal đăng ký:", error);
        }
    };

    useEffect(() => {
        fetchDashboardData();
        fetchDropdownData();
    }, []);

    // Xử lý thêm ngựa mới
    const handleAddHorse = async (e) => {
        e.preventDefault();
        try {
            setIsSubmitting(true);
            
            // SỬA: Thay apiClient bằng axiosClient
            await axiosClient.post('/v1/horses', {
                ...newHorse,
                age: parseInt(newHorse.age),
                ownerId: userInfo?.id
            });
            
            setIsModalOpen(false);
            setNewHorse({ name: '', age: '' });
            await fetchDashboardData();
        } catch (err) {
            console.error("Lỗi khi thêm ngựa:", err);
            alert("Có lỗi xảy ra khi thêm ngựa mới.");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            setIsRegSubmitting(true);
            await axiosClient.post('/v1/registrations/register', regForm);
            alert("🎉 Đăng ký thành công! Đã gửi lời mời đến Jockey.");
            setIsRegModalOpen(false);
            setRegForm({ tournamentId: '', horseId: '', jockeyId: '' });
            await fetchDashboardData(); // Refresh data ngay lập tức
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || error.response?.data || "Đã có lỗi xảy ra!");
        } finally {
            setIsRegSubmitting(false);
        }
    };

    const renderOwnerRefereeResult = (results, horseId) => {
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
        return filtered.map((result) => `${result.position}. ${result.horseName || result.horseId}`).join(', ');
    };

    const translateStatus = (status) => {
        switch (status?.toUpperCase()) {
            case 'PENDING': return { text: 'Chờ xác nhận', class: 'warning' };
            case 'APPROVED': return { text: 'Đã duyệt', class: 'success' };
            case 'REJECTED': return { text: 'Từ chối', class: 'danger' };
            case 'HEALTHY': return { text: 'Khỏe mạnh', class: 'success' };
            case 'SICK': return { text: 'Đang ốm', class: 'danger' };
            case 'ACCEPTED': return { text: 'Đã chấp nhận', class: 'success' };
            case 'DECLINED': return { text: 'Đã từ chối', class: 'danger' };
            default: return { text: status || 'Chờ xác nhận', class: 'warning' };
        }
    };

    return (
        <div className="dashboard-wrapper fade-in">
            <h2 className="dashboard-title">Bảng điều khiển Chủ Ngựa</h2>
            
            <div className="dashboard-section">
                <div className="section-header">
                    <h3>Ngựa của tôi</h3>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <button 
                            className="btn-primary btn-sm" 
                            style={{ backgroundColor: '#10b981' }}
                            onClick={() => setIsRegModalOpen(true)}
                        >
                            Tham gia giải đấu
                        </button>
                        <button 
                            className="btn-primary btn-sm" 
                            onClick={() => setIsModalOpen(true)}
                        >
                            <FiPlus /> Thêm ngựa mới
                        </button>
                    </div>
                </div>
                <div className="table-responsive">
                    <table className="tm-table">
                        <thead>
                            <tr>
                                <th>Tên ngựa</th>
                                <th>Tuổi</th>
                                <th>Số trận thắng</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            {myHorses.length > 0 ? myHorses.map(horse => (
                                <tr key={horse.id}>
                                    <td><strong>{horse.name}</strong></td>
                                    <td>{horse.age}</td>
                                    <td>{horse.wins}</td>
                                    <td>
                                        <span className={`profile-status-badge ${translateStatus(horse.status).class}`}>
                                            {translateStatus(horse.status).text}
                                        </span>
                                    </td>
                                    <td>
                                        <button className="btn-secondary btn-sm">Chi tiết</button>
                                    </td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="5" style={{textAlign: 'center', padding: '20px', color: '#6b7280'}}>Không có dữ liệu</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            <div className="dashboard-section" style={{marginTop: '30px'}}>
                <div className="section-header">
                    <h3>Quản lý Jockey (Nài ngựa)</h3>
                    <button className="btn-outline btn-sm"><FiList /> Xem tất cả</button>
                </div>
                <div className="table-responsive">
                    <table className="tm-table">
                        <thead>
                            <tr>
                                <th>Jockey</th>
                                <th>Ngựa</th>
                                <th>Giải đấu</th>
                                <th>Nài ngựa xác nhận</th>
                                <th>Ban tổ chức duyệt</th>
                                <th>Kết quả trọng tài</th>
                            </tr>
                        </thead>
                        <tbody>
                            {jockeyRequests.length > 0 ? jockeyRequests.map(req => (
                                <tr key={req.registrationId}>
                                    <td>{req.jockeyName || 'Chưa chọn'}</td>
                                    <td>{req.horseName}</td>
                                    <td>{req.tournamentName}</td>
                                    <td>
                                        <span className={`profile-status-badge ${translateStatus(req.status).class}`}>
                                            {translateStatus(req.status).text}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`profile-status-badge ${translateStatus(req.adminStatus).class}`}>
                                            {translateStatus(req.adminStatus).text}
                                        </span>
                                    </td>
                                    <td>{renderOwnerRefereeResult(req.raceResults, req.horseId)}</td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="6" style={{textAlign: 'center', padding: '20px', color: '#6b7280'}}>Không có dữ liệu</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

           {/* MODAL THÊM NGỰA MỚI */}
            {isModalOpen && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    zIndex: 9999
                }}>
                    <div style={{
                        backgroundColor: '#fff', padding: '24px', borderRadius: '8px',
                        width: '100%', maxWidth: '400px', boxShadow: '0 10px 15px rgba(0,0,0,0.1)'
                    }}>
                        <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#333', marginTop: 0, marginBottom: '20px' }}>
                            Thêm ngựa mới
                        </h3>
                        <form onSubmit={handleAddHorse}>
                            <div style={{ marginBottom: '16px' }}>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#4a5568', marginBottom: '8px' }}>
                                    Tên ngựa
                                </label>
                                <input 
                                    type="text" 
                                    required
                                    style={{
                                        width: '100%', padding: '10px 12px', border: '1px solid #cbd5e0', 
                                        borderRadius: '6px', outline: 'none', boxSizing: 'border-box'
                                    }}
                                    value={newHorse.name}
                                    onChange={(e) => setNewHorse({...newHorse, name: e.target.value})}
                                    placeholder="Ví dụ: Tía Chớp"
                                />
                            </div>
                            <div style={{ marginBottom: '24px' }}>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#4a5568', marginBottom: '8px' }}>
                                    Tuổi
                                </label>
                                <input 
                                    type="number" 
                                    required
                                    min="1"
                                    style={{
                                        width: '100%', padding: '10px 12px', border: '1px solid #cbd5e0', 
                                        borderRadius: '6px', outline: 'none', boxSizing: 'border-box'
                                    }}
                                    value={newHorse.age}
                                    onChange={(e) => setNewHorse({...newHorse, age: e.target.value})}
                                    placeholder="Ví dụ: 3"
                                />
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                                <button 
                                    type="button" 
                                    onClick={() => setIsModalOpen(false)}
                                    style={{
                                        padding: '8px 16px', border: '1px solid #cbd5e0', backgroundColor: '#f7fafc',
                                        color: '#4a5568', borderRadius: '6px', cursor: 'pointer', fontWeight: '500'
                                    }}
                                >
                                    Hủy
                                </button>
                                <button 
                                    type="submit" 
                                    disabled={isSubmitting}
                                    style={{
                                        padding: '8px 16px', border: 'none', backgroundColor: '#3182ce',
                                        color: 'white', borderRadius: '6px', cursor: isSubmitting ? 'not-allowed' : 'pointer',
                                        opacity: isSubmitting ? 0.7 : 1, fontWeight: '500'
                                    }}
                                >
                                    {isSubmitting ? 'Đang lưu...' : 'Lưu thông tin'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* MODAL ĐĂNG KÝ GIẢI ĐẤU */}
            {isRegModalOpen && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    zIndex: 9999
                }}>
                    <div style={{
                        backgroundColor: '#fff', padding: '24px', borderRadius: '8px',
                        width: '100%', maxWidth: '400px', boxShadow: '0 10px 15px rgba(0,0,0,0.1)'
                    }}>
                        <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#333', marginTop: 0, marginBottom: '20px' }}>
                            Đăng Ký Tham Gia Giải Đấu
                        </h3>
                        <form onSubmit={handleRegister}>
                            <div style={{ marginBottom: '16px' }}>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#4a5568', marginBottom: '8px' }}>
                                    Chọn Giải Đấu
                                </label>
                                <select 
                                    required
                                    style={{
                                        width: '100%', padding: '10px 12px', border: '1px solid #cbd5e0', 
                                        borderRadius: '6px', outline: 'none'
                                    }}
                                    value={regForm.tournamentId}
                                    onChange={(e) => setRegForm({...regForm, tournamentId: e.target.value})}
                                >
                                    <option value="">-- Chọn --</option>
                                    {tournaments.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                                </select>
                            </div>
                            <div style={{ marginBottom: '16px' }}>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#4a5568', marginBottom: '8px' }}>
                                    Chọn Ngựa
                                </label>
                                <select 
                                    required
                                    style={{
                                        width: '100%', padding: '10px 12px', border: '1px solid #cbd5e0', 
                                        borderRadius: '6px', outline: 'none'
                                    }}
                                    value={regForm.horseId}
                                    onChange={(e) => setRegForm({...regForm, horseId: e.target.value})}
                                >
                                    <option value="">-- Chọn --</option>
                                    {myHorses.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}
                                </select>
                            </div>
                            <div style={{ marginBottom: '24px' }}>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#4a5568', marginBottom: '8px' }}>
                                    Chọn Nài Ngựa (Jockey)
                                </label>
                                <select 
                                    required
                                    style={{
                                        width: '100%', padding: '10px 12px', border: '1px solid #cbd5e0', 
                                        borderRadius: '6px', outline: 'none'
                                    }}
                                    value={regForm.jockeyId}
                                    onChange={(e) => setRegForm({...regForm, jockeyId: e.target.value})}
                                >
                                    <option value="">-- Chọn --</option>
                                    {jockeys.map(j => <option key={j.id} value={j.id}>{j.name} ({j.experienceYears} năm KN)</option>)}
                                </select>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                                <button 
                                    type="button" 
                                    onClick={() => setIsRegModalOpen(false)}
                                    style={{
                                        padding: '8px 16px', border: '1px solid #cbd5e0', backgroundColor: '#f7fafc',
                                        color: '#4a5568', borderRadius: '6px', cursor: 'pointer', fontWeight: '500'
                                    }}
                                >
                                    Hủy
                                </button>
                                <button 
                                    type="submit" 
                                    disabled={isRegSubmitting}
                                    style={{
                                        padding: '8px 16px', border: 'none', backgroundColor: '#10b981',
                                        color: 'white', borderRadius: '6px', cursor: isRegSubmitting ? 'not-allowed' : 'pointer',
                                        opacity: isRegSubmitting ? 0.7 : 1, fontWeight: '500'
                                    }}
                                >
                                    {isRegSubmitting ? 'Đang gửi...' : 'Gửi lời mời'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default HorseOwnerDashboard;