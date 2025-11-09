import { Filter, Grid, List, Search } from 'lucide-react';

import ProductDetail from '../components/ProductDetail';
import { Pagination } from '../components/Pagination';
import { ProductCard } from '../components/ProductCard';
import { ProductFilters } from '../components/ProductFilters';
import { ProductListItem } from '../components/ProductListItem';
import { useProductsPage } from '../hooks/useProductsPage';
import type { Product } from '../hooks/useProducts';
import type { ApiError } from '../types/error';

const Products = () => {
  const {
    currentPage,
    searchQuery,
    viewMode,
    showFilters,
    priceRange,
    loadingProductId,
    addingToCartId,
    categories,
    selectedProductId,
    isModalOpen,
    
    data,
    isLoading,
    isError,
    error,
    
    setCurrentPage,
    setViewMode,
    setShowFilters,
    setPriceRange,
    handleCategoryChange,
    handleSortChange,
    handleSearchChange,
    handleProductClick,
    handleCloseModal,
    addToCart,
    toggleWishlist,
  } = useProductsPage();

  const sortOptions = [
    { value: 'name-asc', label: 'Name A-Z' },
    { value: 'name-desc', label: 'Name Z-A' },
    { value: 'price-asc', label: 'Price: Low to High' },
    { value: 'price-desc', label: 'Price: High to Low' },
    { value: 'createdAt-desc', label: 'Newest' },
    { value: 'createdAt-asc', label: 'Oldest' },
  ];

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
                className={`p-2 rounded-lg transition-colors ${
                  viewMode === 'grid'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-800 text-gray-400 hover:text-white'
                }`}
              >
                <Grid className="w-5 h-5" />
              </button>
              <button
                onClick={() => setViewMode('list')}
                className={`p-2 rounded-lg transition-colors ${
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
              selectedCategory={categories[0] || ''}
              priceRange={priceRange}
              onCategoryChange={handleCategoryChange}
              onPriceRangeChange={setPriceRange}
            />
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
                Showing{' '}
                {data?.products.length ? (currentPage - 1) * 12 + 1 : 0}-
                {Math.min(currentPage * 12, data?.totalElements || 0)} of{' '}
                {data?.totalElements || 0} products
              </p>
            </div>

            {viewMode === 'grid' ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
                {data?.products.map((product: Product, index: number) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    onProductClick={handleProductClick}
                    onAddToCart={addToCart}
                    onToggleWishlist={toggleWishlist}
                    isLoading={loadingProductId === product.id}
                    isAddingToCart={addingToCartId === product.id}
                    animationDelay={index * 0.05}
                  />
                ))}
              </div>
            ) : (
              <div className="space-y-4 mb-8">
                {data?.products.map((product: Product, index: number) => (
                  <ProductListItem
                    key={product.id}
                    product={product}
                    onProductClick={handleProductClick}
                    onAddToCart={addToCart}
                    onToggleWishlist={toggleWishlist}
                    isLoading={loadingProductId === product.id}
                    isAddingToCart={addingToCartId === product.id}
                    animationDelay={index * 0.05}
                  />
                ))}
              </div>
            )}

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
