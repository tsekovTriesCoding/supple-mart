import { api } from './index';

export interface Review {
  id: string;
  user: {
    id: string;
    name: string;
    email: string;
  };
  product: {
    id: string;
    name: string;
    imageUrl: string;
    price: number;
  };
  rating: number;
  comment: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReviewRequest {
  productId: string;
  rating: number;
  comment: string;
}

export interface UpdateReviewRequest {
  rating: number;
  comment: string;
}

export const reviewsAPI = {
  getUserReviews: async (): Promise<Review[]> => {
    const response = await api.get('/reviews');
    return response.data;
  },

  getReviewById: async (reviewId: string): Promise<Review> => {
    const response = await api.get(`/reviews/${reviewId}`);
    return response.data;
  },

  createReview: async (review: CreateReviewRequest): Promise<Review> => {
    const response = await api.post('/reviews', review);
    return response.data;
  },

  updateReview: async (reviewId: string, review: UpdateReviewRequest): Promise<Review> => {
    const response = await api.put(`/reviews/${reviewId}`, review);
    return response.data;
  },

  deleteReview: async (reviewId: string): Promise<void> => {
    await api.delete(`/reviews/${reviewId}`);
  },

  getProductReviews: async (productId: string): Promise<Review[]> => {
    const response = await api.get(`/reviews/product/${productId}`);
    return response.data;
  }
};

