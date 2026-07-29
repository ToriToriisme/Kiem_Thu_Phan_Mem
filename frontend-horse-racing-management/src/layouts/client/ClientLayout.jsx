import { Outlet } from 'react-router-dom';
import ClientNavbar from '../../components/ClientNavbar/ClientNavbar';
import ClientFooter from '../../components/ClientFooter/ClientFooter';
import './ClientLayout.css';

const ClientLayout = () => {
  return (
    <div className="client-layout">
      <ClientNavbar />
      <main className="client-main">
        <Outlet />
      </main>
      <ClientFooter />
    </div>
  );
};

export default ClientLayout;
