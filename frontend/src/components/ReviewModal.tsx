import { useState, useEffect } from 'react';
import { Star, X, Send, AlertCircle } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';

import { reviewsAPI } from '../lib/api/reviews';
import type { CreateReviewRequest, UpdateReviewRequest } from '../types/review';

interface ReviewModalProps {
  productId: string;
  productName: string;
  productImage: string;
  isOpen: boolean;
  onClose: () => void;
  onReviewSubmitted?: () => void;
  editMode?: boolean;
  reviewId?: string;
  initialRating?: number;
  initialComment?: string;
}

const ReviewModal = ({ 
  productId, 
  productName, 
  productImage, 
  isOpen, 
  onClose, 
  onReviewSubmitted,
  editMode = false,
  reviewId,
  initialRating = 0,
  initialComment = ''
}: ReviewModalProps) => {
  const [rating, setRating] = useState(0);
  const [hoveredRating, setHoveredRating] = useState(0);
  const [comment, setComment] = useState('');
  const [validationError, setValidationError] = useState<string | null>(null);

  const createReviewMutation = useMutation({
    mutationFn: (data: CreateReviewRequest) => reviewsAPI.createReview(data),
    onSuccess: () => {
      setRating(0);
      setComment('');
      setValidationError(null);
      toast.success('Review submitted successfully');
      onReviewSubmitted?.();
      onClose();
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to submit review'
        : 'Failed to submit review';
      toast.error(message);
    }
  });

  const updateReviewMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateReviewRequest }) => 
      reviewsAPI.updateReview(id, data),
    onSuccess: () => {
      setRating(0);
      setComment('');
      setValidationError(null);
      toast.success('Review updated successfully');
      onReviewSubmitted?.();
      onClose();
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to update review'
        : 'Failed to update review';
      toast.error(message);
    }
  });

  const isSubmitting = createReviewMutation.isPending || updateReviewMutation.isPending;

  useEffect(() => {
    if (isOpen) {
      if (editMode) {
        setRating(initialRating);
        setComment(initialComment);
      } else {
        setRating(0);
        setComment('');
      }
      setValidationError(null);
    }
  }, [isOpen, editMode, initialRating, initialComment]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (rating === 0) {
      setValidationError('Please select a rating');
      return;
    }

    if (comment.trim().length < 10) {
      setValidationError('Please write at least 10 characters in your review');
      return;
    }

    if (comment.trim().length > 500) {
      setValidationError('Review must be 500 characters or less');
      return;
    }

    setValidationError(null);

    if (editMode && reviewId) {
      updateReviewMutation.mutate({
        id: reviewId,
        data: {
          rating,
          comment: comment.trim()
        }
      });
    } else {
      createReviewMutation.mutate({
        productId,
        rating,
        comment: comment.trim()
      });
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setRating(0);
      setComment('');
      setValidationError(null);
      onClose();
    }
  };

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget && !isSubmitting) {
      handleClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div 
      className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4"
      onClick={handleBackdropClick}
    >
      <div className="bg-gray-900 rounded-2xl max-w-md w-full max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-gray-900/95 backdrop-blur-sm border-b border-gray-700 p-6 flex items-center justify-between">
          <h2 className="text-xl font-bold text-white">{editMode ? 'Edit Review' : 'Write a Review'}</h2>
          <button
            onClick={handleClose}
            disabled={isSubmitting}
            className="p-2 hover:bg-gray-800 rounded-lg transition-colors disabled:opacity-50 cursor-pointer"
          >
            <X className="w-5 h-5 text-gray-400" />
          </button>
        </div>

        <div className="p-6">
          <div className="flex items-center space-x-4 mb-6 p-4 bg-gray-800 rounded-lg">
            <img
              src={productImage}
              alt={productName}
              className="w-16 h-16 object-cover rounded-lg"
              onError={(e) => {
                const target = e.target as HTMLImageElement;
                target.src = '/placeholder-product.jpg';
              }}
            />
            <div>
              <h3 className="font-semibold text-white">{productName}</h3>
              <p className="text-gray-400 text-sm">Leave your honest review</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-white font-medium mb-3">
                Rating <span className="text-red-400">*</span>
              </label>
              <div className="flex items-center space-x-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    onClick={() => setRating(star)}
                    onMouseEnter={() => setHoveredRating(star)}
                    onMouseLeave={() => setHoveredRating(0)}
                    className="p-1 transition-transform hover:scale-110 cursor-pointer"
                  >
                    <Star
                      className={`w-8 h-8 ${
                        star <= (hoveredRating || rating)
                          ? 'text-yellow-400 fill-current'
                          : 'text-gray-600'
                      }`}
                    />
                  </button>
                ))}
              </div>
              {rating > 0 && (
                <p className="text-sm text-gray-400 mt-2">
                  {rating === 1 && "Poor"}
                  {rating === 2 && "Fair"}
                  {rating === 3 && "Good"}
                  {rating === 4 && "Very Good"}
                  {rating === 5 && "Excellent"}
                </p>
              )}
            </div>

            <div>
              <label className="block text-white font-medium mb-3">
                Your Review <span className="text-red-400">*</span>
              </label>
              <textarea
                value={comment}
                onChange={(e) => {
                  const newValue = e.target.value;
                  if (newValue.length <= 500) {
                    setComment(newValue);
                  }
                }}
                placeholder="Share your experience with this product..."
                rows={4}
                maxLength={500}
                className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500 resize-none"
                disabled={isSubmitting}
              />
              <div className="flex justify-between items-center mt-2">
                <span className="text-sm text-gray-400">
                  Minimum 10 characters
                </span>
                <span className={`text-sm ${
                  comment.length < 10 
                    ? 'text-gray-400' 
                    : comment.length >= 450 
                      ? 'text-yellow-400' 
                      : comment.length >= 500
                        ? 'text-red-400'
                        : 'text-green-400'
                }`}>
                  {comment.length}/500
                </span>
              </div>
            </div>

            {validationError && (
              <div className="flex items-center space-x-2 p-3 bg-red-900/20 border border-red-500/30 rounded-lg">
                <AlertCircle className="w-5 h-5 text-red-400" />
                <span className="text-red-400 text-sm">{validationError}</span>
              </div>
            )}

            <div className="flex space-x-3 pt-4">
              <button
                type="button"
                onClick={handleClose}
                disabled={isSubmitting}
                className="flex-1 px-4 py-3 border border-gray-600 text-gray-300 rounded-lg hover:bg-gray-800 transition-colors disabled:opacity-50 cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSubmitting || rating === 0 || comment.trim().length < 10 || comment.trim().length > 500}
                className="flex-1 px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed inline-flex items-center justify-center space-x-2"
              >
                {isSubmitting ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    <span>{editMode ? 'Updating...' : 'Submitting...'}</span>
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4" />
                    <span>{editMode ? 'Update Review' : 'Submit Review'}</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ReviewModal;
