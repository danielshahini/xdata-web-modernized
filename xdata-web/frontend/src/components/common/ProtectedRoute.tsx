import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[];
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const { isAuthenticated, isAdmin, isInstructor, isStudent } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles) {
    const roles = allowedRoles.map(r => r.toUpperCase());
    let isAllowed = false;
    
    if (roles.includes('ADMIN') && isAdmin) isAllowed = true;
    if (roles.includes('INSTRUCTOR') && isInstructor) isAllowed = true;
    if (roles.includes('STUDENT') && isStudent) isAllowed = true;

    if (!isAllowed) {
      // Redirect to their default dashboard if role not allowed
      return <Navigate to="/" replace />;
    }
  }

  return <>{children}</>;
};

export default ProtectedRoute;
