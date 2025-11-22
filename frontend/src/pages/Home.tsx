import { Link, useNavigate } from 'react-router-dom';
import { useState, useEffect, useRef } from 'react';

import { ArrowRight, Star, ShoppingBag, Truck, Shield, Headphones, Loader2, ChevronLeft, ChevronRight } from 'lucide-react';
import { useProducts, type Product } from '../hooks/useProducts';
import { formatCategoryForDisplay } from '../utils/categoryUtils';

const Home = () => {
  const navigate = useNavigate();
  const { data: productsData, isLoading, error } = useProducts({
    limit: 8,
    sortBy: 'createdAt',
    sortOrder: 'desc'
  });

  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const [isAutoScrolling, setIsAutoScrolling] = useState(true);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  const handleProductClick = (productId: string) => {
    navigate(`/products/${productId}`);
  };

  useEffect(() => {
    if (!isAutoScrolling || !scrollContainerRef.current || !productsData?.products) return;

    let animationId: number;

    const smoothScroll = () => {
      const container = scrollContainerRef.current;
      if (!container) return;

      const scrollSpeed = 0.5;
      const maxScroll = container.scrollWidth - container.clientWidth;

      if (container.scrollLeft >= maxScroll) {
        container.scrollLeft = 0;
      } else {
        container.scrollLeft += scrollSpeed;
      }

      if (isAutoScrolling) {
        animationId = requestAnimationFrame(smoothScroll);
      }
    };

    animationId = requestAnimationFrame(smoothScroll);

    return () => {
      if (animationId) {
        cancelAnimationFrame(animationId);
      }
    };
  }, [isAutoScrolling, productsData?.products]);

  const updateScrollButtons = () => {
    if (!scrollContainerRef.current) return;

    const container = scrollContainerRef.current;
    setCanScrollLeft(container.scrollLeft > 0);
    setCanScrollRight(container.scrollLeft < container.scrollWidth - container.clientWidth);
  };
  const scrollLeft = () => {
    if (!scrollContainerRef.current) return;
    const cardWidth = 320 + 24;
    scrollContainerRef.current.scrollBy({ left: -cardWidth, behavior: 'smooth' });
    setIsAutoScrolling(false);
  };

  const scrollRight = () => {
    if (!scrollContainerRef.current) return;
    const cardWidth = 320 + 24;
    scrollContainerRef.current.scrollBy({ left: cardWidth, behavior: 'smooth' });
    setIsAutoScrolling(false);
  };

  useEffect(() => {
    if (!isAutoScrolling) {
      const timeout = setTimeout(() => {
        setIsAutoScrolling(true);
      }, 3000);

      return () => clearTimeout(timeout);
    }
  }, [isAutoScrolling]);

  const features = [
    {
      icon: <Truck className="w-8 h-8 text-blue-400" />,
      title: "Free Shipping",
      description: "Free delivery on orders over $50"
    },
    {
      icon: <Shield className="w-8 h-8 text-blue-400" />,
      title: "Quality Guaranteed",
      description: "100% authentic products"
    },
    {
      icon: <Headphones className="w-8 h-8 text-blue-400" />,
      title: "24/7 Support",
      description: "Expert customer service"
    }
  ]

  return (
    <div className="animate-fade-in">
      <section className="relative py-20 mb-16">
        <div className="absolute inset-0 bg-linear-to-r from-blue-900/20 to-purple-900/20 rounded-2xl"></div>
        <div className="relative text-center">
          <h1 className="text-5xl md:text-6xl font-bold mb-6 bg-linear-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
            Premium Health Supplements
          </h1>
          <p className="text-xl md:text-2xl text-gray-300 mb-8 max-w-3xl mx-auto">
            Discover the finest collection of health supplements, vitamins, and wellness products
            to fuel your journey to optimal health.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/products" className="btn-primary inline-flex items-center space-x-2">
              <ShoppingBag className="w-5 h-5" />
              <span>Shop Now</span>
              <ArrowRight className="w-5 h-5" />
            </Link>
            <Link to="/about" className="btn-outline inline-flex items-center space-x-2">
              <span>Learn More</span>
            </Link>
          </div>
        </div>
      </section>
      <section className="mb-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {features.map((feature, index) => (
            <div key={index} className="card p-6 text-center animate-slide-in" style={{ animationDelay: `${index * 0.1}s` }}>
              <div className="flex justify-center mb-4">
                {feature.icon}
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">{feature.title}</h3>
              <p className="text-gray-400">{feature.description}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="mb-16">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">Top Rated Products</h2>
          <p className="text-gray-400 text-lg">Hand-picked favorites from our customers</p>
        </div>

        {isLoading && (
          <div className="flex justify-center items-center py-16">
            <Loader2 className="w-8 h-8 text-blue-400 animate-spin" />
            <span className="ml-3 text-gray-400">Loading products...</span>
          </div>
        )}

        {error && (
          <div className="text-center py-16">
            <p className="text-red-400 mb-4">Failed to load products</p>
            <Link to="/products" className="btn-primary">
              View All Products
            </Link>
          </div>
        )}

        {!isLoading && !error && productsData?.products && (
          <>
            <div className="relative">
              <button
                onClick={scrollLeft}
                disabled={!canScrollLeft}
                className={`absolute left-0 top-1/2 -translate-y-1/2 z-10 p-3 rounded-full bg-gray-800 border border-gray-700 transition-all duration-200 ${canScrollLeft
                    ? 'hover:bg-gray-700 text-white'
                    : 'opacity-50 cursor-not-allowed text-gray-500'
                  }`}
                aria-label="Previous products"
              >
                <ChevronLeft className="w-6 h-6" />
              </button>

              <button
                onClick={scrollRight}
                disabled={!canScrollRight}
                className={`absolute right-0 top-1/2 -translate-y-1/2 z-10 p-3 rounded-full bg-gray-800 border border-gray-700 transition-all duration-200 ${canScrollRight
                    ? 'hover:bg-gray-700 text-white'
                    : 'opacity-50 cursor-not-allowed text-gray-500'
                  }`}
                aria-label="Next products"
              >
                <ChevronRight className="w-6 h-6" />
              </button>

              <div
                ref={scrollContainerRef}
                className="overflow-x-auto scrollbar-hide mx-12"
                onScroll={updateScrollButtons}
                onMouseEnter={() => setIsAutoScrolling(false)}
                onMouseLeave={() => setIsAutoScrolling(true)}
                onTouchStart={() => setIsAutoScrolling(false)}
                onTouchEnd={() => {
                  setTimeout(() => setIsAutoScrolling(true), 2000);
                }}
              >
                <div className="flex space-x-6 pb-4" style={{ width: 'max-content' }}>
                  {productsData.products.map((product: Product, index: number) => (
                    <div
                      key={product.id}
                      onClick={() => handleProductClick(product.id)}
                      className="shrink-0 w-80 card-hover p-6 animate-slide-in group cursor-pointer"
                      style={{ animationDelay: `${index * 0.1}s` }}
                    >
                      <div className="aspect-square bg-gray-800 rounded-lg mb-4 overflow-hidden">
                        <img
                          src={product.imageUrl || 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?w=300&h=300&fit=crop&crop=center'}
                          alt={product.name}
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                        />
                      </div>
                      <div className="flex justify-between items-start mb-2">
                        <span className="text-sm text-blue-400 font-medium">{formatCategoryForDisplay(product.category)}</span>
                        <div className="flex items-center space-x-1">
                          <Star className="w-4 h-4 text-yellow-400 fill-current" />
                          <span className="text-sm text-gray-300">{product.averageRating.toFixed(1)}</span>
                        </div>
                      </div>
                      <h3 className="text-xl font-semibold text-white mb-2 group-hover:text-blue-400 transition-colors">
                        {product.name}
                      </h3>
                      <p className="text-gray-400 text-sm mb-4 line-clamp-2">{product.description}</p>
                      <div className="flex justify-between items-center">
                        <span className="text-2xl font-bold text-blue-400">${product.price.toFixed(2)}</span>
                        {!product.inStock && (
                          <span className="text-red-400 text-sm font-medium">Out of Stock</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="text-center mt-2 md:hidden">
                <p className="text-gray-500 text-sm">← Swipe to see more products →</p>
              </div>
            </div>
          </>
        )}

        {!isLoading && !error && (!productsData?.products || productsData.products.length === 0) && (
          <div className="text-center py-16">
            <p className="text-gray-400 mb-4">No products available at the moment</p>
            <Link to="/products" className="btn-primary">
              Check Back Later
            </Link>
          </div>
        )}
      </section>
      <section className="text-center py-16">
        <div className="card p-12">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
            Ready to Start Your Health Journey?
          </h2>
          <p className="text-gray-400 text-lg mb-8 max-w-2xl mx-auto">
            Join thousands of satisfied customers who trust SuppleMart for their health and wellness needs.
          </p>
          <Link to="/products" className="btn-primary inline-flex items-center space-x-2">
            <span>Browse All Products</span>
            <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>
    </div>
  )
}

export default Home;
