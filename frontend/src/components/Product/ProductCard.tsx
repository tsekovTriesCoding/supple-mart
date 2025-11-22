import { Heart, ShoppingBag, Star } from 'lucide-react';

import type { Product } from '../../hooks/useProducts';
import { formatCategoryForDisplay } from '../../utils/categoryUtils';

interface ProductCardProps {
  product: Product;
  onProductClick: (productId: string) => void;
  onAddToCart: (product: Product) => void;
  onToggleWishlist: (product: Product) => void;
  isLoading?: boolean;
  isAddingToCart?: boolean;
  isInWishlist?: boolean;
  animationDelay?: number;
  variant?: 'card' | 'list';
}

export const ProductCard = ({
  product,
  onProductClick,
  onAddToCart,
  onToggleWishlist,
  isLoading = false,
  isAddingToCart = false,
  isInWishlist = false,
  animationDelay = 0,
  variant = 'card',
}: ProductCardProps) => {
  const isListView = variant === 'list';

  return (
    <div
      className={`card-hover ${isListView ? 'p-6' : 'p-4'} animate-slide-in cursor-pointer transition-all duration-200 ${
        isLoading ? 'opacity-75' : isListView ? 'hover:scale-[1.01]' : 'hover:scale-[1.02]'
      }`}
      style={{ animationDelay: `${animationDelay}s` }}
      onClick={() => onProductClick(product.id)}
    >
      {isLoading && (
        <div className="absolute inset-0 bg-blue-500/20 rounded-lg flex items-center justify-center z-10">
          <div className="bg-blue-500 text-white px-3 py-1 rounded-full text-sm font-medium flex items-center space-x-2">
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
            <span>Opening...</span>
          </div>
        </div>
      )}

      <div className={isListView ? 'flex flex-col md:flex-row gap-6 relative' : 'relative'}>
        <div className={`bg-gray-800 rounded-lg overflow-hidden relative ${
          isListView ? 'w-full md:w-48 h-48 shrink-0' : 'aspect-square mb-4'
        }`}>
          <img
            src={product.imageUrl}
            alt={product.name}
            className={`w-full h-full object-cover ${!isListView && 'hover:scale-105 transition-transform duration-300'}`}
          />
          {!product.inStock && (
            <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
              <span className="text-white font-semibold">Out of Stock</span>
            </div>
          )}
          {!isListView && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onToggleWishlist(product);
              }}
              className={`absolute top-2 right-2 p-2 rounded-full transition-colors ${
                isInWishlist ? 'bg-red-500/80 hover:bg-red-600/80' : 'bg-black/50 hover:bg-black/70'
              }`}
            >
              <Heart className={`w-4 h-4 text-white ${isInWishlist ? 'fill-current' : ''}`} />
            </button>
          )}
        </div>

        <div className={isListView ? 'flex-1' : 'space-y-2'}>
          <div className="flex justify-between items-start">
            <div className={isListView ? '' : 'flex-1'}>
              <span className="text-sm text-blue-400 font-medium">
                {formatCategoryForDisplay(product.category)}
              </span>
              <h3 className={`font-semibold text-white ${isListView ? 'text-xl mt-1' : 'text-lg mt-2'} line-clamp-2`}>
                {product.name}
              </h3>
              {product.brand && <p className="text-gray-400 mt-1">{product.brand}</p>}
            </div>
            {isListView ? (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onToggleWishlist(product);
                }}
                className={`p-2 rounded-full transition-colors ${
                  isInWishlist ? 'bg-red-500/20 hover:bg-red-500/30' : 'hover:bg-gray-700'
                }`}
              >
                <Heart className={`w-5 h-5 ${
                  isInWishlist ? 'text-red-400 fill-current' : 'text-gray-400'
                }`} />
              </button>
            ) : (
              <div className="flex items-center space-x-1">
                <Star className="w-4 h-4 text-yellow-400 fill-current" />
                <span className="text-sm text-gray-300">{product.averageRating}</span>
                <span className="text-xs text-gray-500">
                  ({product.totalReviews === 1 ? '1 review' : `${product.totalReviews} reviews`})
                </span>
              </div>
            )}
          </div>

          {isListView && (
            <>
              <div className="flex items-center space-x-1 mb-3">
                <Star className="w-4 h-4 text-yellow-400 fill-current" />
                <span className="text-sm text-gray-300">{product.averageRating}</span>
                <span className="text-xs text-gray-500">({product.totalReviews} reviews)</span>
              </div>
              <p className="text-gray-400 mb-4 line-clamp-2">{product.description}</p>
            </>
          )}

          <div className={`flex items-center ${isListView ? 'justify-between' : 'space-x-2'}`}>
            <div className="flex items-center space-x-2">
              <span className={`font-bold text-blue-400 ${isListView ? 'text-2xl' : 'text-xl'}`}>
                ${product.price}
              </span>
              {product.originalPrice && (
                <span className={`text-gray-500 line-through ${isListView ? 'text-lg' : 'text-sm'}`}>
                  ${product.originalPrice}
                </span>
              )}
            </div>
            {isListView && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onAddToCart(product);
                }}
                disabled={!product.inStock || isAddingToCart}
                className="btn-primary inline-flex items-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isAddingToCart ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    <span>Adding...</span>
                  </>
                ) : (
                  <>
                    <ShoppingBag className="w-4 h-4" />
                    <span>{product.inStock ? 'Add to Cart' : 'Out of Stock'}</span>
                  </>
                )}
              </button>
            )}
          </div>

          {!isListView && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onAddToCart(product);
              }}
              disabled={!product.inStock || isAddingToCart}
              className="btn-primary w-full inline-flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isAddingToCart ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  <span>Adding...</span>
                </>
              ) : (
                <>
                  <ShoppingBag className="w-4 h-4" />
                  <span>{product.inStock ? 'Add to Cart' : 'Out of Stock'}</span>
                </>
              )}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
