import { useState } from 'react';
import { Heart} from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';

import { useCart, useWishlist } from '../hooks';
import { ProductCard } from '../components/Product';
import { LoadingSpinner } from '../components/LoadingSpinner';
import type { WishlistItem } from '../types/wishlist';
import type { Product } from '../hooks/useProducts';

const Wishlist = () => {
  const navigate = useNavigate();
  const { addItem } = useCart();
  const { wishlistItems, wishlistCount, isLoadingWishlist, removeFromWishlist } = useWishlist();
  const [addingToCartId, setAddingToCartId] = useState<string | null>(null);

  const convertToProduct = (item: WishlistItem): Product => ({
    id: item.productId,
    name: item.productName,
    description: item.productDescription,
    price: item.price,
    originalPrice: item.originalPrice,
    category: item.category,
    imageUrl: item.imageUrl || '',
    inStock: item.inStock,
    stock: item.stockQuantity,
    averageRating: item.averageRating,
    totalReviews: item.totalReviews,
  });

  const handleProductClick = (productId: string) => {
    navigate(`/products/${productId}`);
  };

  const handleAddToCart = async (product: Product) => {
    setAddingToCartId(product.id);
    try {
      await addItem(product.id, 1);
    } catch (error) {
      console.error('Failed to add to cart:', error);
    } finally {
      setAddingToCartId(null);
    }
  };

  const handleToggleWishlist = async (product: Product) => {
    await removeFromWishlist(product.id);
  };

  if (!localStorage.getItem('token')) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <Heart className="w-16 h-16 text-gray-600 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-white mb-4">Please Sign In</h1>
          <p className="text-gray-400 mb-6">You need to be logged in to view your wishlist.</p>
          <Link to="/" className="btn-primary">
            Go Home
          </Link>
        </div>
      </div>
    );
  }

  if (isLoadingWishlist) {
    return <LoadingSpinner size="lg" message="Loading your wishlist..." fullScreen />;
  }

  if (wishlistItems.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <Heart className="w-24 h-24 text-gray-600 mx-auto mb-6" />
          <h2 className="text-3xl font-bold text-white mb-4">Your Wishlist is Empty</h2>
          <p className="text-gray-400 mb-8">Start adding products you love to your wishlist!</p>
          <Link to="/products" className="btn-primary">
            Browse Products
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-white mb-2">My Wishlist</h1>
              <p className="text-gray-400">
                {wishlistCount} {wishlistCount === 1 ? 'item' : 'items'} saved
              </p>
            </div>
            <Heart className="w-8 h-8 text-red-400 fill-current" />
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {wishlistItems.map((item: WishlistItem, index: number) => {
            const product = convertToProduct(item);
            
            return (
              <ProductCard
                key={item.id}
                product={product}
                onProductClick={handleProductClick}
                onAddToCart={handleAddToCart}
                onToggleWishlist={handleToggleWishlist}
                isAddingToCart={addingToCartId === product.id}
                isInWishlist={true}
                animationDelay={index * 0.05}
              />
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default Wishlist;
