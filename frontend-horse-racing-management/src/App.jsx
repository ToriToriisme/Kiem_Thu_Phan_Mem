import React, { useEffect } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchCurrentUser, setInitDone } from './redux/slices/authSlice';
import AppRoutes from './routes/AppRoutes';



function App() {
  const dispatch = useDispatch();
  const { isInitializing } = useSelector((state) => state.auth);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      dispatch(fetchCurrentUser());
    } else {
      dispatch(setInitDone());
    }
  }, [dispatch]);

  if (isInitializing) {
    return <div>Loading...</div>; // Show a loading screen while checking auth
  }

  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}

export default App;
