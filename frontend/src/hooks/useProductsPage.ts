import { useReducer, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';

import { useCart } from './useCart';
import { useProducts, useProductCategories, type Product } from './useProducts';
import {
  formatCategoryFromUrl,
  formatCategoryForUrl,
  urlCategoryToBackend,
} from '../utils/categoryUtils';
import { productsPageReducer, initialState } from '../reducers/productsPageReducer';

export const useProductsPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { addItem } = useCart();
  const [state, dispatch] = useReducer(productsPageReducer, initialState);

  useEffect(() => {
    const categoryFromUrl = searchParams.get('category');
    const searchFromUrl = searchParams.get('search');

    if (categoryFromUrl || searchFromUrl) {
      dispatch({
        type: 'INIT_FROM_URL',
        payload: {
          category: categoryFromUrl ? formatCategoryFromUrl(categoryFromUrl) : undefined,
          search: searchFromUrl || undefined,
        },
      });
    }
  }, [searchParams]);

  const { data, isLoading, isError, error } = useProducts({
    page: state.currentPage,
    limit: 12,
    category: urlCategoryToBackend(formatCategoryForUrl(state.selectedCategory)),
    search: state.searchQuery || undefined,
    minPrice: state.priceRange.min ? parseFloat(state.priceRange.min) : undefined,
    maxPrice: state.priceRange.max ? parseFloat(state.priceRange.max) : undefined,
    sortBy: state.sortBy,
    sortOrder: state.sortOrder,
  });

  const { data: categoriesData } = useProductCategories();
  const categories = ['all', ...(categoriesData || [])];

  const handleCategoryChange = (category: string) => {
    const newCategory = category === 'all' ? '' : category;
    dispatch({ type: 'SET_SELECTED_CATEGORY', payload: newCategory });

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
    dispatch({ type: 'SET_SORT', payload: { sortBy: field, sortOrder: order } });
  };

  const handleSearchChange = (query: string) => {
    dispatch({ type: 'SET_SEARCH_QUERY', payload: query });

    const newSearchParams = new URLSearchParams(searchParams);
    if (query) {
      newSearchParams.set('search', query);
    } else {
      newSearchParams.delete('search');
    }
    setSearchParams(newSearchParams);
  };

  const handleProductClick = (productId: number) => {
    dispatch({ type: 'OPEN_PRODUCT_MODAL', payload: productId });

    setTimeout(() => {
      dispatch({ type: 'SET_LOADING_PRODUCT_ID', payload: null });
    }, 500);
  };

  const handleCloseModal = () => {
    dispatch({ type: 'CLOSE_PRODUCT_MODAL' });
  };

  const addToCart = async (product: Product) => {
    if (!product.inStock) return;

    dispatch({ type: 'SET_ADDING_TO_CART_ID', payload: product.id });
    try {
      await addItem(product.id.toString(), 1);
      console.log(`Added ${product.name} to cart`);
    } catch (error) {
      console.error('Failed to add item to cart:', error);
    } finally {
      dispatch({ type: 'SET_ADDING_TO_CART_ID', payload: null });
    }
  };

  const toggleWishlist = (product: Product) => {
    console.log('Toggle wishlist:', product);
  };

  return {
    currentPage: state.currentPage,
    selectedCategory: state.selectedCategory,
    searchQuery: state.searchQuery,
    viewMode: state.viewMode,
    showFilters: state.showFilters,
    priceRange: state.priceRange,
    sortBy: state.sortBy,
    sortOrder: state.sortOrder,
    selectedProductId: state.selectedProductId,
    isModalOpen: state.isModalOpen,
    loadingProductId: state.loadingProductId,
    addingToCartId: state.addingToCartId,
    categories,
    
    data,
    isLoading,
    isError,
    error,
    
    setCurrentPage: (page: number) => dispatch({ type: 'SET_CURRENT_PAGE', payload: page }),
    setViewMode: (mode: 'grid' | 'list') => dispatch({ type: 'SET_VIEW_MODE', payload: mode }),
    setShowFilters: (show: boolean) => dispatch({ type: 'SET_SHOW_FILTERS', payload: show }),
    setPriceRange: (range: { min: string; max: string }) => dispatch({ type: 'SET_PRICE_RANGE', payload: range }),
    handleCategoryChange,
    handleSortChange,
    handleSearchChange,
    handleProductClick,
    handleCloseModal,
    addToCart,
    toggleWishlist,
  };
};
