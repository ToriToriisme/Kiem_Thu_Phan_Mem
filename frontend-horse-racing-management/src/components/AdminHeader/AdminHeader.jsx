import { FiSearch, FiBell, FiSettings } from 'react-icons/fi';
import './AdminHeader.css';

const AdminHeader = () => {
  return (
    <header className="admin-header">
      <div className="header-search">
        <div className="search-bar">
          <FiSearch className="search-icon" />
          <input type="text" placeholder="Search tournaments, horses, or users..." />
        </div>
      </div>

      <div className="header-actions">
        <button className="icon-btn">
          <FiBell size={20} />
        </button>
        <button className="icon-btn">
          <FiSettings size={20} />
        </button>
        <div className="user-profile">
          <div className="user-info">
            <span className="user-name">Admin User</span>
            <span className="user-role">SUPER ADMIN</span>
          </div>
          <img src="https://ui-avatars.com/api/?name=Admin+User&background=0D8ABC&color=fff" alt="Avatar" className="avatar" />
        </div>
      </div>
    </header>
  );
};

export default AdminHeader;
