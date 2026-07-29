import RegisterTournamentForm from '../components/RegisterTournamentForm';
import React from 'react';
import { Routes, Route } from 'react-router-dom';
import PageTitle from '../components/PageTitle';
import NotFound from '../pages/NotFound';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import AdminApprovalDashboard from '../pages/admin/AdminApprovalDashboard';

import { ClientRoutes } from './ClientRoutes';
import { AdminRoutes } from './AdminRoutes';
import JockeyDashboard from '../pages/client/Profile/dashboards/JockeyDashboard';

const AppRoutes = () => {
  return (
      <Routes>
        {/* Auth Routes */}
        <Route path="/login" element={<PageTitle title="Đăng Nhập"><Login /></PageTitle>} />
        <Route path="/register" element={<PageTitle title="Đăng Ký"><Register /></PageTitle>} />

        {/* Client Routes */}
        {ClientRoutes}

        {/* Custom Routes */}
        <Route path="/register-tournament" element={<PageTitle title="Đăng Ký Giải Đấu"><RegisterTournamentForm /></PageTitle>} />
        <Route path="/jockey-dashboard" element={<JockeyDashboard />} />

      {/* Admin Routes */}
      {AdminRoutes}
        <Route path="/admin/approval-dashboard" element={<PageTitle title="Duyệt đơn & Phân công"><AdminApprovalDashboard /></PageTitle>} />

        {/* Not Found */}
        <Route path="*" element={<PageTitle title="404 Not Found"><NotFound /></PageTitle>} />
      </Routes>
  );
};

export default AppRoutes;