import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';

import { useCart } from './useCart';
import { useProducts, useProductCategories, type Product } from './useProducts';
import {
  formatCategoryFromUrl,
  formatCategoryForUrl,
  urlCategoryToBackend,
} from '../utils/categoryUtils';

export const useProductsPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { addItem } = useCart();

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
  const [loadingProductId, setLoadingProductId] = useState<number | null>(null);
  const [addingToCartId, setAddingToCartId] = useState<number | null>(null);

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
    sortBy,
    sortOrder,
  });

  const { data: categoriesData } = useProductCategories();
  const categories = ['all', ...(categoriesData || [])];

  const handleCategoryChange = (category: string) => {
    const newCategory = category === 'all' ? '' : category;
    setSelectedCategory(newCategory);
    setCurrentPage(1);

    const newSearchParams = new URLSearchParams(searchParams);
    if (newCategory) {
      newSearchParams.set('category', formatCategoryForUrl(newCategory));
    } else {
      newSearchParams.delete('category');
    }
    setSearchParams(newSearchParams);
  };

  const handleSortChange = (sortValue: string) => {
    const [field, order] = sortValue.split('-') as [
      'name' | 'price' | 'createdAt',
      'asc' | 'desc'
    ];
    setSortBy(field);
    setSortOrder(order);
    setCurrentPage(1);
  };

  const handleSearchChange = (query: string) => {
    setSearchQuery(query);
    setCurrentPage(1);

    const newSearchParams = new URLSearchParams(searchParams);
    if (query) {
      newSearchParams.set('search', query);
    } else {
      newSearchParams.delete('search');
    }
    setSearchParams(newSearchParams);
  };

  const handleProductClick = (productId: number) => {
    setLoadingProductId(productId);
    setSelectedProductId(productId);
    setIsModalOpen(true);

    setTimeout(() => {
      setLoadingProductId(null);
    }, 500);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedProductId(null);
    setLoadingProductId(null);
  };

  const addToCart = async (product: Product) => {
    if (!product.inStock) return;

    setAddingToCartId(product.id);
    try {
      await addItem(product.id.toString(), 1);
      console.log(`Added ${product.name} to cart`);
    } catch (error) {
      console.error('Failed to add item to cart:', error);
    } finally {
      setAddingToCartId(null);
    }
  };

  const toggleWishlist = (product: Product) => {
    console.log('Toggle wishlist:', product);
  };

  return {
    currentPage,
    selectedCategory,
    searchQuery,
    viewMode,
    showFilters,
    priceRange,
    sortBy,
    sortOrder,
    selectedProductId,
    isModalOpen,
    loadingProductId,
    addingToCartId,
    categories,
    
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
  };
};
