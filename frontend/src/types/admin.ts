export interface AdminProduct {
  id: number;
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  category: string;
  stockQuantity: number;
  imageUrl?: string;
  inStock: boolean;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
  totalSales?: number;
  views?: number;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  category: string;
  stockQuantity: number;
  imageUrl?: string;
  isActive: boolean;
}

export interface UpdateProductRequest {
  name?: string;
  description?: string;
  price?: number;
  originalPrice?: number;
  category?: string;
  stockQuantity?: number;
  imageUrl?: string;
  isActive?: boolean;
}

export interface AdminProductsResponse {
  content: AdminProduct[];
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
}

export interface DashboardStats {
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
  totalCustomers: number;
  lowStockProducts: number;
}

export interface AdminOrder {
  id: number;
  orderNumber: string;
  customerName: string;
  customerEmail: string;
  totalAmount: number;
  status: string;
  createdAt: string;
  items: AdminOrderItem[];
}

export interface AdminOrderItem {
  id: number;
  productName: string;
  quantity: number;
  price: number;
}

export interface AdminUser {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  createdAt: string;
  lastLogin?: string;
}

export interface AdminOrdersResponse {
  content: AdminOrder[];
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
}

export interface AdminUsersResponse {
  content: AdminUser[];
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
}

export interface CacheStats {
  size: number;
  hitCount: number;
  missCount: number;
  hitRate: number;
  evictionCount: number;
}

export interface CacheStatsResponse {
  [cacheName: string]: CacheStats;
}
