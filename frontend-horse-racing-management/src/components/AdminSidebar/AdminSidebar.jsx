import { NavLink } from 'react-router-dom';
import { MdDashboard, MdSettings, MdHelpOutline } from 'react-icons/md';
import { FaUsers, FaTrophy, FaPlus, FaClipboardList } from 'react-icons/fa';
import { GiHorseHead } from 'react-icons/gi';
import { useSelector } from 'react-redux';
import './AdminSidebar.css';

const AdminSidebar = () => {
  const { user } = useSelector(state => state.auth);

  const hasPermission = (permission) => {
    return user?.permissions?.includes(permission);
  };

  return (
    <aside className="admin-sidebar">
      <div className="sidebar-logo">
        <h2>EquineElite</h2>
        <span className="logo-sub">Management Portal</span>
      </div>
      
      <div className="sidebar-content">
        <ul className="sidebar-menu">
          <li>
            <NavLink to="/admin" end className={({ isActive }) => (isActive ? 'active' : '')}>
              <MdDashboard size={20} className="menu-icon" /> Dashboard
            </NavLink>
          </li>
          {hasPermission('PERM_USER_MANAGER') && (
            <li>
              <NavLink to="/admin/users" className={({ isActive }) => (isActive ? 'active' : '')}>
                <FaUsers size={20} className="menu-icon" /> Quản lý User
              </NavLink>
            </li>
          )}
          {hasPermission('PERM_ROLE_MANAGER') && (
            <li>
              <NavLink to="/admin/roles" className={({ isActive }) => (isActive ? 'active' : '')}>
                <MdSettings size={20} className="menu-icon" /> Quản lý Vai trò
              </NavLink>
            </li>
          )}
          {hasPermission('PERM_TOURNAMENT_MANAGER') && (
            <li>
              <NavLink to="/admin/tournaments" className={({ isActive }) => (isActive ? 'active' : '')}>
                <FaTrophy size={20} className="menu-icon" /> Quản lý Giải Đấu
              </NavLink>
            </li>
          )}
          {hasPermission('PERM_TOURNAMENT_MANAGER') && (
            <li>
              <NavLink to="/admin/approval-dashboard" className={({ isActive }) => (isActive ? 'active' : '')}>
                <FaClipboardList size={20} className="menu-icon" /> Duyệt & Phân công
              </NavLink>
            </li>
          )}
          {hasPermission('PERM_HORSE_MANAGER') && (
            <li>
              <NavLink to="/admin/horses" className={({ isActive }) => (isActive ? 'active' : '')}>
                <GiHorseHead size={20} className="menu-icon" /> Quản lý Ngựa
              </NavLink>
            </li>
          )}
        </ul>
      </div>

      <div className="sidebar-footer">
        {hasPermission('PERM_TOURNAMENT_MANAGER') && (
          <button className="btn-create-tournament">
            <FaPlus size={14} /> Tạo Giải Đấu
          </button>
        )}
        <ul className="sidebar-bottom-menu">
          <li>
            <a href="#settings"><MdSettings size={20} className="menu-icon" /> Settings</a>
          </li>
          <li>
            <a href="#support"><MdHelpOutline size={20} className="menu-icon" /> Support</a>
          </li>
        </ul>
      </div>
    </aside>
  );
};

export default AdminSidebar;
