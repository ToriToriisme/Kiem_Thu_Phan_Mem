import './ClientFooter.css';

const ClientFooter = () => {
  return (
    <footer className="client-footer">
      <div className="footer-content">
        <span className="footer-logo">EquineElite</span>
        <p>&copy; 2024 EquineElite Thoroughbreds. All rights reserved.</p>
      </div>
      <div className="footer-links">
        <a href="#">Privacy Policy</a>
        <a href="#">Terms of Service</a>
        <a href="#">Pedigree Guide</a>
        <a href="#">Contact Support</a>
      </div>
    </footer>
  );
};

export default ClientFooter;
