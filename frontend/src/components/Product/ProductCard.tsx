import { Heart, ShoppingBag, Star } from 'lucide-react';

import type { Product } from '../../hooks/useProducts';
import { formatCategoryForDisplay } from '../../utils/categoryUtils';

interface ProductCardProps {
  product: Product;
  onProductClick: (productId: number) => void;
  onAddToCart: (product: Product) => void;
  onToggleWishlist: (product: Product) => void;
  isLoading?: boolean;
  isAddingToCart?: boolean;
  animationDelay?: number;
}

export const ProductCard = ({
  product,
  onProductClick,
  onAddToCart,
  onToggleWishlist,
  isLoading = false,
  isAddingToCart = false,
  animationDelay = 0,
}: ProductCardProps) => {
  return (
    <div
      className={`card-hover p-4 animate-slide-in cursor-pointer transition-all duration-200 ${
        isLoading ? 'opacity-75 scale-95' : 'hover:scale-[1.02]'
      }`}
      style={{ animationDelay: `${animationDelay}s` }}
      onClick={() => onProductClick(product.id)}
    >
      <div className="relative">
        {isLoading && (
          <div className="absolute inset-0 bg-blue-500/20 rounded-lg flex items-center justify-center z-10">
            <div className="bg-blue-500 text-white px-3 py-1 rounded-full text-sm font-medium flex items-center space-x-2">
              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
              <span>Opening...</span>
            </div>
          </div>
        )}

        <div className="aspect-square bg-gray-800 rounded-lg mb-4 overflow-hidden relative">
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
          />
          {!product.inStock && (
            <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
              <span className="text-white font-semibold">Out of Stock</span>
            </div>
          )}
          <button
            onClick={(e) => {
              e.stopPropagation();
              onToggleWishlist(product);
            }}
            className="absolute top-2 right-2 p-2 rounded-full bg-black/50 hover:bg-black/70 transition-colors"
          >
            <Heart className="w-4 h-4 text-white" />
          </button>
        </div>

        <div className="space-y-2">
          <div className="flex justify-between items-start">
            <span className="text-sm text-blue-400 font-medium">
              {formatCategoryForDisplay(product.category)}
            </span>
            <div className="flex items-center space-x-1">
              <Star className="w-4 h-4 text-yellow-400 fill-current" />
              <span className="text-sm text-gray-300">{product.averageRating}</span>
              <span className="text-xs text-gray-500">
                ({product.totalReviews === 1 ? '1 review' : `${product.totalReviews} reviews`})
              </span>
            </div>
          </div>

          <h3 className="text-lg font-semibold text-white line-clamp-2">{product.name}</h3>
          <p className="text-sm text-gray-400">{product.brand}</p>

          <div className="flex items-center space-x-2">
            <span className="text-xl font-bold text-blue-400">${product.price}</span>
            {product.originalPrice && (
              <span className="text-sm text-gray-500 line-through">${product.originalPrice}</span>
            )}
          </div>

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
        </div>
      </div>
    </div>
  );
};
