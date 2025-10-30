import { useState, useEffect } from 'react';

import { reviewsAPI } from '../lib/api/reviews';
import type { Review, CreateReviewRequest, UpdateReviewRequest } from '../lib/api/reviews';

export const useReviews = () => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReviews = async () => {
    try {
      setLoading(true);
      setError(null);
      const userReviews = await reviewsAPI.getUserReviews();
      setReviews(userReviews);
    } catch (err) {
      console.error('Error fetching reviews:', err);
      setError('Failed to load reviews');
    } finally {
      setLoading(false);
    }
  };

  const createReview = async (reviewData: CreateReviewRequest) => {
    try {
      setError(null);
      const newReview = await reviewsAPI.createReview(reviewData);
      setReviews(prev => [newReview, ...prev]);
      return newReview;
    } catch (err) {
      console.error('Error creating review:', err);
      setError('Failed to create review');
      throw err;
    }
  };

  const updateReview = async (reviewId: string, reviewData: UpdateReviewRequest) => {
    try {
      setError(null);
      const updatedReview = await reviewsAPI.updateReview(reviewId, reviewData);
      setReviews(prev => 
        prev.map(review => 
          review.id === reviewId ? updatedReview : review
        )
      );
      return updatedReview;
    } catch (err) {
      console.error('Error updating review:', err);
      setError('Failed to update review');
      throw err;
    }
  };

  const deleteReview = async (reviewId: string) => {
    try {
      setError(null);
      await reviewsAPI.deleteReview(reviewId);
      setReviews(prev => prev.filter(review => review.id !== reviewId));
    } catch (err) {
      console.error('Error deleting review:', err);
      setError('Failed to delete review');
      throw err;
    }
  };

  const refreshReviews = () => {
    fetchReviews();
  };

  const getReviewStats = () => {
    if (reviews.length === 0) {
      return {
        totalReviews: 0,
        averageRating: 0,
        ratingDistribution: [0, 0, 0, 0, 0]
      };
    }

    const totalReviews = reviews.length;
    const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0);
    const averageRating = totalRating / totalReviews;
    
    const ratingDistribution = [0, 0, 0, 0, 0];
    reviews.forEach(review => {
      ratingDistribution[review.rating - 1]++;
    });

    return {
      totalReviews,
      averageRating: Math.round(averageRating * 10) / 10,
      ratingDistribution
    };
  };

  useEffect(() => {
    fetchReviews();
  }, []);

  return {
    reviews,
    loading,
    error,
    createReview,
    updateReview,
    deleteReview,
    refreshReviews,
    getReviewStats
  };
};
