import React from 'react';
import { Link } from 'react-router-dom';

const Unauthorized = () => {
  return (
    <div style={styles.container}>
      <h1 style={styles.title}>403 - Không có quyền truy cập</h1>
      <p style={styles.text}>Bạn không có quyền (permission) để xem trang này.</p>
      <Link to="/admin" style={styles.button}>Quay lại Dashboard</Link>
    </div>
  );
};

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    height: '100%',
    minHeight: '400px',
    textAlign: 'center',
    padding: '2rem'
  },
  title: {
    fontSize: '2rem',
    color: '#ef4444',
    marginBottom: '1rem'
  },
  text: {
    fontSize: '1.1rem',
    color: '#4b5563',
    marginBottom: '2rem'
  },
  button: {
    padding: '10px 20px',
    backgroundColor: '#3b82f6',
    color: 'white',
    textDecoration: 'none',
    borderRadius: '8px',
    fontWeight: '600'
  }
};

export default Unauthorized;
