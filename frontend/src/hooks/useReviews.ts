import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useMemo } from 'react';

import { reviewsAPI } from '../lib/api/reviews';
import type { ReviewResponseDTO, CreateReviewRequest, UpdateReviewRequest } from '../types/review';

const REVIEWS_QUERY_KEY = 'user-reviews';

export const useReviews = () => {
  const queryClient = useQueryClient();

  const { data: reviews = [], isLoading, error } = useQuery({
    queryKey: [REVIEWS_QUERY_KEY],
    queryFn: reviewsAPI.getUserReviews,
    staleTime: 2 * 60 * 1000,
    gcTime: 5 * 60 * 1000,
  });

  const createMutation = useMutation({
    mutationFn: (reviewData: CreateReviewRequest) => reviewsAPI.createReview(reviewData),
    onSuccess: (newReview) => {
      queryClient.setQueryData<ReviewResponseDTO[]>([REVIEWS_QUERY_KEY], (old = []) => [
        newReview,
        ...old,
      ]);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ reviewId, reviewData }: { reviewId: string; reviewData: UpdateReviewRequest }) =>
      reviewsAPI.updateReview(reviewId, reviewData),
    onSuccess: (updatedReview) => {
      queryClient.setQueryData<ReviewResponseDTO[]>([REVIEWS_QUERY_KEY], (old = []) =>
        old.map((review) => (review.id === updatedReview.id ? updatedReview : review))
      );
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (reviewId: string) => reviewsAPI.deleteReview(reviewId),
    onSuccess: (_, reviewId) => {
      queryClient.setQueryData<ReviewResponseDTO[]>([REVIEWS_QUERY_KEY], (old = []) =>
        old.filter((review) => review.id !== reviewId)
      );
    },
  });

  const reviewStats = useMemo(() => {
    if (reviews.length === 0) {
      return {
        totalReviews: 0,
        averageRating: 0,
        ratingDistribution: [0, 0, 0, 0, 0],
      };
    }

    const totalReviews = reviews.length;
    const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0);
    const averageRating = totalRating / totalReviews;

    const ratingDistribution = [0, 0, 0, 0, 0];
    reviews.forEach((review) => {
      ratingDistribution[review.rating - 1]++;
    });

    return {
      totalReviews,
      averageRating: Math.round(averageRating * 10) / 10,
      ratingDistribution,
    };
  }, [reviews]);

  const getReviewStats = () => reviewStats;

  return {
    reviews,
    loading: isLoading,
    error: error ? 'Failed to load reviews' : null,
    createReview: createMutation.mutateAsync,
    updateReview: (reviewId: string, reviewData: UpdateReviewRequest) =>
      updateMutation.mutateAsync({ reviewId, reviewData }),
    deleteReview: deleteMutation.mutateAsync,
    refreshReviews: () => queryClient.invalidateQueries({ queryKey: [REVIEWS_QUERY_KEY] }),
    getReviewStats,
  };
};
