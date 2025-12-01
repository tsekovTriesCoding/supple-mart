import { useState, useMemo, useCallback } from 'react';
import { User, Calendar } from 'lucide-react';

import { StarRating } from '../StarRating';
import type { Review } from '../../types/review';

interface ProductReviewsProps {
  reviews: Review[];
  averageRating: number;
  totalReviews: number;
}

const ProductReviews = ({ reviews, averageRating, totalReviews }: ProductReviewsProps) => {
  const [sortBy, setSortBy] = useState<'createdAt' | 'rating'>('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

  const sortOptions = [
    { value: 'createdAt-desc', label: 'Newest First' },
    { value: 'createdAt-asc', label: 'Oldest First' },
    { value: 'rating-desc', label: 'Highest Rating' },
    { value: 'rating-asc', label: 'Lowest Rating' },
  ];

  const handleSortChange = useCallback((sortValue: string) => {
    const [field, order] = sortValue.split('-');
    setSortBy(field as 'createdAt' | 'rating');
    setSortOrder(order as 'asc' | 'desc');
  }, []);

  const sortedReviews = useMemo(() => {
    return [...reviews].sort((a, b) => {
      if (sortBy === 'createdAt') {
        const dateA = new Date(a.createdAt).getTime();
        const dateB = new Date(b.createdAt).getTime();
        return sortOrder === 'desc' ? dateB - dateA : dateA - dateB;
      } else if (sortBy === 'rating') {
        return sortOrder === 'desc' ? b.rating - a.rating : a.rating - b.rating;
      }
      return 0;
    });
  }, [reviews, sortBy, sortOrder]);

  const formatDate = useCallback((dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  }, []);

  const ratingDistribution = useMemo(() => {
    const distribution = [0, 0, 0, 0, 0];
    reviews.forEach((review: Review) => {
      if (review.rating >= 1 && review.rating <= 5) {
        distribution[review.rating - 1]++;
      }
    });
    return distribution.reverse();
  }, [reviews]);

  return (
    <div className="space-y-6">
      <div className="bg-gray-800 p-6 rounded-lg">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="text-center">
            <div className="text-4xl font-bold text-white mb-2">{averageRating}</div>
            <StarRating rating={averageRating} size="xl" />
            <p className="text-gray-400 mt-2">Based on {totalReviews === 1 ? '1 review' : `${totalReviews} reviews`}</p>
          </div>

          <div className="space-y-2">
            {ratingDistribution.map((count, index) => {
              const starCount = 5 - index;
              const percentage = totalReviews > 0 ? (count / totalReviews) * 100 : 0;

              return (
                <div key={starCount} className="flex items-center space-x-3 text-sm">
                  <span className="text-gray-300 w-8">{starCount}★</span>
                  <div className="flex-1 bg-gray-700 rounded-full h-2">
                    <div
                      className="bg-yellow-400 h-2 rounded-full"
                      style={{ width: `${percentage}%` }}
                    ></div>
                  </div>
                  <span className="text-gray-400 w-8">{count}</span>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {reviews.length > 0 && (
        <div className="flex justify-between items-center">
          <h3 className="text-lg font-semibold text-white">Customer Reviews</h3>
          <select
            value={`${sortBy}-${sortOrder}`}
            onChange={(e) => handleSortChange(e.target.value)}
            className="input text-sm"
          >
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      )}

      {reviews.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-400 mb-4">No reviews yet</p>
          <p className="text-gray-500">Be the first to review this product!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {sortedReviews.map((review: Review) => (
            <div key={review.id} className="bg-gray-800 p-6 rounded-lg">

              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center space-x-3">
                  <div className="flex items-center justify-center w-10 h-10 bg-gray-700 rounded-full">
                    <User className="w-5 h-5 text-gray-400" />
                  </div>
                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="font-medium text-white">{review.userName}</span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-gray-400">
                      <Calendar className="w-4 h-4" />
                      <span>{formatDate(review.createdAt)}</span>
                    </div>
                  </div>
                </div>

                <StarRating rating={review.rating} size="lg" />
              </div>

              <div className="mb-4">
                <p className="text-gray-300 leading-relaxed">{review.comment}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ProductReviews;
