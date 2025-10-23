import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';
import { Star, Filter, Grid, List, Search, ShoppingBag, Heart, Plus, Minus } from 'lucide-react';

interface Product {
  id: number;
  name: string;
  price: number;
  originalPrice?: number;
  rating: number;
  reviewCount: number;
  image: string;
  category: string;
  brand: string;
  inStock: boolean;
  description: string;
  tags: string[];
}

interface ProductsResponse {
  products: Product[];
  total: number;
  page: number;
  totalPages: number;
}

const fetchProducts = async (page: number = 1, category?: string, search?: string): Promise<ProductsResponse> => {
  try {
    const params = new URLSearchParams({
      page: page.toString(),
      limit: '12',
      ...(category && { category }),
      ...(search && { search })
    });

    const response = await api.get(`/products?${params}`);
    return response.data;
  } catch (error) {
    console.warn('API not available, using mock data:', error);
    return mockProductsResponse(page, category, search);
  }
};

const mockProductsResponse = (page: number, category?: string, search?: string): ProductsResponse => {
  const allProducts: Product[] = [
    {
      id: 1,
      name: "Premium Whey Protein Isolate",
      price: 49.99,
      originalPrice: 59.99,
      rating: 4.8,
      reviewCount: 256,
      image: "https://images.unsplash.com/photo-1593095948071-474c5cc2989d?w=300&h=300&fit=crop&crop=center",
      category: "Protein",
      brand: "NutritionPro",
      inStock: true,
      description: "High-quality whey protein isolate for muscle building and recovery. 25g protein per serving.",
      tags: ["muscle-building", "recovery", "low-carb"]
    },
    {
      id: 2,
      name: "Organic Multivitamin Complex",
      price: 29.99,
      rating: 4.6,
      reviewCount: 189,
      image: "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=300&h=300&fit=crop&crop=center",
      category: "Vitamins",
      brand: "GreenLife",
      inStock: true,
      description: "Complete daily vitamin and mineral support from organic sources.",
      tags: ["organic", "daily-health", "immune-support"]
    },
    {
      id: 3,
      name: "Pre-Workout Energy Booster",
      price: 39.99,
      originalPrice: 44.99,
      rating: 4.7,
      reviewCount: 342,
      image: "https://images.unsplash.com/photo-1550572017-edd951b55104?w=300&h=300&fit=crop&crop=center",
      category: "Pre-Workout",
      brand: "PowerFuel",
      inStock: true,
      description: "Natural energy boost with caffeine, B-vitamins, and amino acids for enhanced performance.",
      tags: ["energy", "performance", "focus"]
    },
    {
      id: 4,
      name: "Omega-3 Fish Oil Capsules",
      price: 24.99,
      rating: 4.5,
      reviewCount: 167,
      image: "https://images.unsplash.com/photo-1559181567-c3190ca9959b?w=300&h=300&fit=crop&crop=center",
      category: "Supplements",
      brand: "OceanPure",
      inStock: true,
      description: "High-potency omega-3 fatty acids for heart and brain health.",
      tags: ["heart-health", "brain-health", "anti-inflammatory"]
    },
    {
      id: 5,
      name: "Creatine Monohydrate Powder",
      price: 19.99,
      rating: 4.9,
      reviewCount: 423,
      image: "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=300&h=300&fit=crop&crop=center",
      category: "Supplements",
      brand: "StrengthMax",
      inStock: true,
      description: "Pure creatine monohydrate for increased strength and muscle mass.",
      tags: ["strength", "muscle-building", "power"]
    },
    {
      id: 6,
      name: "Post-Workout Recovery Formula",
      price: 34.99,
      rating: 4.4,
      reviewCount: 98,
      image: "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=300&h=300&fit=crop&crop=center",
      category: "Recovery",
      brand: "RecoverFast",
      inStock: false,
      description: "Advanced recovery blend with BCAAs, glutamine, and electrolytes.",
      tags: ["recovery", "muscle-repair", "hydration"]
    },
    {
      id: 7,
      name: "Vitamin D3 + K2 Complex",
      price: 22.99,
      rating: 4.7,
      reviewCount: 134,
      image: "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=300&h=300&fit=crop&crop=center",
      category: "Vitamins",
      brand: "VitalHealth",
      inStock: true,
      description: "Essential vitamin D3 and K2 for bone health and immune support.",
      tags: ["bone-health", "immune-support", "vitamin-d"]
    },
    {
      id: 8,
      name: "Green Superfood Powder",
      price: 42.99,
      originalPrice: 49.99,
      rating: 4.3,
      reviewCount: 76,
      image: "https://images.unsplash.com/photo-1551782450-a2132b4ba21d?w=300&h=300&fit=crop&crop=center",
      category: "Supplements",
      brand: "GreenVitality",
      inStock: true,
      description: "Blend of organic greens, fruits, and vegetables for daily nutrition.",
      tags: ["organic", "antioxidants", "detox"]
    }
  ];

  let filteredProducts = category
    ? allProducts.filter(p => p.category.toLowerCase() === category.toLowerCase())
    : allProducts;

  if (search) {
    filteredProducts = filteredProducts.filter(p =>
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.description.toLowerCase().includes(search.toLowerCase()) ||
      p.brand.toLowerCase().includes(search.toLowerCase())
    );
  }

  const limit = 12;
  const startIndex = (page - 1) * limit;
  const endIndex = startIndex + limit;
  const paginatedProducts = filteredProducts.slice(startIndex, endIndex);

  return {
    products: paginatedProducts,
    total: filteredProducts.length,
    page,
    totalPages: Math.ceil(filteredProducts.length / limit)
  };
};

