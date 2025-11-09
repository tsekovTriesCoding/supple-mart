import { Heart, ShoppingBag, Star } from 'lucide-react';

import type { Product } from '../../hooks/useProducts';

interface ProductListItemProps {
  product: Product;
  onProductClick: (productId: number) => void;
  onAddToCart: (product: Product) => void;
  onToggleWishlist: (product: Product) => void;
  isLoading?: boolean;
  isAddingToCart?: boolean;
  animationDelay?: number;
}

export const ProductListItem = ({
  product,
  onProductClick,
  onAddToCart,
  onToggleWishlist,
  isLoading = false,
  isAddingToCart = false,
  animationDelay = 0,
}: ProductListItemProps) => {
  return (
    <div
      className={`card-hover p-6 animate-slide-in cursor-pointer transition-all duration-200 ${
        isLoading ? 'opacity-75 scale-98' : 'hover:scale-[1.01]'
      }`}
      style={{ animationDelay: `${animationDelay}s` }}
      onClick={() => onProductClick(product.id)}
    >
      {isLoading && (
        <div className="absolute inset-0 bg-blue-500/20 rounded-lg flex items-center justify-center z-10">
          <div className="bg-blue-500 text-white px-4 py-2 rounded-full font-medium flex items-center space-x-2">
            <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
            <span>Opening product details...</span>
          </div>
        </div>
      )}
      
      <div className="flex flex-col md:flex-row gap-6 relative">
        <div className="w-full md:w-48 h-48 bg-gray-800 rounded-lg overflow-hidden relative shrink-0">
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-cover"
          />
          {!product.inStock && (
            <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
              <span className="text-white font-semibold">Out of Stock</span>
            </div>
          )}
        </div>

        <div className="flex-1">
          <div className="flex justify-between items-start mb-2">
            <div>
              <span className="text-sm text-blue-400 font-medium">{product.category}</span>
              <h3 className="text-xl font-semibold text-white mt-1">{product.name}</h3>
              <p className="text-gray-400 mt-1">{product.brand}</p>
            </div>
            <button
              onClick={(e) => {
                e.stopPropagation();
                onToggleWishlist(product);
              }}
              className="p-2 rounded-full hover:bg-gray-700 transition-colors"
            >
              <Heart className="w-5 h-5 text-gray-400" />
            </button>
          </div>

          <div className="flex items-center space-x-1 mb-3">
            <Star className="w-4 h-4 text-yellow-400 fill-current" />
            <span className="text-sm text-gray-300">{product.averageRating}</span>
            <span className="text-xs text-gray-500">({product.totalReviews} reviews)</span>
          </div>

          <p className="text-gray-400 mb-4 line-clamp-2">{product.description}</p>

          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <span className="text-2xl font-bold text-blue-400">${product.price}</span>
              {product.originalPrice && (
                <span className="text-lg text-gray-500 line-through">${product.originalPrice}</span>
              )}
            </div>
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
          </div>
        </div>
      </div>
    </div>
  );
};
