export interface Cart {
  id: string;
  userId: string;
  items: CartItem[];
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  id: string;
  productId: string;
  productName: string;
  productImageUrl: string;
  price: number;
  quantity: number;
  subtotal: number;
}

export interface AddCartItemRequest {
  productId: string;
  quantity: number;
}

export interface CartState {
  items: CartItem[];
  isLoading: boolean;
  error: string | null;
  isInitialized: boolean;
}

export type CartAction =
  | { type: 'LOADING_START' }
  | { type: 'LOADING_SUCCESS'; payload: CartItem[] }
  | { type: 'LOADING_ERROR'; payload: string }
  | { type: 'CLEAR_CART' }
  | { type: 'SET_INITIALIZED' };

export const formatCartPrice = (price: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(price);
};