const Products = () => {
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [priceRange, setPriceRange] = useState({ min: '', max: '' });
  const [sortBy, setSortBy] = useState('name');

  const { data, isLoading, isError } = useQuery<ProductsResponse>({
    queryKey: ['products', currentPage, selectedCategory, searchQuery],
    queryFn: () => fetchProducts(currentPage, selectedCategory, searchQuery),
    placeholderData: (previousData) => previousData,
  });

  const categories = [
    'All',
    'Protein',
    'Creatine',
    'Pre-Workout',
    'Amino Acids',
    'Vitamins',
    'Weight Loss'
  ];

  const sortOptions = [
    { value: 'name', label: 'Name A-Z' },
    { value: 'price-low', label: 'Price: Low to High' },
    { value: 'price-high', label: 'Price: High to Low' },
    { value: 'rating', label: 'Highest Rated' },
    { value: 'newest', label: 'Newest' }
  ];

  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category === 'All' ? '' : category);
    setCurrentPage(1);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(1);
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
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search products..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="input pl-12 pr-4 w-full"
                />
              </div>
            </form>

            <div className="flex items-center space-x-4">
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
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
                    {categories.map(category => (
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
                Showing {data?.products.length || 0} of {data?.total || 0} products
              </p>
            </div>
            {viewMode === 'grid' ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
                {data?.products.map((product, index) => (
                  <div key={product.id} className="card-hover p-4 animate-slide-in" style={{ animationDelay: `${index * 0.05}s` }}>
                    <div className="relative">
                      <div className="aspect-square bg-gray-800 rounded-lg mb-4 overflow-hidden relative">
                        <img
                          src={product.image}
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
                {data?.products.map((product, index) => (
                  <div key={product.id} className="card-hover p-6 animate-slide-in" style={{ animationDelay: `${index * 0.05}s` }}>
                    <div className="flex flex-col md:flex-row gap-6">
                      <div className="w-full md:w-48 h-48 bg-gray-800 rounded-lg overflow-hidden relative shrink-0">
                        <img
                          src={product.image}
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
                  {Array.from({ length: data.totalPages }, (_, i) => i + 1).map(page => (
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
                  onClick={() => setCurrentPage(prev => Math.min(prev + 1, data.totalPages))}
                  disabled={currentPage === data.totalPages}
                  className="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            )}
          </>
        )}
      </section>
    </div>
  );
};

export default Products;
