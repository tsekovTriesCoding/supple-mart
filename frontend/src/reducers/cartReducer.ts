import type { CartState, CartAction } from '../types/cart';

export const cartReducer = (state: CartState, action: CartAction): CartState => {
  switch (action.type) {
    case 'LOADING_START':
      return { ...state, isLoading: true, error: null };
    case 'LOADING_SUCCESS':
      return { ...state, isLoading: false, items: action.payload, error: null };
    case 'LOADING_ERROR':
      return { ...state, isLoading: false, error: action.payload };
    case 'CLEAR_CART':
      return { ...state, items: [], isLoading: false, error: null };
    case 'SET_INITIALIZED':
      return { ...state, isInitialized: true };
    default:
      return state;
  }
};

export const initialCartState: CartState = {
  items: [],
  isLoading: false,
  error: null,
  isInitialized: false,
};
