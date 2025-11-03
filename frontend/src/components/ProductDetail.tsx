import { Heart, Minus, Plus, Share, Shield, ShoppingCart, Star, Truck, X, MessageSquarePlus } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';

import { calculateDiscountPercentage, formatPrice, isProductOnSale, useProduct } from '../hooks/useProducts';
import { useCart } from '../hooks';
import ProductReviews from './ProductReviews';
import ReviewModal from './ReviewModal';
import { formatCategoryForDisplay } from '../utils/categoryUtils';
import type { ApiError } from '../types/error';

interface ProductDetailProps {
  productId: number | string;
  isOpen: boolean;
  onClose: () => void;
}

const ProductDetail = ({ productId, isOpen, onClose }: ProductDetailProps) => {
  const [quantity, setQuantity] = useState(1);
  const [selectedTab, setSelectedTab] = useState<'description' | 'reviews' | 'shipping'>('description');
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [isClosing, setIsClosing] = useState(false);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);

  const { data: product, isLoading, isError, error, refetch } = useProduct(productId);
  const { addItem } = useCart();

  const hasUserReviewed = () => {
    if (!product?.reviews || !localStorage.getItem('token')) return false;
    
    const userData = localStorage.getItem('user');
    if (!userData) return false;
    
    const user = JSON.parse(userData);
    const userId = user.id;

    return product.reviews.some((review: { userId?: string }) => review.userId === userId);
  };

  const handleClose = useCallback(() => {
    setIsClosing(true);
    setTimeout(() => {
      setIsClosing(false);
      onClose();
    }, 200);
  }, [onClose]);

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && isOpen) {
        handleClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleKeyDown);
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, handleClose]);

  const handleQuantityChange = (change: number) => {
    setQuantity(prev => Math.max(1, prev + change));
  };

  const handleAddToCart = async () => {
    if (product) {
      try {
        await addItem(product.id, quantity);
        setQuantity(1);
        console.log(`Added ${quantity} x ${product.name} to cart`);
        // TODO: Show success message
      } catch (error) {
        console.error('Failed to add item to cart:', error);
        // TODO: Show error message
      }
    }
  };

  const handleWishlistToggle = () => {
    setIsWishlisted(!isWishlisted);
    // TODO: Implement wishlist functionality
  };

  const handleShare = () => {
    // TODO: Implement share functionality
    if (navigator.share && product) {
      navigator.share({
        title: product.name,
        text: product.description,
        url: window.location.href,
      });
    }
  };

  if (!isOpen) return null;

  if (isLoading) {
    return (
      <div className={`fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4 transition-opacity duration-200 ${isClosing ? 'animate-fade-out' : 'animate-fade-in'
        }`}>
        <div className={`bg-gray-900 rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto transition-transform duration-200 ${isClosing ? 'animate-scale-out' : 'animate-scale-in'
          }`}>
          <div className="sticky top-0 bg-gray-900/95 backdrop-blur-sm border-b border-gray-700 p-6 flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="animate-pulse">
                <div className="h-8 bg-gray-700 rounded w-48"></div>
              </div>
              <div className="flex items-center space-x-2 text-blue-400">
                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-400"></div>
                <span className="text-sm font-medium">Loading...</span>
              </div>
            </div>
            <button
              onClick={handleClose}
              className="p-2 hover:bg-gray-800 rounded-lg transition-colors"
            >
              <X className="w-6 h-6 text-gray-400" />
            </button>
          </div>

          <div className="p-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div className="space-y-4">
                <div className="aspect-square bg-gray-800 rounded-2xl animate-pulse flex items-center justify-center">
                  <div className="text-gray-600 text-lg">Loading image...</div>
                </div>
              </div>
              <div className="space-y-6">
                <div className="animate-pulse space-y-4">
                  <div className="h-8 bg-gray-700 rounded w-3/4"></div>
                  <div className="flex items-center space-x-2">
                    <div className="flex space-x-1">
                      {[...Array(5)].map((_, i) => (
                        <div key={i} className="w-5 h-5 bg-gray-700 rounded"></div>
                      ))}
                    </div>
                    <div className="h-4 bg-gray-700 rounded w-20"></div>
                  </div>
                  <div className="space-y-2">
                    <div className="h-8 bg-gray-700 rounded w-32"></div>
                    <div className="h-4 bg-gray-700 rounded w-24"></div>
                  </div>
                  <div className="space-y-2">
                    <div className="h-4 bg-gray-700 rounded w-full"></div>
                    <div className="h-4 bg-gray-700 rounded w-5/6"></div>
                    <div className="h-4 bg-gray-700 rounded w-4/6"></div>
                  </div>
                  <div className="space-y-4">
                    <div className="h-12 bg-gray-700 rounded w-32"></div>
                    <div className="flex space-x-3">
                      <div className="h-12 bg-gray-700 rounded flex-1"></div>
                      <div className="h-12 bg-gray-700 rounded w-12"></div>
                      <div className="h-12 bg-gray-700 rounded w-12"></div>
                    </div>
                  </div>
                </div>
                <div className="text-center py-8">
                  <div className="inline-flex items-center space-x-3 text-gray-400">
                    <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500"></div>
                    <span>Loading product details...</span>
                  </div>
                </div>
              </div>
            </div>
            <div className="mt-8">
              <div className="border-b border-gray-700 mb-6">
                <div className="flex space-x-8">
                  {['Description', 'Reviews', 'Shipping'].map((_, index) => (
                    <div key={index} className="h-10 bg-gray-700 rounded w-24 animate-pulse"></div>
                  ))}
                </div>
              </div>
              <div className="space-y-3">
                <div className="h-4 bg-gray-700 rounded w-full animate-pulse"></div>
                <div className="h-4 bg-gray-700 rounded w-4/5 animate-pulse"></div>
                <div className="h-4 bg-gray-700 rounded w-3/5 animate-pulse"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (isError || !product) {
    const errorMessage = 
      (error as ApiError)?.response?.data?.message || 
      'Sorry, we couldn\'t load the product details.';
    
    return (
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
        <div className="bg-gray-900 rounded-2xl p-8 max-w-md w-full text-center">
          <h3 className="text-xl font-semibold text-white mb-4">Product Not Found</h3>
          <p className="text-gray-400 mb-6">{errorMessage}</p>
          <button onClick={onClose} className="btn-primary">
            Close
          </button>
        </div>
      </div>
    );
  }

  const discount = isProductOnSale(product) ? calculateDiscountPercentage(product.originalPrice!, product.price) : 0;

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      handleClose();
    }
  };

  return (
    <div
      className={`fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4 transition-opacity duration-200 ${isClosing ? 'animate-fade-out' : 'animate-fade-in'
        }`}
      onClick={handleBackdropClick}
    >
      <div className={`bg-gray-900 rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto transition-transform duration-200 ${isClosing ? 'animate-scale-out' : 'animate-scale-in'
        }`}>
        <div className="sticky top-0 bg-gray-900/95 backdrop-blur-sm border-b border-gray-700 p-6 flex items-center justify-between">
          <h2 className="text-2xl font-bold text-white">Product Details</h2>
          <button
            onClick={handleClose}
            className="p-2 hover:bg-gray-800 rounded-lg transition-colors"
          >
            <X className="w-6 h-6 text-gray-400" />
          </button>
        </div>

        <div className="p-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div className="space-y-4">
              <div className="aspect-square bg-gray-800 rounded-2xl overflow-hidden relative">
                <img
                  src={product.imageUrl}
                  alt={product.name}
                  className="w-full h-full object-cover"
                />
                {discount > 0 && (
                  <div className="absolute top-4 left-4 bg-red-500 text-white px-3 py-1 rounded-full text-sm font-medium">
                    -{discount}%
                  </div>
                )}
                {!product.inStock && (
                  <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
                    <span className="bg-red-500 text-white px-4 py-2 rounded-lg font-medium">
                      Out of Stock
                    </span>
                  </div>
                )}
              </div>
            </div>

            <div className="space-y-6">
              <div>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-blue-400 font-medium text-sm">{formatCategoryForDisplay(product.category)}</span>
                  <button
                    onClick={handleShare}
                    className="p-2 hover:bg-gray-800 rounded-lg transition-colors"
                  >
                    <Share className="w-5 h-5 text-gray-400" />
                  </button>
                </div>
                <h1 className="text-3xl font-bold text-white mb-2">{product.name}</h1>
                {product.brand && (
                  <p className="text-gray-400 mb-4">by {product.brand}</p>
                )}
                <div className="flex items-center space-x-2 mb-4">
                  <div className="flex items-center">
                    {[...Array(5)].map((_, i) => (
                      <Star
                        key={i}
                        className={`w-5 h-5 ${i < Math.floor(product.averageRating)
                          ? 'text-yellow-400 fill-current'
                          : 'text-gray-600'
                          }`}
                      />
                    ))}
                  </div>
                  <span className="text-white font-medium">{product.averageRating}</span>
                  <span className="text-gray-400">({product.totalReviews === 1 ? '1 review' : `${product.totalReviews} reviews`})</span>
                </div>

                <div className="flex items-center space-x-3 mb-6">
                  <span className="text-3xl font-bold text-blue-400">
                    {formatPrice(product.price)}
                  </span>
                  {product.originalPrice && (
                    <span className="text-xl text-gray-500 line-through">
                      {formatPrice(product.originalPrice)}
                    </span>
                  )}
                </div>
              </div>

              <div className="space-y-4">
                <div className="flex items-center space-x-4">
                  <span className="text-white font-medium">Quantity:</span>
                  <div className="flex items-center border border-gray-600 rounded-lg">
                    <button
                      onClick={() => handleQuantityChange(-1)}
                      className="p-2 hover:bg-gray-700 transition-colors"
                      disabled={quantity <= 1}
                    >
                      <Minus className="w-4 h-4 text-gray-400" />
                    </button>
                    <span className="px-4 py-2 text-white font-medium min-w-12 text-center">
                      {quantity}
                    </span>
                    <button
                      onClick={() => handleQuantityChange(1)}
                      className="p-2 hover:bg-gray-700 transition-colors"
                      disabled={!product.inStock}
                    >
                      <Plus className="w-4 h-4 text-gray-400" />
                    </button>
                  </div>
                </div>

                <div className="flex space-x-3">
                  <button
                    onClick={handleAddToCart}
                    disabled={!product.inStock}
                    className="btn-primary flex-1 inline-flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <ShoppingCart className="w-5 h-5" />
                    <span>{product.inStock ? 'Add to Cart' : 'Out of Stock'}</span>
                  </button>
                  <button
                    onClick={handleWishlistToggle}
                    className={`p-3 rounded-lg border transition-colors ${isWishlisted
                      ? 'bg-red-500 border-red-500 text-white'
                      : 'border-gray-600 text-gray-400 hover:border-red-500 hover:text-red-500'
                      }`}
                  >
                    <Heart className={`w-5 h-5 ${isWishlisted ? 'fill-current' : ''}`} />
                  </button>
                </div>

                {/* Leave a Review Button */}
                {localStorage.getItem('token') && (
                  <button
                    onClick={() => setIsReviewModalOpen(true)}
                    className={`w-full mt-3 px-4 py-3 border rounded-lg transition-colors inline-flex items-center justify-center space-x-2 ${
                      hasUserReviewed()
                        ? 'bg-gray-700 border-gray-500 text-gray-400 cursor-not-allowed'
                        : 'bg-gray-800 border-gray-600 text-gray-300 hover:bg-gray-700 hover:border-gray-500'
                    }`}
                    disabled={hasUserReviewed()}
                  >
                    <MessageSquarePlus className="w-5 h-5" />
                    <span>{hasUserReviewed() ? 'You\'ve already reviewed this product' : 'Leave a Review'}</span>
                  </button>
                )}

                {product.inStock && (
                  <div className="text-sm">
                    {product.stockQuantity > 10 ? (
                      <span className="text-green-400">✓ In Stock ({product.stockQuantity} available)</span>
                    ) : product.stockQuantity > 0 ? (
                      <span className="text-yellow-400">⚠ Low Stock ({product.stockQuantity} remaining)</span>
                    ) : (
                      <span className="text-red-400">✗ Out of Stock</span>
                    )}
                  </div>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="flex items-center space-x-2 text-sm text-gray-400">
                  <Shield className="w-4 h-4" />
                  <span>Quality Guaranteed</span>
                </div>
                <div className="flex items-center space-x-2 text-sm text-gray-400">
                  <Truck className="w-4 h-4" />
                  <span>Fast Shipping</span>
                </div>
                <div className="flex items-center space-x-2 text-sm text-gray-400">
                  <Star className="w-4 h-4" />
                  <span>Top Rated</span>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-12">
            <div className="border-b border-gray-700">
              <nav className="flex space-x-8">
                {[
                  { id: 'description', label: 'Description' },
                  { id: 'reviews', label: 'Reviews' },
                  { id: 'shipping', label: 'Shipping' },
                ].map(tab => (
                  <button
                    key={tab.id}
                    onClick={() => setSelectedTab(tab.id as 'description' | 'reviews' | 'shipping')}
                    className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${selectedTab === tab.id
                      ? 'border-blue-500 text-blue-400'
                      : 'border-transparent text-gray-400 hover:text-white'
                      }`}
                  >
                    {tab.label}
                  </button>
                ))}
              </nav>
            </div>

            <div className="py-6">
              {selectedTab === 'description' && (
                <div className="prose prose-invert max-w-none">
                  <p className="text-gray-300 leading-relaxed">{product.description}</p>
                  {product.tags && product.tags.length > 0 && (
                    <div className="mt-6">
                      <h4 className="text-white font-medium mb-3">Tags:</h4>
                      <div className="flex flex-wrap gap-2">
                        {product.tags.map((tag: string) => (
                          <span
                            key={tag}
                            className="px-3 py-1 bg-gray-800 text-gray-300 rounded-full text-sm"
                          >
                            {tag}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}

              {selectedTab === 'reviews' && (
                <ProductReviews
                  reviews={product.reviews || []}
                  averageRating={product.averageRating}
                  totalReviews={product.totalReviews}
                />
              )}

              {selectedTab === 'shipping' && (
                <div className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <h4 className="text-white font-medium mb-2">Shipping Options</h4>
                      <ul className="space-y-2 text-gray-300">
                        <li>• Standard Shipping: 5-7 business days</li>
                        <li>• Express Shipping: 2-3 business days</li>
                        <li>• Overnight Shipping: 1 business day</li>
                      </ul>
                    </div>
                    <div>
                      <h4 className="text-white font-medium mb-2">Return Policy</h4>
                      <ul className="space-y-2 text-gray-300">
                        <li>• 30-day return window</li>
                        <li>• Free returns on orders over $50</li>
                        <li>• Original packaging required</li>
                      </ul>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {product && (
        <ReviewModal
          productId={String(product.id)}
          productName={product.name}
          productImage={product.imageUrl}
          isOpen={isReviewModalOpen}
          onClose={() => setIsReviewModalOpen(false)}
          onReviewSubmitted={() => {
            refetch();
            setSelectedTab('reviews');
          }}
        />
      )}
    </div>
  );
};

export default ProductDetail;
