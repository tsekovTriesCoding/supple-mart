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

export interface OrderStats {
  totalOrders: number;
  pendingCount: number;
  paidCount: number;
  processingCount: number;
  shippedCount: number;
  deliveredCount: number;
  cancelledCount: number;
  totalSpent: number;
}

export interface OrdersState {
  orders: Order[];
  stats: OrderStats | null;
  loading: boolean;
  statsLoading: boolean;
  error: string | null;
  totalElements: number;
  totalPages: number;
  currentPage: number;
  filters: OrderFilters;
}

export type OrdersAction =
  | { type: 'FETCH_START' }
  | { type: 'FETCH_SUCCESS'; payload: { orders: Order[]; totalElements: number; totalPages: number; currentPage: number } }
  | { type: 'FETCH_ERROR'; payload: string }
  | { type: 'STATS_FETCH_START' }
  | { type: 'STATS_FETCH_SUCCESS'; payload: OrderStats }
  | { type: 'STATS_FETCH_ERROR' }
  | { type: 'UPDATE_ORDER'; payload: Order }
  | { type: 'SET_FILTERS'; payload: OrderFilters }
  | { type: 'CLEAR_ERROR' };
