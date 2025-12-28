import axios from 'axios';

// In production/Docker, use relative path (nginx proxies to backend)
// In development, use VITE_API_URL or fallback to localhost:8080
const API_BASE_URL = import.meta.env.VITE_API_URL || 
  (import.meta.env.PROD ? '/api/' : 'http://localhost:8080/api/');

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
    // Handle common HTTP errors with user-friendly messages
    if (error.response) {
      const status = error.response.status;
      
      switch (status) {
        case 413:
          error.response.data = { 
            message: 'File size too large. Please upload a file smaller than 5MB' 
          };
          break;
        case 408:
          error.response.data = { 
            message: 'Request timed out. Please try again' 
          };
          break;
        case 429:
          error.response.data = { 
            message: 'Too many requests. Please wait a moment and try again' 
          };
          break;
        case 500:
        case 502:
        case 503:
        case 504:
          if (!error.response.data?.message) {
            error.response.data = { 
              message: 'Server error. Please try again later' 
            };
          }
          break;
      }
      
      // Don't logout on validation errors like wrong password
      if (status === 401 || status === 403) {
        const errorMessage = error.response?.data?.message || '';
        const isValidationError = errorMessage.toLowerCase().includes('password') || 
                                  errorMessage.toLowerCase().includes('incorrect');
        
        if (!isValidationError) {
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
        }
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;