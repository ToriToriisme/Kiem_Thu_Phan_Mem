import React from 'react';
import { Route } from 'react-router-dom';
import ClientLayout from '../layouts/client/ClientLayout';
import PageTitle from '../components/PageTitle';
import Home from '../pages/client/Home';

import Profile from '../pages/client/Profile/Profile';

import GeneralInfo from '../pages/client/Profile/components/GeneralInfo';
import Security from '../pages/client/Profile/components/Security';
import HorseOwnerDashboard from '../pages/client/Profile/dashboards/HorseOwnerDashboard';
import JockeyDashboard from '../pages/client/Profile/dashboards/JockeyDashboard';
import RefereeDashboard from '../pages/client/Profile/dashboards/RefereeDashboard';
import SpectatorDashboard from '../pages/client/Profile/dashboards/SpectatorDashboard';

import Tournaments from '../pages/client/Tournaments/Tournaments';
import TournamentDetails from '../pages/client/Tournaments/TournamentDetails';
import RaceCenter from '../pages/client/RaceCenter/RaceCenter';
import RaceResults from '../pages/client/Results/RaceResults';
import RaceSimulation from '../pages/client/RaceSimulation/RaceSimulation';

export const ClientRoutes = (
  <Route path="/" element={<ClientLayout />}>
    <Route index element={<PageTitle title="Trang Chủ | EquineElite"><Home /></PageTitle>} />
    <Route path="tournaments" element={<PageTitle title="Giải Đấu | EquineElite"><Tournaments /></PageTitle>} />
    <Route path="tournaments/:id" element={<PageTitle title="Chi Tiết Giải Đấu | EquineElite"><TournamentDetails /></PageTitle>} />
    <Route path="races" element={<PageTitle title="Lịch Đua & Cá Cược | EquineElite"><RaceCenter /></PageTitle>} />
    <Route path="races/:raceId/live" element={<PageTitle title="Trường Đua Giả Lập | EquineElite"><RaceSimulation /></PageTitle>} />
    <Route path="results" element={<PageTitle title="Kết Quả & BXH | EquineElite"><RaceResults /></PageTitle>} />
    
    <Route path="profile" element={<PageTitle title="Hồ Sơ Cá Nhân | EquineElite"><Profile /></PageTitle>}>
      <Route index element={<GeneralInfo />} />
      <Route path="security" element={<Security />} />
      <Route path="owner-dashboard" element={<HorseOwnerDashboard />} />
      <Route path="jockey-dashboard" element={<JockeyDashboard />} />
      <Route path="referee-dashboard" element={<RefereeDashboard />} />
      <Route path="spectator-dashboard" element={<SpectatorDashboard />} />
    </Route>
  </Route>
);
