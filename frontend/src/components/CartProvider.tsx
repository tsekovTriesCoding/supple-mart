import { useState, useEffect } from 'react';
import type { ReactNode } from 'react';

import { CartContext, type CartContextType, type CartItem } from '../hooks';
import { cartAPI } from '../lib/api';

export const CartProvider = ({ children }: { children: ReactNode }) => {
  const [items, setItems] = useState<CartItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      // Don't let cart errors crash the app
      refreshCart().catch((err) => {
        console.error('Initial cart load failed:', err);
        // Continue with empty cart rather than crashing
      });
    }
  }, []);

  const refreshCart = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      setItems([]);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const cart = await cartAPI.getCart();
      setItems(cart.items || []);
    } catch (err) {
      // Handle different types of errors more gracefully
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { status?: number } };
        if (axiosError.response?.status === 401 || axiosError.response?.status === 403) {
          // Token is invalid or access forbidden, clear it
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          setItems([]);
          setError('Session expired. Please login again.');
          console.log('Authentication failed, clearing tokens');
          return;
        } else if (axiosError.response?.status === 404) {
          // Cart not found, create empty cart
          setItems([]);
          return;
        }
      }
      
      // For network errors or other issues, just log and continue
      console.error('Error loading cart:', err);
      setError('Failed to load cart');
      setItems([]);
    } finally {
      setIsLoading(false);
    }
  };

  const addItem = async (productId: string, quantity: number = 1) => {
    const token = localStorage.getItem('token');
    if (!token) {
      setError('Please login to add items to cart');
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      await cartAPI.addItem(productId, quantity);
      await refreshCart();
    } catch (err) {
      setError('Failed to add item to cart');
      console.error('Error adding item to cart:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const removeItem = async (cartItemId: string) => {
    setIsLoading(true);
    setError(null);
    try {
      await cartAPI.removeItem(cartItemId);
      await refreshCart();
    } catch (err) {
      setError('Failed to remove item from cart');
      console.error('Error removing item from cart:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const updateQuantity = async (cartItemId: string, quantity: number) => {
    if (quantity <= 0) {
      await removeItem(cartItemId);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      await cartAPI.updateItemQuantity(cartItemId, quantity);
      await refreshCart();
    } catch (err) {
      setError('Failed to update item quantity');
      console.error('Error updating item quantity:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const clearCart = async () => {
    setIsLoading(true);
    setError(null);
    try {
      await cartAPI.clearCart();
      setItems([]);
    } catch (err) {
      setError('Failed to clear cart');
      console.error('Error clearing cart:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const value: CartContextType = {
    items,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    totalItems,
    totalPrice,
    isLoading,
    error,
    refreshCart,
  };

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
};
