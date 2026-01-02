import { useReducer, useEffect, useCallback, useMemo } from 'react';
import type { ReactNode } from 'react';
import { useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';

import { CartContext, type CartContextType } from '../hooks';
import { cartAPI } from '../lib/api';
import { cartReducer, initialCartState } from '../reducers/cartReducer';

export const CartProvider = ({ children }: { children: ReactNode }) => {
  const location = useLocation();
  const [state, dispatch] = useReducer(cartReducer, initialCartState);

  const refreshCart = useCallback(async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      dispatch({ type: 'CLEAR_CART' });
      return;
    }

    dispatch({ type: 'LOADING_START' });
    try {
      const cart = await cartAPI.getCart();
      dispatch({ type: 'LOADING_SUCCESS', payload: cart.items || [] });
    } catch (err) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { status?: number } };
        if (axiosError.response?.status === 401 || axiosError.response?.status === 403) {
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          dispatch({ type: 'LOADING_ERROR', payload: 'Session expired. Please login again.' });
          toast.error('Session expired. Please login again.');
          return;
        } else if (axiosError.response?.status === 404) {
          dispatch({ type: 'LOADING_SUCCESS', payload: [] });
          return;
        }
      }
      
      console.error('Error loading cart:', err);
      dispatch({ type: 'LOADING_ERROR', payload: 'Failed to load cart' });
    }
  }, []);

  useEffect(() => {
    if (location.pathname.startsWith('/admin')) {
      return;
    }

    if (state.isInitialized) {
      return;
    }

    const token = localStorage.getItem('token');
    if (token) {
      dispatch({ type: 'SET_INITIALIZED' });
      refreshCart().catch((err) => {
        console.error('Initial cart load failed:', err);
      });
    }
  }, [location.pathname, state.isInitialized, refreshCart]);

  const addItem = useCallback(async (productId: string, quantity: number = 1) => {
    const token = localStorage.getItem('token');
    if (!token) {
      toast.error('Please login to add items to cart');
      dispatch({ type: 'LOADING_ERROR', payload: 'Please login to add items to cart' });
      return;
    }

    dispatch({ type: 'LOADING_START' });
    try {
      const response = await cartAPI.addItem(productId, quantity);
      dispatch({ type: 'LOADING_SUCCESS', payload: response.items });
      
      const addedItem = response.items.find((item: { productId: string; productName: string }) => item.productId === productId);
      
      if (addedItem) {
        const quantityText = quantity > 1 ? `${quantity} x ` : '';
        toast.success(`${quantityText}${addedItem.productName} added to cart`);
      } else {
        toast.success('Item added to cart');
      }
    } catch (err) {
      dispatch({ type: 'LOADING_ERROR', payload: 'Failed to add item to cart' });
      toast.error('Failed to add item to cart');
      console.error('Error adding item to cart:', err);
    }
  }, []);

  const removeItem = useCallback(async (cartItemId: string) => {
    const itemToRemove = state.items.find(item => item.id === cartItemId);
    
    dispatch({ type: 'LOADING_START' });
    try {
      const response = await cartAPI.removeItem(cartItemId);
      dispatch({ type: 'LOADING_SUCCESS', payload: response.items });
      
      if (itemToRemove) {
        toast.success(`${itemToRemove.productName} removed from cart`);
      } else {
        toast.success('Item removed from cart');
      }
    } catch (err) {
      dispatch({ type: 'LOADING_ERROR', payload: 'Failed to remove item from cart' });
      toast.error('Failed to remove item from cart');
      console.error('Error removing item from cart:', err);
    }
  }, [state.items]);

  const updateQuantity = useCallback(async (cartItemId: string, quantity: number) => {
    if (quantity <= 0) {
      await removeItem(cartItemId);
      return;
    }

    dispatch({ type: 'LOADING_START' });
    try {
      const response = await cartAPI.updateItemQuantity(cartItemId, quantity);
      dispatch({ type: 'LOADING_SUCCESS', payload: response.items });
    } catch (err) {
      dispatch({ type: 'LOADING_ERROR', payload: 'Failed to update item quantity' });
      toast.error('Failed to update item quantity');
      console.error('Error updating item quantity:', err);
    }
  }, [removeItem]);

  const clearCart = useCallback(async () => {
    dispatch({ type: 'LOADING_START' });
    try {
      await cartAPI.clearCart();
      dispatch({ type: 'CLEAR_CART' });
      toast.success('Cart cleared');
    } catch (err) {
      dispatch({ type: 'LOADING_ERROR', payload: 'Failed to clear cart' });
      toast.error('Failed to clear cart');
      console.error('Error clearing cart:', err);
    }
  }, []);

  const totalItems = useMemo(
    () => state.items.reduce((sum, item) => sum + item.quantity, 0),
    [state.items]
  );

  const totalPrice = useMemo(
    () => state.items.reduce((sum, item) => sum + (item.price * item.quantity), 0),
    [state.items]
  );

  const value: CartContextType = useMemo(
    () => ({
      items: state.items,
      addItem,
      removeItem,
      updateQuantity,
      clearCart,
      totalItems,
      totalPrice,
      isLoading: state.isLoading,
      error: state.error,
      refreshCart,
    }),
    [state.items, state.isLoading, state.error, addItem, removeItem, updateQuantity, clearCart, totalItems, totalPrice, refreshCart]
  );

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
};
