import { Star } from 'lucide-react';
import { useCallback } from 'react';

type StarSize = 'sm' | 'md' | 'lg' | 'xl';

interface StarRatingProps {
  rating: number;
  size?: StarSize;
  interactive?: boolean;
  onChange?: (rating: number) => void;
  showValue?: boolean;
  totalReviews?: number;
  className?: string;
}

const sizeClasses: Record<StarSize, string> = {
  sm: 'w-3 h-3',
  md: 'w-4 h-4',
  lg: 'w-5 h-5',
  xl: 'w-6 h-6',
};

export const StarRating = ({
  rating,
  size = 'md',
  interactive = false,
  onChange,
  showValue = false,
  totalReviews,
  className = '',
}: StarRatingProps) => {
  const starSize = sizeClasses[size];

  const handleStarClick = useCallback((star: number) => {
    if (interactive && onChange) {
      onChange(star);
    }
  }, [interactive, onChange]);

  const handleKeyDown = useCallback((star: number, e: React.KeyboardEvent) => {
    if (interactive && onChange && (e.key === 'Enter' || e.key === ' ')) {
      e.preventDefault();
      onChange(star);
    }
  }, [interactive, onChange]);

  return (
    <div className={`flex items-center ${className}`}>
      <div className="flex">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            onClick={() => handleStarClick(star)}
            onKeyDown={(e) => handleKeyDown(star, e)}
            disabled={!interactive}
            className={`${interactive ? 'cursor-pointer hover:scale-110 transition-transform' : 'cursor-default'} focus:outline-none`}
            tabIndex={interactive ? 0 : -1}
            aria-label={interactive ? `Rate ${star} star${star > 1 ? 's' : ''}` : undefined}
          >
            <Star
              className={`${starSize} ${
                star <= rating
                  ? 'fill-yellow-400 text-yellow-400'
                  : 'text-gray-600'
              }`}
            />
          </button>
        ))}
      </div>
      {showValue && (
        <span className="ml-2 text-sm text-gray-300">{rating.toFixed(1)}</span>
      )}
      {totalReviews !== undefined && (
        <span className="ml-1 text-xs text-gray-500">
          ({totalReviews} {totalReviews === 1 ? 'review' : 'reviews'})
        </span>
      )}
    </div>
  );
};
