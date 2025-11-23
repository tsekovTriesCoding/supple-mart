import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`Making ${config.method?.toUpperCase()} request to ${config.url} with token:`, `${token.substring(0, 20)}...`);
    } else {
      console.log(`Making ${config.method?.toUpperCase()} request to ${config.url} without token`);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.log('API Error:', {
      status: error.response?.status,
      data: error.response?.data,
      url: error.config?.url,
      method: error.config?.method
    });
    
    // Don't logout on validation errors like wrong password
    if (error.response?.status === 401 || error.response?.status === 403) {
      const errorMessage = error.response?.data?.message || '';
      const isValidationError = errorMessage.toLowerCase().includes('password') || 
                                errorMessage.toLowerCase().includes('incorrect');
      
      if (!isValidationError) {
        console.log('Authentication failed, clearing tokens');
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
      }
    }
    return Promise.reject(error);
  }
);

export default api;