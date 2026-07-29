import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useSelector } from 'react-redux';

const ProtectedRoute = ({ requiredRole }) => {
    const { isAuthenticated, user } = useSelector(state => state.auth);


    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }


    if (requiredRole && user?.role !== requiredRole && user?.role !== 'ROLE_ADMIN') {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};

export default ProtectedRoute;