import type { ProductsPageState, ProductsPageAction } from '../types/productsPage';

export const initialState: ProductsPageState = {
  currentPage: 1,
  selectedCategory: '',
  searchQuery: '',
  viewMode: 'grid',
  showFilters: false,
  priceRange: { min: '', max: '' },
  sortBy: 'name',
  sortOrder: 'asc',
  selectedProductId: null,
  isModalOpen: false,
  loadingProductId: null,
  addingToCartId: null,
};

export function productsPageReducer(
  state: ProductsPageState,
  action: ProductsPageAction
): ProductsPageState {
  switch (action.type) {
    case 'SET_CURRENT_PAGE':
      return { ...state, currentPage: action.payload };
    case 'SET_SELECTED_CATEGORY':
      return { ...state, selectedCategory: action.payload, currentPage: 1 };
    case 'SET_SEARCH_QUERY':
      return { ...state, searchQuery: action.payload, currentPage: 1 };
    case 'SET_VIEW_MODE':
      return { ...state, viewMode: action.payload };
    case 'SET_SHOW_FILTERS':
      return { ...state, showFilters: action.payload };
    case 'SET_PRICE_RANGE':
      return { ...state, priceRange: action.payload };
    case 'SET_SORT':
      return { ...state, sortBy: action.payload.sortBy, sortOrder: action.payload.sortOrder, currentPage: 1 };
    case 'OPEN_PRODUCT_MODAL':
      return { ...state, selectedProductId: action.payload, isModalOpen: true, loadingProductId: action.payload };
    case 'CLOSE_PRODUCT_MODAL':
      return { ...state, isModalOpen: false, selectedProductId: null, loadingProductId: null };
    case 'SET_LOADING_PRODUCT_ID':
      return { ...state, loadingProductId: action.payload };
    case 'SET_ADDING_TO_CART_ID':
      return { ...state, addingToCartId: action.payload };
    case 'INIT_FROM_URL':
      return {
        ...state,
        selectedCategory: action.payload.category || '',
        searchQuery: action.payload.search || '',
      };
    default:
      return state;
  }
}
