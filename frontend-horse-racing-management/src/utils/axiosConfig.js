import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Khớp với mapping trong controller của bạn
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token'); // Lấy token khi đăng nhập thành công
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default apiClient;