import type { AdminOrdersState, AdminOrdersAction } from '../../types/admin/orders';

export const initialState: AdminOrdersState = {
  orders: [],
  loading: true,
  currentPage: 1,
  totalPages: 0,
  totalElements: 0,
  selectedStatus: 'all',
  selectedOrder: null,
  showDetailsModal: false,
};

export function adminOrdersReducer(
  state: AdminOrdersState,
  action: AdminOrdersAction
): AdminOrdersState {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    case 'SET_ORDERS':
      return {
        ...state,
        orders: action.payload.orders,
        totalPages: action.payload.totalPages,
        totalElements: action.payload.totalElements,
        loading: false,
      };
    case 'SET_CURRENT_PAGE':
      return { ...state, currentPage: action.payload };
    case 'SET_SELECTED_STATUS':
      return { ...state, selectedStatus: action.payload, currentPage: 1 };
    case 'OPEN_DETAILS_MODAL':
      return { ...state, selectedOrder: action.payload, showDetailsModal: true };
    case 'CLOSE_DETAILS_MODAL':
      return { ...state, selectedOrder: null, showDetailsModal: false };
    case 'RESET_TO_PAGE_ONE':
      return { ...state, currentPage: 1 };
    default:
      return state;
  }
}
