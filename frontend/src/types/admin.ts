// Admin-specific product type with all management fields
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
  createdAt?: string;
  updatedAt?: string;
  // Admin-specific fields that customers don't see
  totalSales?: number;
  views?: number;
}

// Request type for creating a product
export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  category: string;
  stock: number;
  imageUrl?: string;
}

// Request type for updating a product
export interface UpdateProductRequest {
  name?: string;
  description?: string;
  price?: number;
  originalPrice?: number;
  category?: string;
  stock?: number;
  imageUrl?: string;
}

// Paginated response for admin products list
export interface AdminProductsResponse {
  products: AdminProduct[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  pageSize: number;
}

// Dashboard statistics
export interface DashboardStats {
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
  totalCustomers: number;
  lowStockProducts: number;
}

// Admin order type (simplified for now)
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

// Admin user type
export interface AdminUser {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  createdAt: string;
  lastLogin?: string;
}
