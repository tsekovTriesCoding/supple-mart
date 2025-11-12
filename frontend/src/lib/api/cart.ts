import { api } from './index';

export const cartAPI = {
  getCart: async () => {
    const response = await api.get('cart');
    return response.data;
  },

  addItem: async (productId: string, quantity: number = 1) => {
    const response = await api.post('cart/items', {
      productId,
      quantity
    });
    return response.data;
  },

  updateItemQuantity: async (cartItemId: string, quantity: number) => {
    const response = await api.put(`cart/items/${cartItemId}`, {
      quantity
    });
    return response.data;
  },

  removeItem: async (cartItemId: string) => {
    const response = await api.delete(`cart/items/${cartItemId}`);
    return response.data;
  },

  clearCart: async () => {
    const response = await api.delete('cart');
    return response.data;
  }
};

export default cartAPI;