import { useState, useEffect, useCallback, useMemo } from 'react';
import { Filter, Grid, List, Search } from 'lucide-react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

import { ProductCard, ProductFilters } from '../components/Product';
import { Pagination } from '../components/Pagination';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { useProducts, useProductCategories, type Product } from '../hooks/useProducts';
import { useCart, useWishlist } from '../hooks';
import type { ApiError } from '../types/error';
import {
  formatCategoryFromUrl,
  formatCategoryForUrl,
  urlCategoryToBackend,
} from '../utils/categoryUtils';

const Products = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const { addItem } = useCart();
  const { checkIsInWishlist, toggleWishlist: toggleWishlistHook } = useWishlist();
  
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<'name' | 'price' | 'createdAt'>('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [priceRange, setPriceRange] = useState({ min: '', max: '' });
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [addingToCartId, setAddingToCartId] = useState<string | null>(null);

  useEffect(() => {
    const categoryFromUrl = searchParams.get('category');
    const searchFromUrl = searchParams.get('search');

    if (categoryFromUrl) {
      setSelectedCategory(formatCategoryFromUrl(categoryFromUrl));
    }
    if (searchFromUrl) {
      setSearchQuery(searchFromUrl);
    }
  }, [searchParams]);

  const { data, isLoading, isError, error } = useProducts({
    page: currentPage,
    limit: 12,
    category: urlCategoryToBackend(formatCategoryForUrl(selectedCategory)),
    search: searchQuery || undefined,
    minPrice: priceRange.min ? parseFloat(priceRange.min) : undefined,
    maxPrice: priceRange.max ? parseFloat(priceRange.max) : undefined,
    sortBy,
    sortOrder,
  });

  const { data: categoriesData } = useProductCategories();
  const categories = useMemo(() => ['all', ...(categoriesData || [])], [categoriesData]);

  const handleCategoryChange = useCallback((category: string) => {
    const newCategory = category === 'all' ? 'all' : category;
    setSelectedCategory(newCategory);
    setCurrentPage(1);

    const newSearchParams = new URLSearchParams(searchParams);
    if (newCategory !== 'all') {
      newSearchParams.set('category', formatCategoryForUrl(newCategory));
    } else {
      newSearchParams.delete('category');
    }
    setSearchParams(newSearchParams);
  }, [searchParams, setSearchParams]);

  const handleSortChange = useCallback((sortValue: string) => {
    const [field, order] = sortValue.split('-') as [
      'name' | 'price' | 'createdAt',
      'asc' | 'desc'
    ];
    setSortBy(field);
    setSortOrder(order);
    setCurrentPage(1);
  }, []);

  const handleSearchChange = useCallback((query: string) => {
    setSearchQuery(query);
    setCurrentPage(1);

    const newSearchParams = new URLSearchParams(searchParams);
    if (query) {
      newSearchParams.set('search', query);
    } else {
      newSearchParams.delete('search');
    }
    setSearchParams(newSearchParams);
  }, [searchParams, setSearchParams]);

  const handleProductClick = useCallback((productId: string) => {
    navigate(`/products/${productId}`);
  }, [navigate]);

  const addToCart = useCallback(async (product: Product) => {
    if (!product.inStock) {
      toast.error('Product is out of stock');
      return;
    }

    setAddingToCartId(product.id);
    try {
      await addItem(product.id, 1);
    } catch (error) {
      console.error('Failed to add item to cart:', error);
    } finally {
      setAddingToCartId(null);
    }
  }, [addItem]);

  const toggleWishlist = useCallback(async (product: Product) => {
    await toggleWishlistHook(product.id, product.name);
  }, [toggleWishlistHook]);

  const sortOptions = useMemo(() => [
    { value: 'name-asc', label: 'Name A-Z' },
    { value: 'name-desc', label: 'Name Z-A' },
    { value: 'price-asc', label: 'Price: Low to High' },
    { value: 'price-desc', label: 'Price: High to Low' },
    { value: 'createdAt-desc', label: 'Newest' },
    { value: 'createdAt-asc', label: 'Oldest' },
  ], []);

  if (isError) {
    const errorMessage =
      (error as ApiError)?.response?.data?.message ||
      'Unable to load products. Please try again later.';

    return (
      <div className="animate-fade-in">
        <div className="text-center py-16">
          <div className="card p-12">
            <h2 className="text-2xl font-bold text-red-400 mb-4">Something went wrong</h2>
            <p className="text-gray-400 mb-6">{errorMessage}</p>
            <button onClick={() => window.location.reload()} className="btn-primary">
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <section className="mb-8">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-white mb-4">Our Products</h1>
          <p className="text-gray-400 max-w-2xl mx-auto">
            Discover our curated collection of premium products
          </p>
        </div>

        <div className="card p-6">
          <div className="flex flex-col md:flex-row gap-4 mb-4">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSearchChange(searchQuery);
              }}
              className="flex-1"
            >
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search products..."
                  value={searchQuery}
                  onChange={(e) => handleSearchChange(e.target.value)}
                  className="input w-full"
                  style={{ paddingLeft: '2.75rem', paddingRight: '1rem' }}
                />
              </div>
            </form>

            <select
              onChange={(e) => handleSortChange(e.target.value)}
              className="input md:w-64"
            >
              {sortOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center justify-between">
            <div className="flex space-x-2">
              <button
                onClick={() => setViewMode('grid')}
                className={`p-2 rounded-lg transition-colors cursor-pointer ${
                  viewMode === 'grid'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-800 text-gray-400 hover:text-white'
                }`}
              >
                <Grid className="w-5 h-5" />
              </button>
              <button
                onClick={() => setViewMode('list')}
                className={`p-2 rounded-lg transition-colors cursor-pointer ${
                  viewMode === 'list'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-800 text-gray-400 hover:text-white'
                }`}
              >
                <List className="w-5 h-5" />
              </button>
            </div>
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="btn-outline inline-flex items-center space-x-2"
            >
              <Filter className="w-5 h-5" />
              <span>Filters</span>
            </button>
          </div>

          {showFilters && (
            <ProductFilters
              categories={categories}
              selectedCategory={selectedCategory}
              priceRange={priceRange}
              onCategoryChange={handleCategoryChange}
              onPriceRangeChange={setPriceRange}
            />
          )}
        </div>
      </section>

      <section>
        {isLoading ? (
          <LoadingSpinner size="lg" message="Loading products..." className="py-16" />
        ) : (
          <>
            <div className="flex justify-between items-center mb-6">
              <p className="text-gray-400">
                {data?.products.length ? (
                  <>
                    Showing {data.currentPage * data.size + 1}-
                    {Math.min((data.currentPage + 1) * data.size, data.totalElements)} of{' '}
                    {data.totalElements} products
                  </>
                ) : (
                  'No products found'
                )}
              </p>
            </div>

            <div className={viewMode === 'grid' 
              ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8'
              : 'space-y-4 mb-8'
            }>
              {data?.products.map((product: Product, index: number) => (
                <ProductCard
                  key={product.id}
                  product={product}
                  variant={viewMode === 'grid' ? 'card' : 'list'}
                  onProductClick={handleProductClick}
                  onAddToCart={addToCart}
                  onToggleWishlist={toggleWishlist}
                  isLoading={false}
                  isAddingToCart={addingToCartId === product.id}
                  isInWishlist={checkIsInWishlist(product.id)}
                  animationDelay={index * 0.05}
                />
              ))}
            </div>

            {data && (
              <Pagination
                currentPage={currentPage}
                totalPages={data.totalPages}
                onPageChange={setCurrentPage}
              />
            )}
          </>
        )}
      </section>
    </div>
  );
};

export default Products;
