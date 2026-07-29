import React, { useEffect, useMemo, useState } from 'react';
import axiosClient from '../../../services/axiosClient';
import { showConfirmModal, showErrorAlert, showToast } from '../../../utils/alertUtils';
import { FaCalendarAlt, FaFlagCheckered, FaPlus, FaRoute, FaTrophy, FaUsers } from 'react-icons/fa';
import { FiEdit2, FiTrash2, FiX } from 'react-icons/fi';
import './TournamentManagement.css';

const TOURNAMENT_STATUS = [
    { value: 'UPCOMING', label: 'Sắp diễn ra' },
    { value: 'ONGOING', label: 'Đang diễn ra' },
    { value: 'COMPLETED', label: 'Đã kết thúc' },
];

const RACE_STATUS = [
    { value: 'SCHEDULED', label: 'Đã lên lịch' },
    { value: 'IN_PROGRESS', label: 'Đang diễn ra' },
    { value: 'COMPLETED', label: 'Hoàn thành' },
    { value: 'CANCELLED', label: 'Đã hủy' },
];

const emptyTournamentForm = {
    id: '',
    name: '',
    description: '',
    startDate: '',
    endDate: '',
    status: 'UPCOMING',
};

const emptyRaceForm = {
    id: '',
    tournamentId: '',
    name: '',
    startTime: '',
    distance: '',
    status: 'SCHEDULED',
};

const formatDateInput = (value) => {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    return date.toISOString().slice(0, 10);
};

const formatDateTimeInput = (value) => {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 16);
};

const formatDate = (value) => {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '—';
    return date.toLocaleDateString('vi-VN');
};

const formatDateTime = (value) => {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '—';
    return date.toLocaleString('vi-VN');
};

const getStatusLabel = (list, value) => {
    return list.find((item) => item.value === value)?.label || value || '—';
};

const getRegistrationStatusLabel = (status) => {
    switch (status) {
        case 'PENDING':
            return 'Chờ duyệt';
        case 'APPROVED':
            return 'Đã duyệt';
        case 'REJECTED':
            return 'Từ chối';
        case 'CANCELLED':
            return 'Đã huỷ';
        default:
            return status || '—';
    }
};

