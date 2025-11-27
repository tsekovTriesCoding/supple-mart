import { Star, Edit3, Trash2, Calendar, Package } from 'lucide-react';
import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';

import { reviewsAPI } from '../lib/api/reviews';
import ReviewModal from '../components/ReviewModal';
import { LoadingSpinner } from '../components/LoadingSpinner';
import type { ReviewResponseDTO } from '../types/review';

const Reviews = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingReview, setEditingReview] = useState<ReviewResponseDTO | null>(null);

  const { data: reviews = [], isLoading: loading, error } = useQuery({
    queryKey: ['user-reviews'],
    queryFn: reviewsAPI.getUserReviews,
    staleTime: 2 * 60 * 1000,
    gcTime: 5 * 60 * 1000,
  });

  const deleteMutation = useMutation({
    mutationFn: (reviewId: string) => reviewsAPI.deleteReview(reviewId),
    onSuccess: (_, reviewId) => {
      queryClient.setQueryData<ReviewResponseDTO[]>(['user-reviews'], (old = []) =>
        old.filter((review) => review.id !== reviewId)
      );
      toast.success('Review deleted successfully');
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to delete review'
        : 'Failed to delete review';
      toast.error(message);
    },
  });

  const reviewStats = useMemo(() => {
    if (reviews.length === 0) {
      return {
        totalReviews: 0,
        averageRating: 0,
        ratingDistribution: { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 },
      };
    }

    const ratingDistribution = reviews.reduce(
      (acc, review) => {
        acc[review.rating as keyof typeof acc]++;
        return acc;
      },
      { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
    );

    const averageRating =
      reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length;

    return {
      totalReviews: reviews.length,
      averageRating: Number(averageRating.toFixed(1)),
      ratingDistribution,
    };
  }, [reviews]);

  const getReviewStats = () => reviewStats;
  const deleteReview = (reviewId: string) => deleteMutation.mutateAsync(reviewId);
  const refreshReviews = () => queryClient.invalidateQueries({ queryKey: ['user-reviews'] });

  const renderStars = (rating: number) => {
    return (
      <div className="flex items-center space-x-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={`w-4 h-4 ${star <= rating ? 'text-yellow-400 fill-current' : 'text-gray-400'}`}
          />
        ))}
      </div>
    );
  };

  const handleUpdateReview = (reviewId: string) => {
    const review = reviews.find(r => r.id === reviewId);
    if (review) {
      setEditingReview(review);
      setIsEditModalOpen(true);
    }
  };

  const handleViewProduct = (productId: string) => {
    navigate(`/products/${productId}`);
  };

  const handleDeleteReview = async (reviewId: string) => {
    if (window.confirm('Are you sure you want to delete this review?')) {
      await deleteReview(reviewId);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (!localStorage.getItem('token')) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-white mb-4">Please Sign In</h1>
          <p className="text-gray-400">You need to be logged in to view your reviews.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return <LoadingSpinner size="lg" message="Loading your reviews..." fullScreen />;
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-white mb-4">Error Loading Reviews</h1>
          <p className="text-red-400">{error?.message || 'Failed to load reviews'}</p>
        </div>
      </div>
    );
  }

  const stats = getReviewStats();

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <div className="flex justify-between items-center mb-8">
            <h1 className="text-3xl font-bold text-white">My Reviews</h1>
            <div className="text-gray-400">
              {stats.totalReviews} review{stats.totalReviews !== 1 ? 's' : ''}
            </div>
          </div>

          {reviews.length === 0 ? (
            <div className="card p-8 text-center">
              <Star className="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-white mb-2">No Reviews Yet</h2>
              <p className="text-gray-400 mb-6">You haven't written any reviews yet. Purchase and review products to share your experience!</p>
              <button className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors cursor-pointer">
                Start Shopping
              </button>
            </div>
          ) : (
            <div className="space-y-6">
              {reviews.map((review) => (
                <div key={review.id} className="card p-6">
                  <div className="flex flex-col md:flex-row gap-4">
                    <div className="shrink-0">
                      <img
                        src={review.product.imageUrl}
                        alt={review.product.name}
                        className="w-20 h-20 object-cover rounded-lg"
                        onError={(e) => {
                          const target = e.target as HTMLImageElement;
                          target.style.display = 'none';
                          target.nextElementSibling?.classList.remove('hidden');
                        }}
                      />
                      <div className="w-20 h-20 bg-gray-700 rounded-lg items-center justify-center hidden">
                        <Package className="w-8 h-8 text-gray-400" />
                      </div>
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex flex-col md:flex-row md:items-start md:justify-between mb-3">
                        <div>
                          <h3 className="text-lg font-semibold text-white mb-1">{review.product.name}</h3>
                          <p className="text-gray-400 text-sm mb-2">${review.product.price.toFixed(2)}</p>
                          <div className="flex items-center space-x-3 mb-2">
                            {renderStars(review.rating)}
                            <span className="text-xs bg-green-900 text-green-300 px-2 py-1 rounded-full">
                              Verified Purchase
                            </span>
                          </div>
                        </div>

                        <div className="flex items-center space-x-2">
                          <button
                            onClick={() => handleUpdateReview(review.id)}
                            className="p-2 text-gray-400 hover:text-blue-400 transition-colors"
                            title="Edit Review"
                          >
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDeleteReview(review.id)}
                            className="p-2 text-gray-400 hover:text-red-400 transition-colors"
                            title="Delete Review"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>

                      <p className="text-gray-300 mb-3 leading-relaxed wrap-break-word">{review.comment}</p>

                      <div className="flex justify-between items-center text-sm text-gray-400">
                        <div className="flex items-center">
                          <Calendar className="w-4 h-4 mr-1" />
                          <span>Reviewed on {formatDate(review.createdAt)}</span>
                          {review.updatedAt !== review.createdAt && (
                            <span className="ml-2">(Updated {formatDate(review.updatedAt)})</span>
                          )}
                        </div>
                        <button 
                          onClick={() => handleViewProduct(review.product.id)}
                          className="text-blue-400 hover:text-blue-300 transition-colors"
                        >
                          View Product
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}

              <div className="card p-6 mt-8">
                <h3 className="text-lg font-semibold text-white mb-4">Review Summary</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-blue-400">
                      {stats.averageRating}
                    </div>
                    <div className="text-gray-400 text-sm">Average Rating</div>
                  </div>

                  <div className="text-center">
                    <div className="text-2xl font-bold text-green-400">
                      {stats.totalReviews}
                    </div>
                    <div className="text-gray-400 text-sm">Total Reviews</div>
                  </div>

                  <div className="text-center">
                    <div className="text-2xl font-bold text-yellow-400">
                      {stats.ratingDistribution[4]}
                    </div>
                    <div className="text-gray-400 text-sm">5-Star Reviews</div>
                  </div>

                  <div className="text-center">
                    <div className="text-2xl font-bold text-white">
                      {Object.values(stats.ratingDistribution).reduce((sum, count) => sum + count, 0)}
                    </div>
                    <div className="text-gray-400 text-sm">All Reviews</div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {editingReview && (
        <ReviewModal
          productId={editingReview.product.id}
          productName={editingReview.product.name}
          productImage={editingReview.product.imageUrl}
          isOpen={isEditModalOpen}
          onClose={() => {
            setIsEditModalOpen(false);
            setEditingReview(null);
          }}
          onReviewSubmitted={() => {
            refreshReviews();
          }}
          editMode={true}
          reviewId={editingReview.id}
          initialRating={editingReview.rating}
          initialComment={editingReview.comment}
        />
      )}
    </div>
  );
};

export default Reviews;
