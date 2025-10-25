import { Filter, Grid, Heart, List, Minus, Plus, Search, ShoppingBag, Star } from 'lucide-react';
import { useState } from 'react';

import ProductDetail from '../components/ProductDetail';
import { useProductCategories, useProducts, type Product } from '../hooks/useProducts';

const Products = () => {
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [priceRange, setPriceRange] = useState({ min: '', max: '' });
  const [sortBy, setSortBy] = useState<'name' | 'price' | 'createdAt'>('name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { data, isLoading, isError } = useProducts({
    page: currentPage,
    limit: 12,
    category: selectedCategory || undefined,
    search: searchQuery || undefined,
    sortBy,
    sortOrder
  });

  const productsData = data;

  const { data: categoriesData } = useProductCategories();

  const categories = categoriesData || [
    'All',
    'Protein',
    'Creatine',
    'Pre-Workout',
    'Amino Acids',
    'Vitamins',
    'Weight Loss'
  ];

  const sortOptions = [
    { value: 'name-asc', label: 'Name A-Z' },
    { value: 'name-desc', label: 'Name Z-A' },
    { value: 'price-asc', label: 'Price: Low to High' },
    { value: 'price-desc', label: 'Price: High to Low' },
    { value: 'createdAt-desc', label: 'Newest' },
    { value: 'createdAt-asc', label: 'Oldest' }
  ];

  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category === 'All' ? '' : category);
    setCurrentPage(1);
  };

  const handleSortChange = (sortValue: string) => {
    const [field, direction] = sortValue.split('-');
    setSortBy(field as 'name' | 'price' | 'createdAt');
    setSortOrder(direction as 'asc' | 'desc');
    setCurrentPage(1);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(1);
  };

  const handleProductClick = (productId: number) => {
    setSelectedProductId(productId);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedProductId(null);
  };

  const addToCart = (product: Product) => {
    console.log('Adding to cart:', product);
  };

  const toggleWishlist = (product: Product) => {
    console.log('Toggle wishlist:', product);
  };

  if (isError) {
    return (
      <div className="animate-fade-in">
        <div className="text-center py-16">
          <div className="card p-12">
            <h2 className="text-2xl font-bold text-red-400 mb-4">Something went wrong</h2>
            <p className="text-gray-400 mb-6">Unable to load products. Please try again later.</p>
            <button
              onClick={() => window.location.reload()}
              className="btn-primary"
            >
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
          <h1 className="text-4xl md:text-5xl font-bold mb-4 bg-linear-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
            Premium Products
          </h1>
          <p className="text-gray-400 text-lg max-w-2xl mx-auto">
            Discover our complete collection of high-quality supplements and wellness products
          </p>
        </div>

        <div className="card p-6 mb-8">
          <div className="flex flex-col lg:flex-row gap-4 items-center justify-between">
            <form onSubmit={handleSearch} className="flex-1 w-full lg:max-w-md">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" />
                <input
                  type="text"
                  placeholder="Search products..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="input w-full"
                  style={{ paddingLeft: '3rem', paddingRight: '1rem' }}
                />
              </div>
            </form>

            <div className="flex items-center space-x-4">
              <select
                value={`${sortBy}-${sortOrder}`}
                onChange={(e) => handleSortChange(e.target.value)}
                className="input"
              >
                {sortOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <div className="flex rounded-lg border border-gray-600 overflow-hidden">
                <button
                  onClick={() => setViewMode('grid')}
                  className={`p-2 ${viewMode === 'grid' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:text-white'}`}
                >
                  <Grid className="w-5 h-5" />
                </button>
                <button
                  onClick={() => setViewMode('list')}
                  className={`p-2 ${viewMode === 'list' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:text-white'}`}
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
          </div>

          {showFilters && (
            <div className="mt-6 pt-6 border-t border-gray-600 animate-slide-in">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div>
                  <h3 className="text-lg font-semibold text-white mb-3">Categories</h3>
                  <div className="space-y-2">
                    {categories.map((category: string) => (
                      <button
                        key={category}
                        onClick={() => handleCategoryChange(category)}
                        className={`block w-full text-left px-3 py-2 rounded-lg transition-colors ${(category === 'All' && !selectedCategory) || category === selectedCategory
                          ? 'bg-blue-600 text-white'
                          : 'text-gray-400 hover:text-white hover:bg-gray-700'
                          }`}
                      >
                        {category}
                      </button>
                    ))}
                  </div>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-white mb-3">Price Range</h3>
                  <div className="space-y-3">
                    <div className="flex space-x-2">
                      <input
                        type="number"
                        placeholder="Min"
                        value={priceRange.min}
                        onChange={(e) => setPriceRange(prev => ({ ...prev, min: e.target.value }))}
                        className="input flex-1"
                      />
                      <input
                        type="number"
                        placeholder="Max"
                        value={priceRange.max}
                        onChange={(e) => setPriceRange(prev => ({ ...prev, max: e.target.value }))}
                        className="input flex-1"
                      />
                    </div>
                    <button className="btn-secondary w-full">Apply</button>
                  </div>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-white mb-3">Quick Filters</h3>
                  <div className="space-y-2">
                    <label className="flex items-center space-x-2 text-gray-300 cursor-pointer">
                      <input type="checkbox" className="rounded border-gray-600" />
                      <span>In Stock Only</span>
                    </label>
                    <label className="flex items-center space-x-2 text-gray-300 cursor-pointer">
                      <input type="checkbox" className="rounded border-gray-600" />
                      <span>On Sale</span>
                    </label>
                    <label className="flex items-center space-x-2 text-gray-300 cursor-pointer">
                      <input type="checkbox" className="rounded border-gray-600" />
                      <span>Free Shipping</span>
                    </label>
                    <label className="flex items-center space-x-2 text-gray-300 cursor-pointer">
                      <input type="checkbox" className="rounded border-gray-600" />
                      <span>Top Rated (4.5+)</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </section>
      <section>
        {isLoading ? (
          <div className="text-center py-16">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400 mb-4"></div>
            <p className="text-gray-400">Loading products...</p>
          </div>
        ) : (
          <>
            <div className="flex justify-between items-center mb-6">
              <p className="text-gray-400">
                Showing {data?.products.length || 0} of {data?.totalElements || 0} products
              </p>
            </div>
            {viewMode === 'grid' ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
                {(data || productsData)?.products.map((product: Product, index: number) => (
                  <div
                    key={product.id}
                    className="card-hover p-4 animate-slide-in cursor-pointer"
                    style={{ animationDelay: `${index * 0.05}s` }}
                    onClick={() => handleProductClick(product.id)}
                  >
                    <div className="relative">
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
                          onClick={() => toggleWishlist(product)}
                          className="absolute top-2 right-2 p-2 rounded-full bg-black/50 hover:bg-black/70 transition-colors"
                        >
                          <Heart className="w-4 h-4 text-white" />
                        </button>
                      </div>

                      <div className="space-y-2">
                        <div className="flex justify-between items-start">
                          <span className="text-sm text-blue-400 font-medium">{product.category}</span>
                          <div className="flex items-center space-x-1">
                            <Star className="w-4 h-4 text-yellow-400 fill-current" />
                            <span className="text-sm text-gray-300">{product.rating}</span>
                            <span className="text-xs text-gray-500">({product.reviewCount})</span>
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
                          onClick={() => addToCart(product)}
                          disabled={!product.inStock}
                          className="btn-primary w-full inline-flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          <ShoppingBag className="w-4 h-4" />
                          <span>{product.inStock ? 'Add to Cart' : 'Out of Stock'}</span>
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="space-y-4 mb-8">
                {(data || productsData)?.products.map((product: Product, index: number) => (
                  <div
                    key={product.id}
                    className="card-hover p-6 animate-slide-in cursor-pointer"
                    style={{ animationDelay: `${index * 0.05}s` }}
                    onClick={() => handleProductClick(product.id)}
                  >
                    <div className="flex flex-col md:flex-row gap-6">
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
                            onClick={() => toggleWishlist(product)}
                            className="p-2 rounded-full hover:bg-gray-700 transition-colors"
                          >
                            <Heart className="w-5 h-5 text-gray-400" />
                          </button>
                        </div>

                        <div className="flex items-center space-x-1 mb-3">
                          <Star className="w-4 h-4 text-yellow-400 fill-current" />
                          <span className="text-sm text-gray-300">{product.rating}</span>
                          <span className="text-xs text-gray-500">({product.reviewCount} reviews)</span>
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
                            onClick={() => addToCart(product)}
                            disabled={!product.inStock}
                            className="btn-primary inline-flex items-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            <ShoppingBag className="w-4 h-4" />
                            <span>{product.inStock ? 'Add to Cart' : 'Out of Stock'}</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {data && data.totalPages > 1 && (
              <div className="flex justify-center items-center space-x-2">
                <button
                  onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                  disabled={currentPage === 1}
                  className="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <Minus className="w-4 h-4" />
                </button>

                <div className="flex space-x-1">
                  {Array.from({ length: (data || productsData)?.totalPages || 1 }, (_, i) => i + 1).map(page => (
                    <button
                      key={page}
                      onClick={() => setCurrentPage(page)}
                      className={`px-3 py-2 rounded-lg transition-colors ${page === currentPage
                        ? 'bg-blue-600 text-white'
                        : 'text-gray-400 hover:text-white hover:bg-gray-700'
                        }`}
                    >
                      {page}
                    </button>
                  ))}
                </div>

                <button
                  onClick={() => setCurrentPage(prev => Math.min(prev + 1, (data || productsData)?.totalPages || 1))}
                  disabled={currentPage === ((data || productsData)?.totalPages || 1)}
                  className="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            )}
          </>
        )}
      </section>

      {/* Product Detail Modal */}
      {selectedProductId && (
        <ProductDetail
          productId={selectedProductId}
          isOpen={isModalOpen}
          onClose={handleCloseModal}
        />
      )}
    </div>
  );
};

export default Products;
