import { api } from './index';
import type { 
  AdminProduct, 
  AdminProductsResponse,
  AdminOrdersResponse,
  AdminUsersResponse,
  CreateProductRequest, 
  UpdateProductRequest,
  DashboardStats
} from '../../types/admin';

export type {
  AdminProduct,
  AdminProductsResponse,
  CreateProductRequest,
  UpdateProductRequest,
  DashboardStats
};

class AdminAPI {
  async getDashboardStats(): Promise<DashboardStats> {
    const { data } = await api.get('admin/dashboard/stats');
    return data;
  }

  async getAllProducts(params?: {
    page?: number;
    limit?: number;
    search?: string;
    category?: string;
    sortBy?: string;
    sortOrder?: string;
  }): Promise<AdminProductsResponse> {
    const queryParams = new URLSearchParams();
    
    if (params?.page !== undefined) queryParams.append('page', (params.page - 1).toString());
    if (params?.limit) queryParams.append('size', params.limit.toString());
    if (params?.search) queryParams.append('search', params.search);
    if (params?.category && params.category !== 'all') queryParams.append('category', params.category);
    if (params?.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params?.sortOrder) queryParams.append('sortOrder', params.sortOrder);

    const { data } = await api.get(`admin/products?${queryParams.toString()}`);
    
    return {
      content: data.content || data.products || [],
      totalPages: data.totalPages,
      totalElements: data.totalElements,
      currentPage: data.number + 1,
      pageSize: data.size
    };
  }

  async createProduct(productData: CreateProductRequest): Promise<AdminProduct> {
    const { data } = await api.post('admin/products', productData);
    return data;
  }

  async updateProduct(id: number, productData: UpdateProductRequest): Promise<AdminProduct> {
    const { data } = await api.put(`admin/products/${id}`, productData);
    return data;
  }

  async deleteProduct(id: number): Promise<void> {
    await api.delete(`/admin/products/${id}`);
  }

  async uploadProductImage(file: File): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);

    const { data } = await api.post('admin/products/upload-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    
    return data.imageUrl || data.url || data;
  }

  async getAllOrders(params?: {
    page?: number;
    limit?: number;
    status?: string;
  }): Promise<AdminOrdersResponse> {
    const queryParams = new URLSearchParams();
    
    if (params?.page !== undefined) queryParams.append('page', (params.page - 1).toString());
    if (params?.limit) queryParams.append('size', params.limit.toString());
    if (params?.status && params.status !== 'all') queryParams.append('status', params.status.toUpperCase());

    const { data } = await api.get(`admin/orders?${queryParams.toString()}`);
    return {
      content: data.orders || data.content || [],
      currentPage: data.number + 1,
      pageSize: data.size,
      totalPages: data.totalPages,
      totalElements: data.totalElements
    };
  }

  async updateOrderStatus(orderId: number, status: string): Promise<void> {
    await api.patch(`admin/orders/${orderId}/status`, { status: status.toUpperCase() });
  }

  async getAllUsers(params?: {
    page?: number;
    limit?: number;
    search?: string;
    role?: string;
  }): Promise<AdminUsersResponse> {
    const queryParams = new URLSearchParams();
    
    if (params?.page !== undefined) queryParams.append('page', (params.page - 1).toString());
    if (params?.limit) queryParams.append('size', params.limit.toString());
    if (params?.search) queryParams.append('search', params.search);
    if (params?.role) queryParams.append('role', params.role);

    const { data } = await api.get(`admin/users?${queryParams.toString()}`);
    return {
      content: data.content,
      currentPage: data.number + 1,
      pageSize: data.size,
      totalPages: data.totalPages,
      totalElements: data.totalElements
    };
  }
}

export const adminAPI = new AdminAPI();
