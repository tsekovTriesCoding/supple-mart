import type { AdminProduct, CreateProductRequest } from '../admin';

export type AdminProductsState = {
  products: AdminProduct[];
  isLoading: boolean;
  error: string | null;
  searchQuery: string;
  selectedCategory: string;
  currentPage: number;
  totalPages: number;
  totalElements: number;
  showModal: boolean;
  editingProduct: AdminProduct | null;
  uploading: boolean;
  formData: CreateProductRequest;
};

export type AdminProductsAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_PRODUCTS'; payload: { products: AdminProduct[]; totalPages: number; totalElements: number } }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'SET_SEARCH_QUERY'; payload: string }
  | { type: 'SET_SELECTED_CATEGORY'; payload: string }
  | { type: 'SET_CURRENT_PAGE'; payload: number }
  | { type: 'OPEN_MODAL' }
  | { type: 'CLOSE_MODAL' }
  | { type: 'START_EDIT'; payload: AdminProduct }
  | { type: 'SET_UPLOADING'; payload: boolean }
  | { type: 'UPDATE_FORM_DATA'; payload: Partial<CreateProductRequest> }
  | { type: 'RESET_FORM' };
