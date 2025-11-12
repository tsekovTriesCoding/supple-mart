import { api } from './index';

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

    const response = await api.get(`products?${queryParams.toString()}`);
    return response.data;
  },

  getProductById: async (id: string | number) => {
    const response = await api.get(`products/${id}`);
    return response.data;
  },

  getCategories: async () => {
    const response = await api.get('products/categories');
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

    const response = await api.get(`products/search?${queryParams.toString()}`);
    return response.data;
  },

  getFeaturedProducts: async (limit?: number) => {
    const queryParams = limit ? `?limit=${limit}` : '';
    const response = await api.get(`products/featured${queryParams}`);
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

    const response = await api.get(`products?${queryParams.toString()}`);
    return response.data;
  },
};

export default productsAPI;
