import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux'; // để lấy user info
import axiosClient from "../services/axiosClient"; // dùng axiosClient đã cấu hình

const RegisterTournamentForm = () => {
    // Lấy thông tin user từ Redux (đã login)
    const user = useSelector(state => state.auth.user);
    const ownerId = user?.id; // giả sử user.id chính là ownerId (hoặc user.ownerId tùy cấu trúc)

    const [tournaments, setTournaments] = useState([]);
    const [horses, setHorses] = useState([]);
    const [jockeys, setJockeys] = useState([]);
    const [formData, setFormData] = useState({
        tournamentId: '',
        horseId: '',
        jockeyId: ''
    });
    const [message, setMessage] = useState('');
    const [isError, setIsError] = useState(false);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        // Gọi API lấy danh sách giải đấu
        axiosClient.get('/v1/tournaments')
            .then(res => setTournaments(res.data))
            .catch(err => console.error("Lỗi lấy giải đấu:", err));

        // Gọi API lấy danh sách ngựa theo chủ (ownerId)
        if (ownerId) {
            axiosClient.get(`/v1/horses/owner/${ownerId}`)
                .then(res => setHorses(res.data))
                .catch(err => console.error("Lỗi lấy ngựa:", err));
        }

        // Gọi API lấy danh sách Jockey (tất cả)
        axiosClient.get('/v1/jockeys')
            .then(res => setJockeys(res.data))
            .catch(err => console.error("Lỗi lấy Jockey:", err));
    }, [ownerId]); // chạy lại khi ownerId thay đổi

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.tournamentId || !formData.horseId || !formData.jockeyId) {
            setIsError(true);
            setMessage("Vui lòng chọn đầy đủ giải đấu, ngựa và Jockey!");
            return;
        }

        setLoading(true);
        setMessage('');

        try {
            // Gọi API đăng ký (POST /api/v1/registrations/register)
            const response = await axiosClient.post('/v1/registrations/register', formData);
            setIsError(false);
            setMessage(`🎉 Đăng ký thành công! Đã gửi lời mời đến Jockey.`);
            setFormData({ tournamentId: '', horseId: '', jockeyId: '' });
        } catch (error) {
            setIsError(true);
            setMessage(error.response?.data || "Đã có lỗi xảy ra!");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: '450px', margin: '40px auto', padding: '20px', border: '1px solid #ccc', borderRadius: '8px' }}>
            <h2 style={{ textAlign: 'center' }}>Đăng Ký Giải Đấu & Mời Jockey</h2>
            <form onSubmit={handleSubmit}>
                {/* Chọn giải đấu */}
                <div style={{ marginBottom: '15px' }}>
                    <label>Chọn Giải Đấu:</label>
                    <select name="tournamentId" value={formData.tournamentId} onChange={handleChange} style={{ width: '100%', padding: '10px' }}>
                        <option value="">-- Chọn --</option>
                        {tournaments.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                    </select>
                </div>

                {/* Chọn ngựa (chỉ ngựa của chủ) */}
                <div style={{ marginBottom: '15px' }}>
                    <label>Chọn Ngựa:</label>
                    <select name="horseId" value={formData.horseId} onChange={handleChange} style={{ width: '100%', padding: '10px' }}>
                        <option value="">-- Chọn --</option>
                        {horses.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}
                    </select>
                </div>

                {/* Chọn Jockey */}
                <div style={{ marginBottom: '20px' }}>
                    <label>Chọn Jockey:</label>
                    <select name="jockeyId" value={formData.jockeyId} onChange={handleChange} style={{ width: '100%', padding: '10px' }}>
                        <option value="">-- Chọn --</option>
                        {jockeys.map(j => <option key={j.id} value={j.id}>{j.name} (KN: {j.experienceYears} năm)</option>)}
                    </select>
                </div>

                <button type="submit" disabled={loading} style={{ width: '100%', padding: '12px', backgroundColor: '#007bff', color: '#fff', border: 'none', borderRadius: '4px' }}>
                    {loading ? 'Đang xử lý...' : 'Xác Nhận Đăng Ký & Gửi Lời Mời'}
                </button>
            </form>
            {message && <div style={{ marginTop: '15px', padding: '12px', borderRadius: '4px', backgroundColor: isError ? '#f8d7da' : '#d4edda', color: isError ? '#721c24' : '#155724' }}>{message}</div>}
        </div>
    );
};

export default RegisterTournamentForm;