import { Heart, Minus, Plus, Share2, Shield, ShoppingCart, Star, Truck, ArrowLeft, MessageSquarePlus } from 'lucide-react';
import { useEffect, useState, useCallback, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

import { calculateDiscountPercentage, formatPrice, isProductOnSale, useProduct } from '../hooks/useProducts';
import { useCart, useWishlist, useAuth } from '../hooks';
import { ProductReviews } from '../components/Product';
import ReviewModal from '../components/ReviewModal';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { formatCategoryForDisplay } from '../utils/categoryUtils';
import type { ApiError } from '../types/error';

const ProductDetail = () => {
  const { productId } = useParams<{ productId: string }>();
  const navigate = useNavigate();
  const [quantity, setQuantity] = useState(1);
  const [selectedTab, setSelectedTab] = useState<'description' | 'reviews' | 'shipping'>('description');
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);

  const { data: product, isLoading, isError, error, refetch } = useProduct(productId!);
  const { addItem } = useCart();
  const { isWishlisted, toggleWishlist, isTogglingWishlist } = useWishlist({ 
    productId: productId?.toString() 
  });
  const { isLoggedIn, user } = useAuth();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [productId]);

  const hasUserReviewed = useMemo(() => {
    if (!product?.reviews || !isLoggedIn || !user) return false;
    return product.reviews.some((review: { userId?: string }) => review.userId === user.id);
  }, [product?.reviews, isLoggedIn, user]);

  const handleQuantityChange = useCallback((change: number) => {
    setQuantity(prev => Math.max(1, prev + change));
  }, []);

  const handleAddToCart = useCallback(async () => {
    if (product) {
      if (!product.inStock) {
        toast.error('Product is out of stock');
        return;
      }

      try {
        await addItem(product.id, quantity);
        setQuantity(1);
      } catch (error) {
        console.error('Failed to add item to cart:', error);
      }
    }
  }, [product, quantity, addItem]);

  const handleWishlistToggle = useCallback(async () => {
    if (!product) return;
    await toggleWishlist(product.id, product.name);
  }, [product, toggleWishlist]);

  const handleShare = useCallback(() => {
    if (navigator.share && product) {
      navigator.share({
        title: product.name,
        text: product.description,
        url: window.location.href,
      }).catch(err => console.error('Share failed:', err));
    }
  }, [product]);

  const handleReviewAdded = useCallback(() => {
    refetch();
  }, [refetch]);

  if (isLoading) {
    return <LoadingSpinner size="lg" message="Loading product..." fullScreen />;
  }

  if (isError) {
    const apiError = error as ApiError;
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">⚠️</div>
          <h2 className="text-2xl font-bold text-white mb-2">Error Loading Product</h2>
          <p className="text-gray-400 mb-4">{apiError.response?.data?.message || 'Failed to load product details'}</p>
          <button onClick={() => navigate('/products')} className="btn-primary">
            Back to Products
          </button>
        </div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <div className="text-gray-500 text-6xl mb-4">📦</div>
          <h2 className="text-2xl font-bold text-white mb-2">Product Not Found</h2>
          <p className="text-gray-400 mb-4">The product you're looking for doesn't exist</p>
          <button onClick={() => navigate('/products')} className="btn-primary">
            Back to Products
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="container mx-auto px-4 py-8">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center space-x-2 text-gray-400 hover:text-white transition-colors mb-6 cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Back</span>
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-12">
          <div className="relative">
            <div className="aspect-square bg-gray-800 rounded-lg overflow-hidden">
              <img
                src={product.imageUrl}
                alt={product.name}
                className="w-full h-full object-cover"
              />
            </div>
            {!product.inStock && (
              <div className="absolute inset-0 bg-black/60 flex items-center justify-center rounded-lg">
                <span className="text-white text-2xl font-semibold">Out of Stock</span>
              </div>
            )}
          </div>

          <div className="space-y-6">
            <div>
              <div className="flex items-start justify-between mb-2">
                <div className="flex-1">
                  <span className="text-blue-400 text-sm font-medium">
                    {formatCategoryForDisplay(product.category)}
                  </span>
                  <h1 className="text-3xl font-bold text-white mt-2">{product.name}</h1>
                  {product.brand && (
                    <p className="text-gray-400 mt-1">by {product.brand}</p>
                  )}
                </div>
              </div>

              <div className="flex items-center space-x-4 mb-4">
                <div className="flex items-center space-x-1">
                  <Star className="w-5 h-5 text-yellow-400 fill-current" />
                  <span className="text-white font-medium">{product.averageRating}</span>
                  <span className="text-gray-400">
                    ({product.totalReviews} {product.totalReviews === 1 ? 'review' : 'reviews'})
                  </span>
                </div>
              </div>

              <div className="flex items-center space-x-4 mb-6">
                <span className="text-4xl font-bold text-blue-400">
                  {formatPrice(product.price)}
                </span>
                {isProductOnSale(product) && (
                  <>
                    <span className="text-xl text-gray-500 line-through">
                      {formatPrice(product.originalPrice!)}
                    </span>
                    <span className="bg-red-500 text-white px-3 py-1 rounded-full text-sm font-bold">
                      {calculateDiscountPercentage(product.originalPrice!, product.price)}% OFF
                    </span>
                  </>
                )}
              </div>
            </div>

            {product.inStock && (
              <div className="space-y-4">
                <div>
                  <label className="text-gray-300 block mb-2">Quantity</label>
                  <div className="flex items-center space-x-4">
                    <button
                      onClick={() => handleQuantityChange(-1)}
                      disabled={quantity <= 1}
                      className="p-2 bg-gray-700 hover:bg-gray-600 rounded disabled:opacity-50 disabled:cursor-not-allowed transition-colors cursor-pointer"
                    >
                      <Minus className="w-5 h-5 text-white" />
                    </button>
                    <span className="text-white font-semibold text-xl w-12 text-center">{quantity}</span>
                    <button
                      onClick={() => handleQuantityChange(1)}
                      className="p-2 bg-gray-700 hover:bg-gray-600 rounded transition-colors cursor-pointer"
                    >
                      <Plus className="w-5 h-5 text-white" />
                    </button>
                  </div>
                </div>

                <div className="flex space-x-3">
                  <button
                    onClick={handleAddToCart}
                    className="btn-primary flex-1 flex items-center justify-center space-x-2"
                  >
                    <ShoppingCart className="w-5 h-5" />
                    <span>Add to Cart</span>
                  </button>

                  <button
                    onClick={handleWishlistToggle}
                    disabled={isTogglingWishlist}
                    className={`p-3 rounded-lg transition-colors cursor-pointer ${
                      isWishlisted
                        ? 'bg-red-500 hover:bg-red-600 text-white'
                        : 'bg-gray-700 hover:bg-gray-600 text-white'
                    }`}
                  >
                    <Heart className={`w-6 h-6 ${isWishlisted ? 'fill-current' : ''}`} />
                  </button>

                  <button
                    onClick={handleShare}
                    className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors cursor-pointer"
                  >
                    <Share2 className="w-6 h-6 text-white" />
                  </button>
                </div>
              </div>
            )}

            {!product.inStock && (
              <div className="p-4 bg-red-500/10 border border-red-500 rounded-lg">
                <p className="text-red-400 font-semibold">This product is currently out of stock</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4 pt-6 border-t border-gray-700">
              <div className="flex items-center space-x-3">
                <Truck className="w-6 h-6 text-blue-400" />
                <div>
                  <p className="text-white font-medium">Free Shipping</p>
                  <p className="text-gray-400 text-sm">On orders over $50</p>
                </div>
              </div>
              <div className="flex items-center space-x-3">
                <Shield className="w-6 h-6 text-blue-400" />
                <div>
                  <p className="text-white font-medium">Secure Payment</p>
                  <p className="text-gray-400 text-sm">100% secure</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="mb-8">
          <div className="flex space-x-1 border-b border-gray-700">
            {['description', 'reviews', 'shipping'].map((tab) => (
              <button
                key={tab}
                onClick={() => setSelectedTab(tab as typeof selectedTab)}
                className={`px-6 py-3 font-medium capitalize transition-colors cursor-pointer ${
                  selectedTab === tab
                    ? 'text-blue-400 border-b-2 border-blue-400'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                {tab}
              </button>
            ))}
          </div>

          <div className="py-6">
            {selectedTab === 'description' && (
              <div className="text-gray-300 space-y-4">
                <p>{product.description}</p>
              </div>
            )}

            {selectedTab === 'reviews' && (
              <div>
                {isLoggedIn && !hasUserReviewed && (
                  <button
                    onClick={() => setIsReviewModalOpen(true)}
                    className="btn-primary mb-6 flex items-center space-x-2"
                  >
                    <MessageSquarePlus className="w-5 h-5" />
                    <span>Write a Review</span>
                  </button>
                )}
                <ProductReviews reviews={product.reviews || []} averageRating={product.averageRating} totalReviews={product.totalReviews} />
              </div>
            )}

            {selectedTab === 'shipping' && (
              <div className="text-gray-300 space-y-4">
                <div>
                  <h3 className="text-white font-semibold mb-2">Shipping Information</h3>
                  <ul className="list-disc list-inside space-y-2">
                    <li>Free shipping on orders over $50</li>
                    <li>Standard delivery: 3-5 business days</li>
                    <li>Express delivery: 1-2 business days (additional charges apply)</li>
                    <li>International shipping available</li>
                  </ul>
                </div>
                <div>
                  <h3 className="text-white font-semibold mb-2">Returns & Exchanges</h3>
                  <ul className="list-disc list-inside space-y-2">
                    <li>30-day return policy</li>
                    <li>Free returns on all orders</li>
                    <li>Items must be unused and in original packaging</li>
                  </ul>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {isReviewModalOpen && (
        <ReviewModal
          productId={product.id}
          productName={product.name}
          productImage={product.imageUrl}
          isOpen={isReviewModalOpen}
          onClose={() => setIsReviewModalOpen(false)}
          onReviewSubmitted={handleReviewAdded}
        />
      )}
    </div>
  );
};

export default ProductDetail;
