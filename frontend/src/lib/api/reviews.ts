import { api } from './index';
import type { 
  Review,
  ReviewResponseDTO,
  CreateReviewRequest, 
  UpdateReviewRequest 
} from '../../types/review';

export const reviewsAPI = {
  getUserReviews: async (): Promise<ReviewResponseDTO[]> => {
    const response = await api.get('reviews');
    return response.data;
  },

  getReviewById: async (reviewId: string): Promise<Review> => {
    const response = await api.get(`reviews/${reviewId}`);
    return response.data;
  },

  createReview: async (review: CreateReviewRequest): Promise<ReviewResponseDTO> => {
    const response = await api.post('reviews', review);
    return response.data;
  },

  updateReview: async (reviewId: string, review: UpdateReviewRequest): Promise<ReviewResponseDTO> => {
    const response = await api.put(`reviews/${reviewId}`, review);
    return response.data;
  },

  deleteReview: async (reviewId: string): Promise<void> => {
    await api.delete(`reviews/${reviewId}`);
  },

  getProductReviews: async (productId: string): Promise<Review[]> => {
    const response = await api.get(`reviews/product/${productId}`);
    return response.data;
  }
};

