import React, { useState, useEffect } from 'react';
import apiClient from '../utils/axiosConfig';

const OwnerDashboard = () => {
  const [userInfo, setUserInfo] = useState(null);
  const [horses, setHorses] = useState([]);
  const [jockeys, setJockeys] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // State cho Modal Thêm Ngựa
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newHorse, setNewHorse] = useState({
    name: '',
    age: '',
    wins: 0
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Hàm dịch trạng thái (Switch case)
  const translateStatus = (status) => {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return { text: 'Chờ xác nhận', color: 'bg-yellow-100 text-yellow-700' };
      case 'APPROVED':
        return { text: 'Đã duyệt', color: 'bg-green-100 text-green-700' };
      case 'REJECTED':
        return { text: 'Từ chối', color: 'bg-red-100 text-red-700' };
      case 'HEALTHY':
        return { text: 'Khỏe mạnh', color: 'bg-green-100 text-green-700' };
      case 'SICK':
        return { text: 'Đang ốm', color: 'bg-red-100 text-red-700' };
      default:
        return { text: status || 'Khỏe mạnh', color: 'bg-green-100 text-green-700' };
    }
  };

  // 1. Hàm load dữ liệu ban đầu
  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const userRes = await apiClient.get('/auth/me');
      setUserInfo(userRes.data);
      
      const horsesRes = await apiClient.get('/horses');
      setHorses(horsesRes.data);

      setJockeys([
        { id: 1, jockeyName: 'Nguyễn Văn A', horseName: 'Tía Chớp', tournament: 'Giải Mùa Hè 2026' }
      ]);
    } catch (err) {
      console.error("Lỗi tải dữ liệu:", err);
      setError("Không thể tải dữ liệu. Bạn đã đăng nhập chưa?");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  // 2. Hàm xử lý Thêm Ngựa Mới
  const handleAddHorseSubmit = async (e) => {
    e.preventDefault();
    try {
      setIsSubmitting(true);
      
      // Gọi API POST tới Backend Java
      await apiClient.post('/horses', {
        ...newHorse,
        age: parseInt(newHorse.age),
        // Nếu Backend cần ownerId để biết ngựa của ai, mở comment dòng dưới (cần chỉnh sửa theo Entity Backend)
        // ownerId: userInfo?.id 
      });

      // Tắt modal, reset form và load lại danh sách ngựa
      setIsModalOpen(false);
      setNewHorse({ name: '', age: '', wins: 0 });
      await fetchDashboardData(); 

    } catch (err) {
      console.error("Lỗi khi thêm ngựa:", err);
      alert("Có lỗi xảy ra khi thêm ngựa mới. Vui lòng kiểm tra console.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) return <div className="text-center py-10 font-bold text-blue-600">Đang tải dữ liệu Bảng điều khiển...</div>;
  if (error) return <div className="text-center py-10 font-bold text-red-500">{error}</div>;

  return (
    <div className="flex min-h-screen bg-[#f8f9fa] pt-10 justify-center font-sans relative">
      <div className="w-full max-w-[1200px] flex gap-6 px-4">
        
        {/* SIDEBAR TÀI KHOẢN */}
        <div className="w-64 flex-shrink-0">
          <div className="bg-white rounded-lg shadow-sm p-4 border border-gray-100">
            <h3 className="text-xs font-bold text-gray-400 mb-3 uppercase tracking-wider">Tài khoản</h3>
            <ul className="space-y-1 mb-6">
              <li className="px-3 py-2 text-gray-600 hover:bg-gray-50 rounded-md cursor-pointer flex items-center gap-2 text-sm">
                <i className="fa-regular fa-user"></i> Thông tin cơ bản
              </li>
              <li className="px-3 py-2 text-gray-600 hover:bg-gray-50 rounded-md cursor-pointer flex items-center gap-2 text-sm">
                <i className="fa-solid fa-lock"></i> Bảo mật
              </li>
            </ul>

            <h3 className="text-xs font-bold text-gray-400 mb-3 uppercase tracking-wider">Khu vực chủ ngựa</h3>
            <ul className="space-y-1">
              <li className="px-3 py-2 text-blue-600 bg-[#eff4ff] font-medium rounded-md cursor-pointer flex items-center gap-2 text-sm">
                <i className="fa-solid fa-briefcase"></i> Quản lý chung
              </li>
            </ul>
          </div>
        </div>

        {/* NỘI DUNG CHÍNH */}
        <div className="flex-1 bg-white rounded-lg shadow-sm border border-gray-100 p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Bảng điều khiển Chủ Ngựa</h2>
          <p className="text-gray-500 mb-8">Xin chào, {userInfo?.fullName || userInfo?.username}</p>

          {/* KHU VỰC 1: NGỰA CỦA TÔI */}
          <div className="mb-10">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-[17px] font-bold text-gray-800">Ngựa của tôi</h3>
              <button 
                onClick={() => setIsModalOpen(true)}
                className="bg-[#3b82f6] hover:bg-blue-600 text-white text-sm font-medium py-2 px-4 rounded-[6px] flex items-center gap-2 transition-all"
              >
                <span>+</span> Thêm ngựa mới
              </button>
            </div>
            
            <div className="border border-gray-200 rounded-lg overflow-hidden">
              <table className="w-full text-left text-sm text-gray-700">
                <thead className="bg-[#f8f9fa] text-gray-500 text-xs uppercase font-bold border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-4">Tên ngựa</th>
                    <th className="px-6 py-4">Tuổi</th>
                    <th className="px-6 py-4">Trạng thái</th>
                    <th className="px-6 py-4 text-center">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {horses.length > 0 ? (
                    horses.map((horse, index) => (
                      <tr key={index} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 font-semibold text-gray-900">{horse.name || `Ngựa #${horse.id}`}</td>
                        <td className="px-6 py-4">{horse.age || 'N/A'}</td>
                        <td className="px-6 py-4">
                          <span className={`px-2 py-1 text-xs rounded-full font-medium ${translateStatus(horse.status).color}`}>
                            {translateStatus(horse.status).text}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-center">
                          <button className="bg-gray-100 hover:bg-gray-200 text-gray-800 text-xs font-semibold py-1.5 px-4 rounded-[4px] border border-gray-200">
                            Chi tiết
                          </button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr><td colSpan="4" className="text-center py-6 text-gray-500">Chưa có dữ liệu ngựa.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* KHU VỰC 2: QUẢN LÝ JOCKEY */}
          <div>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-[17px] font-bold text-gray-800">
                Quản lý Jockey <br/><span className="text-sm font-normal text-gray-500">(Nài ngựa)</span>
              </h3>
              <button className="border border-gray-300 text-gray-700 text-sm font-medium py-2 px-8 rounded-[6px] hover:bg-gray-50 flex items-center gap-2">
                Xem tất cả
              </button>
            </div>
            <div className="border border-gray-200 rounded-lg overflow-hidden">
              <table className="w-full text-left text-sm text-gray-700">
                <thead className="bg-[#f8f9fa] text-gray-500 text-xs uppercase font-bold border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-4">Jockey</th>
                    <th className="px-6 py-4">Ngựa</th>
                    <th className="px-6 py-4">Giải đấu</th>
                    <th className="px-6 py-4">Trạng thái</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {jockeys.map((jockey, index) => (
                    <tr key={index} className="hover:bg-gray-50">
                      <td className="px-6 py-4 font-semibold text-gray-900">{jockey.jockeyName}</td>
                      <td className="px-6 py-4">{jockey.horseName}</td>
                      <td className="px-6 py-4">{jockey.tournament}</td>
                      <td className="px-6 py-4">
                        <span className={`px-2 py-1 text-xs rounded-full font-medium ${translateStatus(jockey.status).color}`}>
                          {translateStatus(jockey.status).text}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* MODAL THÊM NGỰA MỚI */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md shadow-xl">
            <h3 className="text-xl font-bold text-gray-800 mb-4">Thêm ngựa mới</h3>
            <form onSubmit={handleAddHorseSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">Tên ngựa</label>
                <input 
                  type="text" 
                  required
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  value={newHorse.name}
                  onChange={(e) => setNewHorse({...newHorse, name: e.target.value})}
                  placeholder="Ví dụ: Tía Chớp"
                />
              </div>
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-1">Tuổi</label>
                <input 
                  type="number" 
                  required
                  min="1"
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  value={newHorse.age}
                  onChange={(e) => setNewHorse({...newHorse, age: e.target.value})}
                  placeholder="Ví dụ: 3"
                />
              </div>
              <div className="flex justify-end gap-3">
                <button 
                  type="button" 
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50"
                >
                  Hủy
                </button>
                <button 
                  type="submit" 
                  disabled={isSubmitting}
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-blue-300"
                >
                  {isSubmitting ? 'Đang lưu...' : 'Lưu thông tin'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default OwnerDashboard;