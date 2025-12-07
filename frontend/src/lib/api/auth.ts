import { api } from './index';
import type { OAuth2ProvidersResponse } from '../../types/auth';

// OAuth2 URLs should go directly to the backend, not through /api/ prefix
const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

export const authAPI = {
  login: async (email: string, password: string) => {
    const response = await api.post('auth/login', { email, password });
    return response.data;
  },

  register: async (userData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }) => {
    const response = await api.post('auth/register', userData);
    return response.data;
  },

  logout: async () => {
    try {
      await api.post('auth/logout');
    } catch (error) {
      console.warn('Logout API call failed:', error);
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  },

  refreshToken: async () => {
    const response = await api.post('auth/refresh');
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await api.get('auth/me');
    return response.data;
  },

  // OAuth2 methods
  getOAuth2Providers: async (): Promise<OAuth2ProvidersResponse> => {
    const response = await api.get('auth/oauth2/providers');
    return response.data;
  },

  /**
   * Get the full OAuth2 authorization URL for a provider
   */
  getOAuth2AuthorizationUrl: (provider: string): string => {
    return `${BACKEND_BASE_URL}/oauth2/authorization/${provider}`;
  },

  /**
   * Handle OAuth2 callback - process tokens from URL
   */
  handleOAuth2Callback: (token: string, refreshToken: string): void => {
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
  }
};

export default authAPI;
