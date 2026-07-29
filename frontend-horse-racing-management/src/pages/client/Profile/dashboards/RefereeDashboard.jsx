import React, { useEffect, useState } from 'react';
import { FiEdit3, FiAlertCircle, FiCheckCircle, FiLoader } from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import axiosClient from '../../../../services/axiosClient';

import { createPortal } from 'react-dom';

const RefereeDashboard = () => {
    const { user } = useSelector((state) => state.auth || {});
    const navigate = useNavigate();
    const refereeId = user?.id || user?.userId || localStorage.getItem('userId') || 'referee1';

    const [assignedRaces, setAssignedRaces] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');
    const [activeModal, setActiveModal] = useState(null);
    const [selectedRace, setSelectedRace] = useState(null);
    const [raceParticipants, setRaceParticipants] = useState([]);
    const [raceDetails, setRaceDetails] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const [reportForm, setReportForm] = useState({ reportText: '' });
    const [reportHistory, setReportHistory] = useState([]);
    const [reportsLoading, setReportsLoading] = useState(false);
    const [violationHistory, setViolationHistory] = useState([]);
    const [violationsLoading, setViolationsLoading] = useState(false);
    const [raceResults, setRaceResults] = useState([]);
    const [resultsLoading, setResultsLoading] = useState(false);
    const [violationForm, setViolationForm] = useState({
        horseId: '',
        jockeyId: '',
        violationType: 'FOUL',
        description: '',
        penalty: '100',
        severity: 'MEDIUM',
    });
    const [resultForm, setResultForm] = useState({
        horseId: '',
        jockeyId: '',
        position: '',
        finishTime: '',
    });

    const fetchAssignedRaces = async () => {
        try {
            setLoading(true);
            setError('');
            const data = await axiosClient.get(`/referee/${refereeId}/assigned-races`);
            setAssignedRaces(Array.isArray(data) ? data : []);
        } catch (err) {
            setAssignedRaces([]);
            setError(err.response?.data?.error || 'Không thể tải dữ liệu từ máy chủ');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!refereeId) {
            setLoading(false);
            return;
        }
        fetchAssignedRaces();
    }, [refereeId]);

    const openModal = async (race, type) => {
        setActiveModal(type);
        setSelectedRace(race);
        setMessage('');
        setError('');
        const raceId = race?.raceId || race?.id;

        if (!raceId) return;

        try {
            const [detailsRes, horsesRes] = await Promise.all([
                axiosClient.get(`/referee/race/${raceId}/details`),
                axiosClient.get(`/referee/race/${raceId}/horses`),
            ]);
            setRaceDetails(detailsRes);
            setRaceParticipants(Array.isArray(horsesRes) ? horsesRes : []);

            if (type === 'report') {
                setReportsLoading(true);
                const historyRes = await axiosClient.get(`/referee/race/${raceId}/reports`);
                setReportHistory(Array.isArray(historyRes) ? historyRes : []);
            } else {
                setReportHistory([]);
            }

            if (type === 'violation') {
                setViolationsLoading(true);
                const violationsRes = await axiosClient.get(`/referee/race/${raceId}/violations`);
                setViolationHistory(Array.isArray(violationsRes) ? violationsRes : []);
            } else {
                setViolationHistory([]);
            }

            if (type === 'result') {
                setResultsLoading(true);
                const resultsRes = await axiosClient.get(`/referee/race/${raceId}/results`);
                setRaceResults(Array.isArray(resultsRes) ? resultsRes : []);
            } else {
                setRaceResults([]);
            }
        } catch (err) {
            setRaceDetails(null);
            setRaceParticipants([]);
            setReportHistory([]);
            setViolationHistory([]);
            setRaceResults([]);
            setMessage('Không thể tải dữ liệu cho cuộc đua này.');
        } finally {
            setReportsLoading(false);
            setViolationsLoading(false);
            setResultsLoading(false);
        }
    };

    const closeModal = () => {
        setActiveModal(null);
        setSelectedRace(null);
        setRaceParticipants([]);
        setRaceDetails(null);
        setReportHistory([]);
        setViolationHistory([]);
        setRaceResults([]);
        setMessage('');
        setReportForm({ reportText: '' });
        setViolationForm({
            horseId: '',
            jockeyId: '',
            violationType: 'FOUL',
            description: '',
            penalty: '100',
            severity: 'MEDIUM',
        });
        setResultForm({ horseId: '', jockeyId: '', position: '', finishTime: '' });
    };

    const handleReportSubmit = async (e) => {
        e.preventDefault();
        if (!selectedRace) return;

        try {
            setSubmitting(true);
            const raceId = selectedRace.raceId || selectedRace.id;
            await axiosClient.post(`/referee/${refereeId}/report?raceId=${raceId}`, {
                reportText: reportForm.reportText,
            });
            setMessage('Đã lưu biên bản thành công');
            await fetchAssignedRaces();
            closeModal();
        } catch (err) {
            setMessage(err.response?.data?.error || 'Không thể lưu biên bản');
        } finally {
            setSubmitting(false);
        }
    };

    const handleViolationSubmit = async (e) => {
        e.preventDefault();
        if (!selectedRace) return;

        try {
            setSubmitting(true);
            const raceId = selectedRace.raceId || selectedRace.id;
            await axiosClient.post(`/referee/race/${raceId}/violation`, {
                horseId: violationForm.horseId,
                jockeyId: violationForm.jockeyId,
                violationType: violationForm.violationType,
                description: violationForm.description,
                penalty: Number(violationForm.penalty),
                severity: violationForm.severity,
                refereeId,
            });
            setMessage('Đã ghi nhận vi phạm thành công');
            closeModal();
        } catch (err) {
            setMessage(err.response?.data?.error || 'Không thể ghi nhận vi phạm');
        } finally {
            setSubmitting(false);
        }
    };

    const handleResultSubmit = async (e) => {
        e.preventDefault();
        if (!selectedRace) return;

        try {
            setSubmitting(true);
            const raceId = selectedRace.raceId || selectedRace.id;
            await axiosClient.post(`/referee/race/${raceId}/result`, {
                horseId: resultForm.horseId,
                jockeyId: resultForm.jockeyId,
                position: Number(resultForm.position),
                finishTime: Number(resultForm.finishTime),
            });
            setMessage('Đã lưu kết quả cuộc đua thành công');
            closeModal();
        } catch (err) {
            setMessage(err.response?.data?.error || 'Không thể lưu kết quả cuộc đua');
        } finally {
            setSubmitting(false);
        }
    };

    const handleHorseSelect = (value, formType) => {
        const selectedParticipant = raceParticipants.find((item) => item.horseId === value);
        if (formType === 'violation') {
            setViolationForm((prev) => ({
                ...prev,
                horseId: value,
                jockeyId: selectedParticipant?.jockeyId || '',
            }));
        }
        if (formType === 'result') {
            setResultForm((prev) => ({
                ...prev,
                horseId: value,
                jockeyId: selectedParticipant?.jockeyId || '',
            }));
        }
    };

    const navigateToHorseDetail = (horseId) => {
        if (!horseId) return;
        const raceId = selectedRace?.raceId || selectedRace?.id;
        const query = raceId ? `?raceId=${raceId}` : '';
        navigate(`/horses/${horseId}${query}`);
    };

    const getStatusBadge = (status) => {
        if (!status) return { label: 'Đang chờ', type: 'pending' };
        const normalized = status.toLowerCase();
        if (normalized.includes('sắp') || normalized.includes('upcoming') || normalized.includes('pending')) {
            return { label: 'Sắp diễn ra', type: 'upcoming' };
        }
        if (normalized.includes('hoàn thành') || normalized.includes('completed') || normalized.includes('done')) {
            return { label: 'Đã hoàn thành', type: 'complete' };
        }
        if (normalized.includes('đang') || normalized.includes('in progress')) {
            return { label: 'Đang diễn ra', type: 'in-progress' };
        }
        return { label: status, type: 'other' };
    };

    return (
        <div className="dashboard-wrapper referee-dashboard">
            <div className="referee-dashboard-card">
                <div className="referee-dashboard-header">
                    <div>
                        <h2 className="dashboard-title">Bảng điều khiển Trọng Tài</h2>
                        <p className="dashboard-subtitle">Lịch phân công giám sát</p>
                    </div>
                </div>

                {message && (
                    <div className="profile-status-badge success referee-message">
                        <FiCheckCircle /> {message}
                    </div>
                )}

                <div className="table-responsive">
                    {loading ? (
                        <div className="dashboard-loading">
                            <FiLoader className="spinner" /> Đang tải dữ liệu...
                        </div>
                    ) : error ? (
                        <div className="profile-status-badge danger" style={{ marginTop: '8px' }}>{error}</div>
                    ) : (
                        <table className="tm-table referee-table">
                            <thead>
                                <tr>
                                    <th>Mã cuộc đua</th>
                                    <th>Tên cuộc đua</th>
                                    <th>Trạng thái</th>
                                    <th>Nhiệm vụ</th>
                                </tr>
                            </thead>
                            <tbody>
                                {assignedRaces.length === 0 ? (
                                    <tr>
                                        <td colSpan="4" className="empty-row">Không có cuộc đua nào được phân công.</td>
                                    </tr>
                                ) : (
                                    assignedRaces.map((race) => {
                                        const status = getStatusBadge(race.status || race.raceStatus || raceDetails?.status);
                                        return (
                                            <tr key={race.raceId || race.id}>
                                                <td><strong>{race.raceId || race.id}</strong></td>
                                                <td>{race.raceName || race.name || '---'}</td>
                                                <td>
                                                    <span className={`status-pill ${status.type}`}>{status.label}</span>
                                                </td>
                                                <td>
                                                    <div className="table-actions">
                                                        <button className="btn-primary btn-sm" onClick={() => openModal(race, 'report')}>
                                                            <FiEdit3 /> Lập biên bản
                                                        </button>
                                                        <button className="btn-secondary btn-sm" onClick={() => openModal(race, 'violation')}>
                                                            <FiAlertCircle /> Ghi nhận vi phạm
                                                        </button>
                                                        <button className="btn-outline btn-sm" onClick={() => openModal(race, 'result')}>
                                                            Lập kết quả
                                                        </button>
                                                        {race.status !== 'COMPLETED' && (
                                                            <button 
                                                                className="btn-sm" 
                                                                style={{background: '#10b981', color: 'white', border: 'none', marginLeft: '5px', cursor: 'pointer'}}
                                                                onClick={async () => {
                                                                    if (!window.confirm(`XÁC NHẬN CHỐT: Bạn đã xử lý xong mọi vi phạm và chắc chắn muốn CHỐT KẾT QUẢ để chia thưởng cho cuộc đua này? Hành động này không thể hoàn tác!`)) return;
                                                                    try {
                                                                        setLoading(true);
                                                                        const raceId = race.raceId || race.id;
                                                                        await axiosClient.post(`/v1/rewards/calculate/${raceId}`);
                                                                        try {
                                                                            await axiosClient.post(`/v1/tournaments/advance/${raceId}`);
                                                                        } catch (e) {
                                                                            // Ignore advance error if tournament ends or something
                                                                        }
                                                                        setMessage('Đã chốt kết quả, cộng tiền thưởng thành công và đóng cuộc đua!');
                                                                        fetchAssignedRaces();
                                                                    } catch (err) {
                                                                        setError('Lỗi khi chia thưởng: ' + (err.response?.data?.error || err.message));
                                                                        setLoading(false);
                                                                    }
                                                                }}
                                                            >
                                                                <FiCheckCircle style={{marginRight: '5px'}}/> Chốt & Chia Thưởng
                                                            </button>
                                                        )}
                                                    </div>
                                                </td>
                                            </tr>
                                        );
                                    })
                                )}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>

            {activeModal && selectedRace && createPortal(
                <div className="modal-overlay">
                    <div className="modal-card">
                        <div className="modal-header">
                            <div>
                                <h3>
                                    {activeModal === 'report'
                                        ? 'Lập biên bản'
                                        : activeModal === 'violation'
                                            ? 'Ghi nhận vi phạm'
                                            : 'Lập kết quả cuộc đua'}
                                </h3>
                                <p className="modal-subtitle">Cuộc đua: <strong>{selectedRace.raceName || selectedRace.name}</strong></p>
                            </div>
                            <button type="button" className="btn-ghost" onClick={closeModal} style={{background: 'transparent', border: 'none', fontSize: '1.2rem', cursor: 'pointer', padding: '5px'}}>✖</button>
                        </div>

                        {activeModal === 'report' && (
                            <>
                                <form onSubmit={handleReportSubmit} className="modal-form">
                                    <label htmlFor="reportText">Nội dung biên bản</label>
                                    <textarea
                                        id="reportText"
                                        rows="6"
                                        value={reportForm.reportText}
                                        onChange={(e) => setReportForm({ reportText: e.target.value })}
                                        placeholder="Nhập nội dung biên bản cho cuộc đua..."
                                        required
                                    />
                                    <div className="form-actions" style={{marginTop: '15px'}}>
                                        <button type="submit" className="btn-primary btn-sm" disabled={submitting}>
                                            {submitting ? 'Đang gửi...' : 'Lưu biên bản'}
                                        </button>
                                    </div>
                                </form>

                                <div className="history-section" style={{marginTop: '30px'}}>
                                    <h4>Lịch sử biên bản</h4>
                                    {reportsLoading ? (
                                        <div className="dashboard-loading">Đang tải lịch sử biên bản...</div>
                                    ) : reportHistory.length === 0 ? (
                                        <p>Chưa có biên bản nào cho cuộc đua này.</p>
                                    ) : (
                                        <div className="history-list">
                                            {reportHistory.map((report) => (
                                                <div key={report.id} className="history-item">
                                                    <div className="history-item-header">
                                                        <strong>{report.refereeName || 'Trọng tài'}</strong>
                                                        <span>{new Date(report.createdAt).toLocaleString('vi-VN')}</span>
                                                    </div>
                                                    <p>{report.reportText}</p>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </>
                        )}

                        {activeModal === 'violation' && (
                            <>
                                <form onSubmit={handleViolationSubmit} className="modal-form">
                                    <div className="form-grid">
                                        <div className="form-group">
                                            <label>Chọn ngựa</label>
                                            <div className="select-with-action">
                                                <select value={violationForm.horseId} onChange={(e) => handleHorseSelect(e.target.value, 'violation')} required>
                                                    <option value="">-- Chọn ngựa --</option>
                                                    {raceParticipants.map((item) => (
                                                        <option key={item.horseId} value={item.horseId}>
                                                            {item.horseName || item.horseId}
                                                        </option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                        <div className="form-group">
                                            <label>Mã jockey</label>
                                            <input
                                                type="text"
                                                value={violationForm.jockeyId}
                                                onChange={(e) => setViolationForm((prev) => ({ ...prev, jockeyId: e.target.value }))}
                                                placeholder="Mã jockey"
                                                required
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Loại vi phạm</label>
                                            <select value={violationForm.violationType} onChange={(e) => setViolationForm((prev) => ({ ...prev, violationType: e.target.value }))}>
                                                <option value="FOUL">Vi phạm đạo đức</option>
                                                <option value="FALSE_START">Khởi đầu sai</option>
                                                <option value="EQUIPMENT_ISSUE">Lỗi trang thiết bị</option>
                                                <option value="OTHER">Khác</option>
                                            </select>
                                        </div>
                                        <div className="form-group full-width">
                                            <label>Mô tả vi phạm</label>
                                            <textarea
                                                rows="4"
                                                value={violationForm.description}
                                                onChange={(e) => setViolationForm((prev) => ({ ...prev, description: e.target.value }))}
                                                placeholder="Mô tả vi phạm"
                                                required
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Mức phạt (VNĐ)</label>
                                            <input
                                                type="number"
                                                value={violationForm.penalty}
                                                onChange={(e) => setViolationForm((prev) => ({ ...prev, penalty: e.target.value }))}
                                                placeholder="Mức phạt"
                                                min="0"
                                                required
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Mức độ</label>
                                            <select value={violationForm.severity} onChange={(e) => setViolationForm((prev) => ({ ...prev, severity: e.target.value }))}>
                                                <option value="LOW">Thấp</option>
                                                <option value="MEDIUM">Trung bình</option>
                                                <option value="HIGH">Cao</option>
                                                <option value="CRITICAL">Rất nghiêm trọng</option>
                                            </select>
                                        </div>
                                    </div>
                                    <div className="form-actions" style={{marginTop: '15px'}}>
                                        <button type="submit" className="btn-primary btn-sm" disabled={submitting}>
                                            {submitting ? 'Đang gửi...' : 'Ghi nhận'}
                                        </button>
                                    </div>
                                </form>

                                <div className="history-section" style={{marginTop: '30px'}}>
                                    <h4>Danh sách vi phạm</h4>
                                    {violationsLoading ? (
                                        <div className="dashboard-loading">Đang tải danh sách vi phạm...</div>
                                    ) : violationHistory.length === 0 ? (
                                        <p>Chưa có vi phạm nào cho cuộc đua này.</p>
                                    ) : (
                                        <div className="history-list">
                                            {violationHistory.map((violation) => (
                                                <div key={violation.id} className="history-item">
                                                    <div className="history-item-header">
                                                        <strong>{violation.horseName || violation.horseId}</strong>
                                                        <span>{violation.jockeyName || violation.jockeyId}</span>
                                                    </div>
                                                    <p><strong>Loại:</strong> {violation.violationType}</p>
                                                    <p><strong>Mô tả:</strong> {violation.description}</p>
                                                    <p><strong>Phạt:</strong> {violation.penalty?.toLocaleString()} | <strong>Mức:</strong> {violation.severity}</p>
                                                    <p><small>{new Date(violation.recordedAt).toLocaleString('vi-VN')}</small></p>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </>
                        )}

                        {activeModal === 'result' && (
                            <>
                                <form onSubmit={handleResultSubmit} className="modal-form">
                                    <div className="form-grid">
                                        <div className="form-group">
                                            <label>Chọn ngựa</label>
                                            <div className="select-with-action">
                                                <select value={resultForm.horseId} onChange={(e) => handleHorseSelect(e.target.value, 'result')} required>
                                                    <option value="">-- Chọn ngựa --</option>
                                                    {raceParticipants.map((item) => (
                                                        <option key={item.horseId} value={item.horseId}>
                                                            {item.horseName || item.horseId}
                                                        </option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                        <div className="form-group">
                                            <label>Mã jockey</label>
                                            <input
                                                type="text"
                                                value={resultForm.jockeyId}
                                                onChange={(e) => setResultForm((prev) => ({ ...prev, jockeyId: e.target.value }))}
                                                placeholder="Mã jockey"
                                                required
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Vị trí</label>
                                            <input
                                                type="number"
                                                value={resultForm.position}
                                                onChange={(e) => setResultForm((prev) => ({ ...prev, position: e.target.value }))}
                                                placeholder="Vị trí"
                                                min="1"
                                                required
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Thời gian (giây)</label>
                                            <input
                                                type="number"
                                                step="0.01"
                                                value={resultForm.finishTime}
                                                onChange={(e) => setResultForm((prev) => ({ ...prev, finishTime: e.target.value }))}
                                                placeholder="Thời gian kết thúc"
                                                required
                                            />
                                        </div>
                                    </div>
                                    <div className="form-actions" style={{marginTop: '15px'}}>
                                        <button type="submit" className="btn-primary btn-sm" disabled={submitting}>
                                            {submitting ? 'Đang gửi...' : 'Lưu kết quả'}
                                        </button>
                                    </div>
                                </form>

                                <div className="history-section" style={{marginTop: '30px'}}>
                                    <h4>Danh sách kết quả</h4>
                                    {resultsLoading ? (
                                        <div className="dashboard-loading">Đang tải kết quả...</div>
                                    ) : raceResults.length === 0 ? (
                                        <p>Chưa có kết quả nào cho cuộc đua này.</p>
                                    ) : (
                                        <div className="history-list">
                                            {raceResults.map((result) => (
                                                <div key={result.id} className="history-item" style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                                                    <div>
                                                        <div className="history-item-header">
                                                            <strong>{result.horseName || result.horseId}</strong>
                                                            <span>{result.jockeyName || result.jockeyId}</span>
                                                        </div>
                                                        <p><strong>Vị trí:</strong> {result.position} 
                                                           {result.position === 99 && <span style={{color: 'red', marginLeft: '10px'}}>(Bị Loại / Xử Thua)</span>}
                                                        </p>
                                                        <p><strong>Thời gian:</strong> {result.finishTime}s</p>
                                                        {result.prizeMoney != null && <p><strong>Thưởng:</strong> {result.prizeMoney.toLocaleString()}</p>}
                                                    </div>
                                                    {result.position !== 99 && (
                                                        <button 
                                                            type="button"
                                                            className="btn-outline btn-sm" 
                                                            style={{color: '#ef4444', borderColor: '#ef4444', padding: '4px 8px', fontSize: '0.8rem'}}
                                                            onClick={async () => {
                                                                if (!window.confirm(`Bạn có chắc chắn muốn XỬ THUA chiến mã ${result.horseName}? Kết quả sẽ bị đẩy xuống hạng chót.`)) return;
                                                                try {
                                                                    setResultsLoading(true);
                                                                    await axiosClient.put(`/referee/result/${result.id}`, {
                                                                        position: 99,
                                                                        finishTime: 999,
                                                                        prizeMoney: 0,
                                                                        version: result.version
                                                                    });
                                                                    setMessage('Đã xử thua thành công!');
                                                                    openModal(selectedRace, 'result'); // Refresh
                                                                } catch (err) {
                                                                    alert('Lỗi: ' + (err.response?.data?.error || 'Không thể xử thua'));
                                                                    setResultsLoading(false);
                                                                }
                                                            }}
                                                        >
                                                            ⚠️ Phạt Xử Thua
                                                        </button>
                                                    )}
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </>
                        )}
                    </div>
                </div>
            , document.body)}
        </div>
    );
};

export default RefereeDashboard;
