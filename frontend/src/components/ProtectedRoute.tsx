import { Navigate } from 'react-router-dom';

import type { UserRole } from '../types/auth';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: UserRole;
}

export const ProtectedRoute = ({ children, requiredRole }: ProtectedRouteProps) => {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  
  if (!token || !userStr) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole) {
    try {
      const user = JSON.parse(userStr);
      const userRole: UserRole = user.role;

      if (userRole !== requiredRole) {
        return <Navigate to="/" replace />;
      }
    } catch (error) {
      console.error('Error parsing user data:', error);
      return <Navigate to="/" replace />;
    }
  }

  return <>{children}</>;
};
