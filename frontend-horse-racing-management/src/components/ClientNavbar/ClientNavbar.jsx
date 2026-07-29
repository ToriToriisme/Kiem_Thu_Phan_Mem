import { useState, useRef, useEffect } from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { FiSearch, FiBell, FiUser, FiLogOut, FiSettings } from 'react-icons/fi';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../../redux/slices/authSlice';
import './ClientNavbar.css';

const ClientNavbar = () => {
  const { isAuthenticated, user } = useSelector((state) => state.auth);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleLogout = () => {
    dispatch(logout());
    setShowDropdown(false);
    navigate('/login');
  };

  return (
    <nav className="client-navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          EQUINEELITE
        </Link>
        
        <ul className="navbar-menu">
          <li>
            <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>Trang Chủ</NavLink>
          </li>
          <li>
            <NavLink to="/tournaments" className={({ isActive }) => (isActive ? 'active' : '')}>Giải Đấu</NavLink>
          </li>
          <li>
            <NavLink to="/races" className={({ isActive }) => (isActive ? 'active' : '')}>Lịch Đua & Cá Cược</NavLink>
          </li>
          <li>
            <NavLink to="/results" className={({ isActive }) => (isActive ? 'active' : '')}>Kết Quả & BXH</NavLink>
          </li>
        </ul>

        <div className="navbar-actions">
          <div className="search-bar-client">
            <FiSearch className="search-icon" />
            <input type="text" placeholder="Search bloodlines..." />
          </div>
          
          {isAuthenticated ? (
            <>
              <button className="icon-btn-client">
                <FiBell size={20} />
              </button>
              
              <div className="user-dropdown-container" ref={dropdownRef}>
                <button 
                  className="icon-btn-client user-avatar-btn"
                  onClick={() => setShowDropdown(!showDropdown)}
                >
                  <FiUser size={20} />
                </button>

                {showDropdown && (
                  <div className="user-dropdown-menu">
                    <div className="user-dropdown-header">
                      <p className="user-dropdown-name">{user?.fullName || user?.username || 'User'}</p>
                      <p className="user-dropdown-email">{user?.email}</p>
                    </div>
                    
                    <ul className="user-dropdown-list">
                      {user?.role === 'ROLE_ADMIN' && (
                        <li>
                          <Link to="/admin" onClick={() => setShowDropdown(false)}>
                            <FiSettings className="dropdown-icon" />
                            <span>Trang quản lý</span>
                          </Link>
                        </li>
                      )}
                      <li>
                        <Link to="/profile" onClick={() => setShowDropdown(false)}>
                          <FiUser className="dropdown-icon" />
                          <span>Hồ sơ cá nhân</span>
                        </Link>
                      </li>
                      <li className="dropdown-divider"></li>
                      <li>
                        <button onClick={handleLogout} className="logout-btn">
                          <FiLogOut className="dropdown-icon" />
                          <span>Đăng xuất</span>
                        </button>
                      </li>
                    </ul>
                  </div>
                )}
              </div>
            </>
          ) : (
            <Link to="/login" className="login-link-box">
              Đăng nhập
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
};

export default ClientNavbar;
