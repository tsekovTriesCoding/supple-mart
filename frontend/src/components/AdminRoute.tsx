import { Navigate } from 'react-router-dom';
import type { UserRole } from '../types/auth';

interface AdminRouteProps {
  children: React.ReactNode;
}

const AdminRoute = ({ children }: AdminRouteProps) => {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');

  if (!token || !userStr) {
    return <Navigate to="/login" replace />;
  }

  try {
    const user = JSON.parse(userStr);
    const userRole: UserRole = user.role;

    if (userRole !== 'ADMIN') {
      return <Navigate to="/" replace />;
    }

    return <>{children}</>;
  } catch (error) {
    console.error('Error parsing user data:', error);
    return <Navigate to="/login" replace />;
  }
};

export default AdminRoute;