const TournamentManagement = () => {
    const [activeTab, setActiveTab] = useState('tournaments');
    const [loading, setLoading] = useState(false);

    const [tournaments, setTournaments] = useState([]);
    const [races, setRaces] = useState([]);

    const [selectedTournamentId, setSelectedTournamentId] = useState('');

    const [isTournamentModalOpen, setTournamentModalOpen] = useState(false);
    const [isRaceModalOpen, setRaceModalOpen] = useState(false);

    const [editingTournament, setEditingTournament] = useState(null);
    const [editingRace, setEditingRace] = useState(null);

    const [isParticipantsModalOpen, setParticipantsModalOpen] = useState(false);
    const [participants, setParticipants] = useState([]);
    const [loadingParticipants, setLoadingParticipants] = useState(false);
    const [selectedTournamentName, setSelectedTournamentName] = useState('');

    const [tournamentForm, setTournamentForm] = useState(emptyTournamentForm);
    const [raceForm, setRaceForm] = useState(emptyRaceForm);

    const selectedTournament = useMemo(() => {
        return tournaments.find((item) => item.id === selectedTournamentId);
    }, [tournaments, selectedTournamentId]);

    const fetchTournaments = async () => {
        try {
            setLoading(true);
            const response = await axiosClient.get('/admin/tournaments');
            const list = response || [];
            setTournaments(list);

            if (!selectedTournamentId && list.length > 0) {
                setSelectedTournamentId(list[0].id);
            }
        } catch (error) {
            console.error(error);
            showErrorAlert('Lỗi', 'Không thể tải danh sách giải đấu.');
        } finally {
            setLoading(false);
        }
    };

    const fetchRacesByTournament = async (tournamentId) => {
        if (!tournamentId) {
            setRaces([]);
            return;
        }

        try {
            const response = await axiosClient.get(`/admin/races/tournament/${tournamentId}`);
            setRaces(response || []);
        } catch (error) {
            console.error(error);
            setRaces([]);
            showToast(error.response?.data?.message || 'Không thể tải lịch thi đấu.', 'error');
        }
    };

    useEffect(() => {
        fetchTournaments();
    }, []);

    const openParticipantsModal = async (tournament) => {
        setSelectedTournamentName(tournament.name);
        setParticipantsModalOpen(true);
        setLoadingParticipants(true);
        try {
            const response = await axiosClient.get(`/v1/registrations/tournament/${tournament.id}`);
            setParticipants(response || []);
        } catch (error) {
            console.error(error);
            showToast('Không thể tải danh sách thành viên.', 'error');
            setParticipants([]);
        } finally {
            setLoadingParticipants(false);
        }
    };

    useEffect(() => {
        if (activeTab === 'schedule') {
            fetchRacesByTournament(selectedTournamentId);
        }
    }, [activeTab, selectedTournamentId]);

    const openCreateTournament = () => {
        setEditingTournament(null);
        setTournamentForm(emptyTournamentForm);
        setTournamentModalOpen(true);
    };

    const openEditTournament = (tournament) => {
        setEditingTournament(tournament);
        setTournamentForm({
            id: tournament.id,
            name: tournament.name || '',
            description: tournament.description || '',
            startDate: formatDateInput(tournament.startDate),
            endDate: formatDateInput(tournament.endDate),
            status: tournament.status || 'UPCOMING',
        });
        setTournamentModalOpen(true);
    };

    const handleTournamentChange = (e) => {
        const { name, value } = e.target;
        setTournamentForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleTournamentSubmit = async (e) => {
        e.preventDefault();

        const payload = {
            name: tournamentForm.name.trim(),
            description: tournamentForm.description.trim(),
            startDate: tournamentForm.startDate ? `${tournamentForm.startDate}T00:00:00.000+00:00` : null,
            endDate: tournamentForm.endDate ? `${tournamentForm.endDate}T00:00:00.000+00:00` : null,
            status: tournamentForm.status,
        };

        try {
            if (editingTournament) {
                await axiosClient.put(`/admin/tournaments/${editingTournament.id}`, payload);
                showToast('Cập nhật giải đấu thành công!', 'success');
            } else {
                await axiosClient.post('/admin/tournaments', payload);
                showToast('Tạo giải đấu thành công!', 'success');
            }

            setTournamentModalOpen(false);
            fetchTournaments();
        } catch (error) {
            console.error(error);
            showToast(error.response?.data?.message || 'Lưu giải đấu thất bại.', 'error');
        }
    };

    const handleDeleteTournament = async (id) => {
        const confirmed = await showConfirmModal(
            'Xác nhận xóa',
            'Nếu giải đấu đã có lịch thi đấu, hệ thống sẽ không cho xóa.',
            'Xóa'
        );

        if (!confirmed) return;

        try {
            await axiosClient.delete(`/admin/tournaments/${id}`);
            showToast('Xóa giải đấu thành công!', 'success');

            if (selectedTournamentId === id) {
                setSelectedTournamentId('');
                setRaces([]);
            }

            fetchTournaments();
        } catch (error) {
            console.error(error);
            showErrorAlert('Không thể xóa', error.response?.data?.message || 'Đã xảy ra lỗi.');
        }
    };

    const openCreateRace = () => {
        if (!selectedTournamentId) {
            showToast('Vui lòng chọn giải đấu trước.', 'warning');
            return;
        }

        setEditingRace(null);
        setRaceForm({
            ...emptyRaceForm,
            tournamentId: selectedTournamentId,
        });
        setRaceModalOpen(true);
    };

    const openEditRace = (race) => {
        setEditingRace(race);
        setRaceForm({
            id: race.id,
            tournamentId: race.tournamentId || selectedTournamentId,
            name: race.name || '',
            startTime: formatDateTimeInput(race.startTime),
            distance: race.distance || '',
            status: 'SCHEDULED',
            refereeId: '',
            advancingCount: 5
        });
        setIsRaceModalOpen(true);
    };

    const handleRaceChange = (e) => {
        const { name, value } = e.target;
        setRaceForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleRaceSubmit = async (e) => {
        e.preventDefault();

        const payload = {
            tournamentId: raceForm.tournamentId,
            name: raceForm.name.trim(),
            startTime: raceForm.startTime || null,
            distance: raceForm.distance ? Number(raceForm.distance) : null,
            status: raceForm.status,
        };

        try {
            if (editingRace) {
                await axiosClient.put(`/admin/races/${editingRace.id}`, payload);
                showToast('Cập nhật lịch thi đấu thành công!', 'success');
            } else {
                await axiosClient.post('/admin/races', payload);
                showToast('Tạo lịch thi đấu thành công!', 'success');
            }

            setRaceModalOpen(false);
            setSelectedTournamentId(payload.tournamentId);
            fetchRacesByTournament(payload.tournamentId);
        } catch (error) {
            console.error(error);
            showToast(error.response?.data?.message || 'Lưu lịch thi đấu thất bại.', 'error');
        }
    };

    const handleDeleteRace = async (id) => {
        const confirmed = await showConfirmModal(
            'Xác nhận xóa',
            'Bạn có chắc muốn xóa cuộc đua/vòng đua này?',
            'Xóa'
        );

        if (!confirmed) return;

        try {
            await axiosClient.delete(`/admin/races/${id}`);
            showToast('Xóa cuộc đua thành công!', 'success');
            fetchRacesByTournament(selectedTournamentId);
        } catch (error) {
            console.error(error);
            showErrorAlert('Lỗi', error.response?.data?.message || 'Không thể xóa cuộc đua.');
        }
    };

    return (
        <div className="tm-page">
            <section className="tm-hero">
                <div>
                    <p className="tm-eyebrow">Tournament Administration</p>
                    <h2>Quản lý Giải đấu & Lịch thi đấu</h2>
                    <p>
                        Tạo giải đấu, xem danh sách giải và cấu hình các cuộc đua/vòng đua trực quan theo từng giải đấu.
                    </p>
                </div>
                <div className="tm-hero-icon">
                    <FaTrophy />
                </div>
            </section>

            <section className="tm-stats">
                <div className="tm-stat-card blue">
                    <span><FaTrophy /></span>
                    <div>
                        <p>Tổng giải đấu</p>
                        <strong>{tournaments.length}</strong>
                    </div>
                </div>

                <div className="tm-stat-card green">
                    <span><FaCalendarAlt /></span>
                    <div>
                        <p>Đang diễn ra</p>
                        <strong>{tournaments.filter((item) => item.status === 'ONGOING').length}</strong>
                    </div>
                </div>

                <div className="tm-stat-card orange">
                    <span><FaFlagCheckered /></span>
                    <div>
                        <p>Lịch đang xem</p>
                        <strong>{races.length}</strong>
                    </div>
                </div>
            </section>

            <div className="tm-tabs">
                <button
                    className={activeTab === 'tournaments' ? 'active' : ''}
                    onClick={() => setActiveTab('tournaments')}
                >
                    <FaTrophy /> Quản lý giải đấu
                </button>

                <button
                    className={activeTab === 'schedule' ? 'active' : ''}
                    onClick={() => setActiveTab('schedule')}
                >
                    <FaFlagCheckered /> Lịch thi đấu
                </button>
            </div>

            {activeTab === 'tournaments' && (
                <section className="tm-card">
                    <div className="tm-card-header">
                        <div>
                            <h3>Danh sách giải đấu</h3>
                            <p>Admin tạo, cập nhật và theo dõi các giải đấu đua ngựa.</p>
                        </div>

                        <button className="tm-primary-btn" onClick={openCreateTournament}>
                            <FaPlus /> Thêm giải đấu
                        </button>
                    </div>

                    <div className="tm-table-wrapper">
                        <table className="tm-table">
                            <thead>
                                <tr>
                                    <th>Tên giải đấu</th>
                                    <th>Thời gian</th>
                                    <th>Mô tả</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>

                            <tbody>
                                {loading ? (
                                    <tr>
                                        <td colSpan="5" className="tm-empty">Đang tải dữ liệu...</td>
                                    </tr>
                                ) : tournaments.length === 0 ? (
                                    <tr>
                                        <td colSpan="5" className="tm-empty">Chưa có giải đấu nào.</td>
                                    </tr>
                                ) : (
                                    tournaments.map((tournament) => (
                                        <tr key={tournament.id}>
                                            <td>
                                                <div className="tm-name-cell">
                                                    <div className="tm-avatar"><FaTrophy /></div>
                                                    <div>
                                                        <strong>{tournament.name}</strong>
                                                        <small>Giải đấu đua ngựa</small>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                {formatDate(tournament.startDate)}
                                                <span className="tm-arrow">→</span>
                                                {formatDate(tournament.endDate)}
                                            </td>
                                            <td className="tm-desc">{tournament.description || '—'}</td>
                                            <td>
                                                <span className={`tm-badge ${String(tournament.status).toLowerCase()}`}>
                                                    {getStatusLabel(TOURNAMENT_STATUS, tournament.status)}
                                                </span>
                                            </td>
                                            <td>
                                                <div className="tm-actions">
                                                    <button
                                                        className="tm-icon-btn green"
                                                        title="Xem danh sách tham gia"
                                                        onClick={() => openParticipantsModal(tournament)}
                                                    >
                                                        <FaUsers />
                                                    </button>
                                                    <button
                                                        className="tm-icon-btn blue"
                                                        title="Lập lịch"
                                                        onClick={() => {
                                                            setSelectedTournamentId(tournament.id);
                                                            setActiveTab('schedule');
                                                        }}
                                                    >
                                                        <FaCalendarAlt />
                                                    </button>
                                                    <button
                                                        className="tm-icon-btn yellow"
                                                        title="Sửa"
                                                        onClick={() => openEditTournament(tournament)}
                                                    >
                                                        <FiEdit2 />
                                                    </button>
                                                    <button
                                                        className="tm-icon-btn red"
                                                        title="Xóa"
                                                        onClick={() => handleDeleteTournament(tournament.id)}
                                                    >
                                                        <FiTrash2 />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </section>
            )}

            {activeTab === 'schedule' && (
                <section className="tm-card">
                    <div className="tm-card-header">
                        <div>
                            <h3>Cấu hình lịch thi đấu</h3>
                            <p>Chọn giải đấu và sắp xếp các cuộc đua/vòng đua theo thời gian.</p>
                        </div>

                        <button className="tm-primary-btn" onClick={openCreateRace}>
                            <FaPlus /> Thêm cuộc đua
                        </button>
                    </div>

                    <div className="tm-filter">
                        <label>Chọn giải đấu</label>
                        <select
                            value={selectedTournamentId}
                            onChange={(e) => setSelectedTournamentId(e.target.value)}
                        >
                            <option value="">-- Chọn giải đấu --</option>
                            {tournaments.map((tournament) => (
                                <option key={tournament.id} value={tournament.id}>
                                    {tournament.name}
                                </option>
                            ))}
                        </select>

                        {selectedTournament && (
                            <div className="tm-selected">
                                <strong>{selectedTournament.name}</strong>
                                <span>
                                    {formatDate(selectedTournament.startDate)} - {formatDate(selectedTournament.endDate)}
                                </span>
                            </div>
                        )}
                    </div>

                    <div className="tm-schedule-list">
                        {!selectedTournamentId ? (
                            <div className="tm-empty-panel">Vui lòng chọn giải đấu để xem lịch thi đấu.</div>
                        ) : races.length === 0 ? (
                            <div className="tm-empty-panel">Giải đấu này chưa có cuộc đua/vòng đua nào.</div>
                        ) : (
                            races.map((race, index) => (
                                <div className="tm-race-card" key={race.id}>
                                    <div className="tm-race-index">{index + 1}</div>

                                    <div className="tm-race-content">
                                        <div className="tm-race-title">
                                            <h4>{race.name}</h4>
                                            <span className={`tm-badge ${String(race.status).toLowerCase()}`}>
                                                {getStatusLabel(RACE_STATUS, race.status)}
                                            </span>
                                        </div>

                                        <div className="tm-race-meta">
                                            <span><FaCalendarAlt /> {formatDateTime(race.startTime)}</span>
                                            <span><FaRoute /> {race.distance} m</span>
                                            <span><FaTrophy /> {race.tournamentName || selectedTournament?.name || '—'}</span>
                                        </div>
                                    </div>

                                    <div className="tm-actions">
                                        <button className="tm-icon-btn yellow" onClick={() => openEditRace(race)}>
                                            <FiEdit2 />
                                        </button>
                                        <button className="tm-icon-btn red" onClick={() => handleDeleteRace(race.id)}>
                                            <FiTrash2 />
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </section>
            )}

            {isTournamentModalOpen && (
                <div className="tm-modal-backdrop" onClick={() => setTournamentModalOpen(false)}>
                    <div className="tm-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="tm-modal-header">
                            <h3>{editingTournament ? 'Cập nhật giải đấu' : 'Thêm giải đấu mới'}</h3>
                            <button onClick={() => setTournamentModalOpen(false)}><FiX /></button>
                        </div>

                        <form className="tm-form" onSubmit={handleTournamentSubmit}>
                            <label>
                                Tên giải đấu
                                <input
                                    name="name"
                                    value={tournamentForm.name}
                                    onChange={handleTournamentChange}
                                    placeholder="VD: Giải đua mùa hè 2026"
                                    required
                                />
                            </label>

                            <label>
                                Mô tả
                                <textarea
                                    name="description"
                                    value={tournamentForm.description}
                                    onChange={handleTournamentChange}
                                    placeholder="Mô tả ngắn về giải đấu"
                                    rows="3"
                                />
                            </label>

                            <div className="tm-form-grid">
                                <label>
                                    Ngày bắt đầu
                                    <input
                                        type="date"
                                        name="startDate"
                                        value={tournamentForm.startDate}
                                        onChange={handleTournamentChange}
                                        required
                                    />
                                </label>

                                <label>
                                    Ngày kết thúc
                                    <input
                                        type="date"
                                        name="endDate"
                                        value={tournamentForm.endDate}
                                        onChange={handleTournamentChange}
                                        required
                                    />
                                </label>
                            </div>

                            <label>
                                Trạng thái
                                <select name="status" value={tournamentForm.status} onChange={handleTournamentChange}>
                                    {TOURNAMENT_STATUS.map((item) => (
                                        <option key={item.value} value={item.value}>{item.label}</option>
                                    ))}
                                </select>
                            </label>

                            <div className="tm-modal-actions">
                                <button type="button" className="tm-secondary-btn" onClick={() => setTournamentModalOpen(false)}>
                                    Hủy
                                </button>
                                <button type="submit" className="tm-primary-btn">
                                    {editingTournament ? 'Lưu thay đổi' : 'Tạo giải đấu'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {isRaceModalOpen && (
                <div className="tm-modal-backdrop" onClick={() => setRaceModalOpen(false)}>
                    <div className="tm-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="tm-modal-header">
                            <h3>{editingRace ? 'Cập nhật lịch thi đấu' : 'Thêm cuộc đua/vòng đua'}</h3>
                            <button onClick={() => setRaceModalOpen(false)}><FiX /></button>
                        </div>

                        <form className="tm-form" onSubmit={handleRaceSubmit}>
                            <label>
                                Giải đấu
                                <select
                                    name="tournamentId"
                                    value={raceForm.tournamentId}
                                    onChange={handleRaceChange}
                                    required
                                >
                                    <option value="">-- Chọn giải đấu --</option>
                                    {tournaments.map((tournament) => (
                                        <option key={tournament.id} value={tournament.id}>
                                            {tournament.name}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label>
                                Tên cuộc đua/vòng đua
                                <input
                                    name="name"
                                    value={raceForm.name}
                                    onChange={handleRaceChange}
                                    placeholder="VD: Vòng loại 1, Bán kết, Chung kết"
                                    required
                                />
                            </label>

                            <div className="tm-form-grid">
                                <label>
                                    Thời gian bắt đầu
                                    <input
                                        type="datetime-local"
                                        name="startTime"
                                        value={raceForm.startTime}
                                        onChange={handleRaceChange}
                                        required
                                    />
                                </label>

                                <label>
                                    Quãng đường mét
                                    <input
                                        type="number"
                                        name="distance"
                                        value={raceForm.distance}
                                        onChange={handleRaceChange}
                                        min="1"
                                        placeholder="VD: 1200"
                                        required
                                    />
                                </label>
                            </div>

                            <label>
                                Trạng thái
                                <select name="status" value={raceForm.status} onChange={handleRaceChange}>
                                    {RACE_STATUS.map((item) => (
                                        <option key={item.value} value={item.value}>{item.label}</option>
                                    ))}
                                </select>
                            </label>

                            <label>
                                Số lượng ngựa được đi tiếp (Top N)
                                <input
                                    type="number"
                                    name="advancingCount"
                                    value={raceForm.advancingCount || 5}
                                    onChange={handleRaceChange}
                                    min="1"
                                />
                                <small style={{color: '#64748b', fontSize: '0.8rem', display: 'block', marginTop: '5px'}}>Nếu chọn ≤ 3, hệ thống sẽ tự động coi đây là Vòng Chung Kết.</small>
                            </label>

                            <div className="tm-modal-actions">
                                <button type="button" className="tm-secondary-btn" onClick={() => setRaceModalOpen(false)}>
                                    Hủy
                                </button>
                                <button type="submit" className="tm-primary-btn">
                                    {editingRace ? 'Lưu thay đổi' : 'Tạo cuộc đua'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {isParticipantsModalOpen && (
                <div className="tm-modal-backdrop" onClick={() => setParticipantsModalOpen(false)}>
                    <div className="tm-modal tm-modal-large" onClick={(e) => e.stopPropagation()}>
                        <div className="tm-modal-header">
                            <h3>Danh sách tham gia - {selectedTournamentName}</h3>
                            <button onClick={() => setParticipantsModalOpen(false)}><FiX /></button>
                        </div>
                        <div className="tm-table-wrapper" style={{ maxHeight: '400px', overflowY: 'auto' }}>
                            <table className="tm-table">
                                <thead>
                                    <tr>
                                        <th>STT</th>
                                        <th>Tuyển thủ (Ngựa)</th>
                                        <th>Nài ngựa</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {loadingParticipants ? (
                                        <tr><td colSpan="4" className="tm-empty">Đang tải dữ liệu...</td></tr>
                                    ) : participants.length === 0 ? (
                                        <tr><td colSpan="4" className="tm-empty">Chưa có ai đăng ký tham gia giải đấu này.</td></tr>
                                    ) : (
                                        participants.map((p, idx) => (
                                            <tr key={p.id || idx}>
                                                <td>{idx + 1}</td>
                                                <td>{p.horse?.name || '—'}</td>
                                                <td>{p.jockey?.name || 'Chưa chỉ định'}</td>
                                                <td>
                                                    <span className={`tm-badge ${String(p.status).toLowerCase()}`}>
                                                        {getRegistrationStatusLabel(p.status)}
                                                    </span>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default TournamentManagement;