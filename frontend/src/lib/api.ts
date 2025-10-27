import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

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
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: async (email: string, password: string) => {
    const response = await api.post('/auth/login', { email, password });
    return response.data;
  },

  register: async (userData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }) => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  logout: async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.warn('Logout API call failed:', error);
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  },

  refreshToken: async () => {
    const response = await api.post('/auth/refresh');
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await api.get('/auth/me');
    return response.data;
  }
};

export const productsAPI = {
  getProducts: async (params?: {
    page?: number;
    limit?: number;
    category?: string;
    search?: string;
    minPrice?: number;
    maxPrice?: number;
    sortBy?: 'name' | 'price' | 'createdAt';
    sortOrder?: 'asc' | 'desc';
  }) => {
    const queryParams = new URLSearchParams();

    if (params?.page) queryParams.append('page', (params.page - 1).toString());
    if (params?.limit) queryParams.append('limit', params.limit.toString());
    if (params?.category) queryParams.append('category', params.category);
    if (params?.search) queryParams.append('search', params.search);
    if (params?.minPrice) queryParams.append('minPrice', params.minPrice.toString());
    if (params?.maxPrice) queryParams.append('maxPrice', params.maxPrice.toString());
    if (params?.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params?.sortOrder) queryParams.append('sortDirection', params.sortOrder);

    const response = await api.get(`/products?${queryParams.toString()}`);
    return response.data;
  },

  getProductById: async (id: string | number) => {
    const response = await api.get(`/products/${id}`);
    return response.data;
  },

  getCategories: async () => {
    const response = await api.get('/products/categories');
    return response.data;
  },

  searchProducts: async (searchTerm: string, filters?: {
    category?: string;
    minPrice?: number;
    maxPrice?: number;
  }) => {
    const queryParams = new URLSearchParams();
    queryParams.append('search', searchTerm);

    if (filters?.category) queryParams.append('category', filters.category);
    if (filters?.minPrice) queryParams.append('minPrice', filters.minPrice.toString());
    if (filters?.maxPrice) queryParams.append('maxPrice', filters.maxPrice.toString());

    const response = await api.get(`/products/search?${queryParams.toString()}`);
    return response.data;
  },

  getFeaturedProducts: async (limit?: number) => {
    const queryParams = limit ? `?limit=${limit}` : '';
    const response = await api.get(`/products/featured${queryParams}`);
    return response.data;
  },

  getProductsByCategory: async (category: string, params?: {
    page?: number;
    limit?: number;
    sortBy?: 'name' | 'price' | 'createdAt';
    sortOrder?: 'asc' | 'desc';
  }) => {
    const queryParams = new URLSearchParams();
    queryParams.append('category', category);

    if (params?.page) queryParams.append('page', (params.page - 1).toString());
    if (params?.limit) queryParams.append('limit', params.limit.toString());
    if (params?.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params?.sortOrder) queryParams.append('sortDirection', params.sortOrder);

    const response = await api.get(`/products?${queryParams.toString()}`);
    return response.data;
  },

  // Admin/Auth required endpoints (if user has admin privileges)
  createProduct: async (productData: {
    name: string;
    description: string;
    price: number;
    category: string;
    imageUrl?: string;
    stock?: number;
  }) => {
    const response = await api.post('/products', productData);
    return response.data;
  },

  updateProduct: async (id: string | number, productData: {
    name?: string;
    description?: string;
    price?: number;
    category?: string;
    imageUrl?: string;
    stock?: number;
  }) => {
    const response = await api.put(`/products/${id}`, productData);
    return response.data;
  },

  deleteProduct: async (id: string | number) => {
    const response = await api.delete(`/products/${id}`);
    return response.data;
  }
};

export const cartAPI = {
  // Get user's cart
  getCart: async () => {
    const response = await api.get('/cart');
    return response.data;
  },

  // Add item to cart
  addItem: async (productId: string, quantity: number = 1) => {
    const response = await api.post('/cart/items', {
      productId,
      quantity
    });
    return response.data;
  },

  // Update item quantity in cart
  updateItemQuantity: async (cartItemId: string, quantity: number) => {
    const response = await api.put(`/cart/items/${cartItemId}`, {
      quantity
    });
    return response.data;
  },

  // Remove item from cart
  removeItem: async (cartItemId: string) => {
    const response = await api.delete(`/cart/items/${cartItemId}`);
    return response.data;
  },

  // Clear entire cart
  clearCart: async () => {
    const response = await api.delete('/cart');
    return response.data;
  }
};

export default api;
