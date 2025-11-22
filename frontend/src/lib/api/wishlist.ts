import { api } from './index';
import type { WishlistResponse, AddToWishlistRequest } from '../../types/wishlist';

class WishlistAPI {
  async addToWishlist(productId: string): Promise<{ message: string }> {
    const request: AddToWishlistRequest = { productId };
    const { data } = await api.post('wishlist', request);
    return data;
  }

  async removeFromWishlist(productId: string): Promise<{ message: string }> {
    const { data } = await api.delete(`wishlist/${productId}`);
    return data;
  }

  async getUserWishlist(params?: {
    page?: number;
    size?: number;
  }): Promise<WishlistResponse> {
    const queryParams = new URLSearchParams();
    
    if (params?.page !== undefined) queryParams.append('page', (params.page - 1).toString());
    if (params?.size) queryParams.append('size', params.size.toString());

    const queryString = queryParams.toString();
    const endpoint = queryString ? `wishlist?${queryString}` : 'wishlist';
    
    const { data } = await api.get(endpoint);
    
    return {
      content: data.content || [],
      currentPage: (data.currentPage || 0) + 1,
      pageSize: data.pageSize || 20,
      totalPages: data.totalPages || 0,
      totalElements: data.totalElements || 0
    };
  }

  async checkIfInWishlist(productId: string): Promise<boolean> {
    const { data } = await api.get(`wishlist/check/${productId}`);
    return data.isInWishlist;
  }

  async getWishlistCount(): Promise<number> {
    const { data } = await api.get('wishlist/count');
    return data.count;
  }
}

export const wishlistAPI = new WishlistAPI();
