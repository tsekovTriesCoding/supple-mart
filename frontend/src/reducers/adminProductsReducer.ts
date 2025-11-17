import type { AdminProductsState, AdminProductsAction } from '../types/adminProducts';

const defaultFormData = {
  name: '',
  description: '',
  price: 0,
  originalPrice: 0,
  category: '',
  stockQuantity: 0,
  imageUrl: '',
  isActive: true,
};

export const initialState: AdminProductsState = {
  products: [],
  isLoading: true,
  error: null,
  searchQuery: '',
  selectedCategory: 'all',
  currentPage: 1,
  totalPages: 0,
  showModal: false,
  editingProduct: null,
  uploading: false,
  formData: defaultFormData,
};

export function adminProductsReducer(
  state: AdminProductsState,
  action: AdminProductsAction
): AdminProductsState {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    case 'SET_PRODUCTS':
      return {
        ...state,
        products: action.payload.products,
        totalPages: action.payload.totalPages,
        isLoading: false,
      };
    case 'SET_ERROR':
      return { ...state, error: action.payload };
    case 'SET_SEARCH_QUERY':
      return { ...state, searchQuery: action.payload, currentPage: 1 };
    case 'SET_SELECTED_CATEGORY':
      return { ...state, selectedCategory: action.payload, currentPage: 1 };
    case 'SET_CURRENT_PAGE':
      return { ...state, currentPage: action.payload };
    case 'OPEN_MODAL':
      return { ...state, showModal: true };
    case 'CLOSE_MODAL':
      return { 
        ...state, 
        showModal: false, 
        editingProduct: null, 
        formData: defaultFormData,
        error: null,
      };
    case 'START_EDIT':
      return {
        ...state,
        editingProduct: action.payload,
        formData: {
          name: action.payload.name,
          description: action.payload.description,
          price: action.payload.price,
          originalPrice: action.payload.originalPrice || 0,
          category: action.payload.category,
          stockQuantity: action.payload.stockQuantity || 0,
          imageUrl: action.payload.imageUrl || '',
          isActive: action.payload.active,
        },
        showModal: true,
      };
    case 'SET_UPLOADING':
      return { ...state, uploading: action.payload };
    case 'UPDATE_FORM_DATA':
      return { ...state, formData: { ...state.formData, ...action.payload } };
    case 'RESET_FORM':
      return { 
        ...state, 
        formData: defaultFormData, 
        editingProduct: null,
        error: null,
      };
    default:
      return state;
  }
}
