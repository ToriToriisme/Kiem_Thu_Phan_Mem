import React from 'react';
import { useSelector } from 'react-redux';
import Unauthorized from '../pages/error/Unauthorized';

const PermissionWrapper = ({ requiredPermission, children }) => {
    const { user } = useSelector(state => state.auth);

    if (requiredPermission && (!user?.permissions || !user.permissions.includes(requiredPermission))) {
        return <Unauthorized />;
    }

    return children;
};

export default PermissionWrapper;
