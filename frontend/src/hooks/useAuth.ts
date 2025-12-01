import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authAPI } from '../lib/api';
import { useCart } from './useCart';
import type { User, UserRole } from '../types/auth';

interface UseAuthOptions {
  redirectTo?: string;
  requireAuth?: boolean;
}

export const useAuth = (options: UseAuthOptions = {}) => {
  const { redirectTo, requireAuth = false } = options;
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState<(User & { name?: string }) | null>(null);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const { refreshCart } = useCart();

  const checkAuth = useCallback(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');

    if (token && userData) {
      setIsLoggedIn(true);
      try {
        setUser(JSON.parse(userData));
      } catch {
        setUser(null);
      }
    } else {
      setIsLoggedIn(false);
      setUser(null);
    }
  }, []);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  useEffect(() => {
    if (requireAuth && !isLoggedIn && redirectTo) {
      navigate(redirectTo);
    }
  }, [requireAuth, isLoggedIn, redirectTo, navigate]);

  const logout = useCallback(async () => {
    if (isLoggingOut) return;

    setIsLoggingOut(true);
    try {
      await authAPI.logout();
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      // Always clear local data even if API fails
      localStorage.removeItem('token');
      localStorage.removeItem('user');

      await refreshCart();
      setIsLoggedIn(false);
      setUser(null);
      setIsLoggingOut(false);
      
      navigate('/');
      window.location.reload();
    }
  }, [isLoggingOut, refreshCart, navigate]);

  const isAdmin = user?.role === ('ADMIN' as UserRole);

  const getDisplayName = useCallback(() => {
    return user?.name || user?.firstName || 'User';
  }, [user]);

  return {
    isLoggedIn,
    user,
    isAdmin,
    isLoggingOut,
    logout,
    checkAuth,
    getDisplayName,
  };
};

export const useIsAuthenticated = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    setIsAuthenticated(!!token);
  }, []);

  return isAuthenticated;
};
