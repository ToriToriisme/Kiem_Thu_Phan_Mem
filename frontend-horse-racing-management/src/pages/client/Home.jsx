import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axiosClient from '../../services/axiosClient';
import '../../Home.css';

const Home = () => {
  const navigate = useNavigate();
  const [hotRaces, setHotRaces] = useState([]);

  useEffect(() => {
    fetchHotRaces();
  }, []);

  const fetchHotRaces = async () => {
    try {
      const response = await axiosClient.get('/v1/spectator/races');
      const upcoming = (response || [])
        .filter(r => r.status === 'SCHEDULED' || r.status === 'IN_PROGRESS')
        .slice(0, 2);
      setHotRaces(upcoming);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="home-container">
      {/* HERO SECTION */}
      <div className="hero-section">
        <div className="hero-content">
          <div className="badge-portal">MÙA GIẢI 2026</div>
          <h1 className="hero-title">Khám Phá Kỷ Nguyên <span>Đua Ngựa Đỉnh Cao</span></h1>
          <p className="hero-subtitle">
            Trải nghiệm cảm giác hồi hộp tột độ tại EquineElite. Theo dõi trực tiếp, phân tích chiến thuật, và đặt cược cho chiến mã yêu thích của bạn để giành lấy những phần thưởng giá trị.
          </p>
          <div className="hero-actions">
            <Link to="/races" className="btn-primary">Vào Tiền Ngay</Link>
            <Link to="/tournaments" className="btn-secondary">Xem Các Giải Đấu</Link>
          </div>
        </div>
      </div>

      {/* CONTENT SECTION */}
      <div className="content-wrapper">
        
        {/* VÒNG ĐUA ĐANG NÓNG */}
        {hotRaces.length > 0 && (
          <section style={{ marginBottom: '60px' }}>
            <div className="section-header">
              <div>
                <h2 className="section-title">🔥 Vòng Đua Đang Nóng</h2>
                <p className="section-desc">Những trận thư hùng sắp sửa diễn ra</p>
              </div>
              <Link to="/races" className="link-manage">Xem toàn bộ lịch &rarr;</Link>
            </div>
            <div className="hot-races-grid">
              {hotRaces.map(race => (
                <div key={race.id} className="race-mini-card">
                  <div className="rmc-info">
                    <h3>{race.name}</h3>
                    <p>Cự ly: {race.distance}m • Khởi tranh: {new Date(race.startTime).toLocaleTimeString('vi-VN')}</p>
                  </div>
                  <div>
                    <span className="rmc-status">
                      {race.status === 'IN_PROGRESS' ? 'Đang Chạy' : 'Sắp Diễn Ra'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* TOP NGỰA XUẤT SẮC */}
        <section className="dashboard-section">
          <div className="section-header">
            <div>
              <h2 className="section-title">🌟 Chiến Mã Xuất Sắc Nhất</h2>
              <p className="section-desc">Những huyền thoại trên đường đua EquineElite</p>
            </div>
            <Link to="/results" className="link-manage">Bảng Xếp Hạng &rarr;</Link>
          </div>
          
          <div className="horse-grid">
            {/* Horse Card 1 */}
            <div className="horse-card">
              <div className="horse-img-container">
                <span className="status-badge active-badge">Phong Độ Cao</span>
                <img src="https://images.unsplash.com/photo-1553284965-83fd3e82fa5a?auto=format&fit=crop&w=500&q=80" alt="Hắc Mã" className="horse-img" />
              </div>
              <div className="horse-card-content">
                <h3>Xích Thố</h3>
                <p className="horse-details">Tuổi: 4 • Giống: Ả Rập</p>
                <div className="stats-grid">
                  <div className="stat-box">
                    <span className="stat-label">Tốc Độ</span>
                    <span className="stat-number">98</span>
                  </div>
                  <div className="stat-box">
                    <span className="stat-label">Thể Lực</span>
                    <span className="stat-number">92</span>
                  </div>
                </div>
                <button className="btn-outline" onClick={() => navigate('/results')}>Xem Lịch Sử Thi Đấu</button>
              </div>
            </div>

            {/* Horse Card 2 */}
            <div className="horse-card">
              <div className="horse-img-container">
                <span className="status-badge active-badge">Mới Nổi</span>
                <img src="https://images.unsplash.com/photo-1598974357801-cbca100e65d3?auto=format&fit=crop&w=500&q=80" alt="Bạch Mã" className="horse-img" />
              </div>
              <div className="horse-card-content">
                <h3>Bạch Long</h3>
                <p className="horse-details">Tuổi: 3 • Giống: Thuần Chủng Anh</p>
                <div className="stats-grid">
                  <div className="stat-box">
                    <span className="stat-label">Tốc Độ</span>
                    <span className="stat-number">89</span>
                  </div>
                  <div className="stat-box">
                    <span className="stat-label">Thể Lực</span>
                    <span className="stat-number">95</span>
                  </div>
                </div>
                <button className="btn-outline" onClick={() => navigate('/results')}>Xem Lịch Sử Thi Đấu</button>
              </div>
            </div>

            {/* Horse Card 3 */}
            <div className="horse-card">
              <div className="horse-img-container">
                <span className="status-badge active-badge">Kinh Nghiệm</span>
                <img src="https://images.unsplash.com/photo-1534723145455-cb2be994d5ff?auto=format&fit=crop&w=500&q=80" alt="Ngựa Nâu" className="horse-img" />
              </div>
              <div className="horse-card-content">
                <h3>Hoàng Kim</h3>
                <p className="horse-details">Tuổi: 5 • Giống: Warmblood</p>
                <div className="stats-grid">
                  <div className="stat-box">
                    <span className="stat-label">Tốc Độ</span>
                    <span className="stat-number">91</span>
                  </div>
                  <div className="stat-box">
                    <span className="stat-label">Thể Lực</span>
                    <span className="stat-number">97</span>
                  </div>
                </div>
                <button className="btn-outline" onClick={() => navigate('/results')}>Xem Lịch Sử Thi Đấu</button>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
};

export default Home;
