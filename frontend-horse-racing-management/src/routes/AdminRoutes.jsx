import React from 'react';
import { Route } from 'react-router-dom';
import ProtectedRoute from '../components/ProtectedRoute';
import AdminLayout from '../layouts/admin/AdminLayout';
import PageTitle from '../components/PageTitle';
import AdminDashboard from '../pages/admin/AdminDashboard';
import UserManagement from '../pages/admin/UserManagement/UserManagement';
import RoleManagement from '../pages/admin/RoleManagement/RoleManagement';
import TournamentManagement from '../pages/admin/TournamentManagement/TournamentManagement';
import PermissionWrapper from '../components/PermissionWrapper';
import AdminApprovalDashboard from '../pages/admin/AdminApprovalDashboard';

export const AdminRoutes = (
  <Route path="/admin" element={<ProtectedRoute requiredRole="ROLE_ADMIN" />}>
    <Route element={<PageTitle title="Admin Portal | EquineElite"><AdminLayout /></PageTitle>}>
      <Route index element={<PageTitle title="Dashboard | Admin"><AdminDashboard /></PageTitle>} />
      <Route path="users" element={<PermissionWrapper requiredPermission="PERM_USER_MANAGER"><PageTitle title="User Management | Admin"><UserManagement /></PageTitle></PermissionWrapper>} />
      <Route path="roles" element={<PermissionWrapper requiredPermission="PERM_ROLE_MANAGER"><PageTitle title="Role Management | Admin"><RoleManagement /></PageTitle></PermissionWrapper>} />
       <Route path="tournaments" element={<PermissionWrapper requiredPermission="PERM_TOURNAMENT_MANAGER"><PageTitle title="Quản lý giải đấu | Admin"><TournamentManagement /></PageTitle></PermissionWrapper>} />
       <Route path="approval-dashboard" element={<PermissionWrapper requiredPermission="PERM_TOURNAMENT_MANAGER"><PageTitle title="Duyệt đơn & Phân công | Admin"><AdminApprovalDashboard /></PageTitle></PermissionWrapper>} />
      <Route path="horses" element={<PermissionWrapper requiredPermission="PERM_HORSE_MANAGER"><PageTitle title="Entities Management | Admin"><div><h2>Entities Management</h2></div></PageTitle></PermissionWrapper>} />
    </Route>
  </Route>
);
