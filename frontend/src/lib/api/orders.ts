import { api } from './index';

export type OrderStatus = 
  | 'PENDING' 
  | 'PAID' 
  | 'PROCESSING' 
  | 'SHIPPED' 
  | 'DELIVERED' 
  | 'CANCELLED';

export interface OrderItem {
  id: string;
  product: {
    id: string;
    name: string;
    imageUrl: string;
    price: number;
  };
  quantity: number;
  price: number;
}

export interface Order {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
  items: OrderItem[];
  shippingAddress: string;
  stripePaymentIntentId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderFilters {
  status?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  limit?: number;
}

export interface OrdersResponse {
  orders: Order[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}

export interface CreateOrderRequest {
  shippingAddress: string;
}

export const ordersAPI = {
  createOrder: async (orderData: CreateOrderRequest): Promise<Order> => {
    const response = await api.post('/orders', orderData);
    return response.data;
  },

  getUserOrders: async (filters?: OrderFilters): Promise<OrdersResponse> => {
    const params = new URLSearchParams();
    
    if (filters?.status) params.append('status', filters.status);
    if (filters?.startDate) params.append('startDate', filters.startDate);
    if (filters?.endDate) params.append('endDate', filters.endDate);
    if (filters?.page) params.append('page', (filters.page - 1).toString());
    if (filters?.limit) params.append('limit', filters.limit.toString());

    const response = await api.get(`/orders?${params.toString()}`);
    return response.data;
  },

  getOrderById: async (orderId: string): Promise<Order> => {
    const response = await api.get(`/orders/${orderId}`);
    return response.data;
  },

  cancelOrder: async (orderId: string): Promise<Order> => {
    const response = await api.post(`/orders/${orderId}/cancel`);
    return response.data;
  },


  trackOrder: async (orderNumber: string): Promise<Order> => {
    const response = await api.get(`/orders/track/${orderNumber}`);
    return response.data;
  },

  getOrderHistory: async (orderId: string) => {
    const response = await api.get(`/orders/${orderId}/history`);
    return response.data;
  },

  requestReturn: async (orderId: string, reason: string, items?: string[]) => {
    const response = await api.post(`/orders/${orderId}/return`, {
      reason,
      items
    });
    return response.data;
  }
};
