export type ProductsPageState = {
  currentPage: number;
  selectedCategory: string;
  searchQuery: string;
  viewMode: 'grid' | 'list';
  showFilters: boolean;
  priceRange: { min: string; max: string };
  sortBy: 'name' | 'price' | 'createdAt';
  sortOrder: 'asc' | 'desc';
  selectedProductId: number | null;
  isModalOpen: boolean;
  loadingProductId: number | null;
  addingToCartId: number | null;
};

export type ProductsPageAction =
  | { type: 'SET_CURRENT_PAGE'; payload: number }
  | { type: 'SET_SELECTED_CATEGORY'; payload: string }
  | { type: 'SET_SEARCH_QUERY'; payload: string }
  | { type: 'SET_VIEW_MODE'; payload: 'grid' | 'list' }
  | { type: 'SET_SHOW_FILTERS'; payload: boolean }
  | { type: 'SET_PRICE_RANGE'; payload: { min: string; max: string } }
  | { type: 'SET_SORT'; payload: { sortBy: 'name' | 'price' | 'createdAt'; sortOrder: 'asc' | 'desc' } }
  | { type: 'OPEN_PRODUCT_MODAL'; payload: number }
  | { type: 'CLOSE_PRODUCT_MODAL' }
  | { type: 'SET_LOADING_PRODUCT_ID'; payload: number | null }
  | { type: 'SET_ADDING_TO_CART_ID'; payload: number | null }
  | { type: 'INIT_FROM_URL'; payload: { category?: string; search?: string } };
