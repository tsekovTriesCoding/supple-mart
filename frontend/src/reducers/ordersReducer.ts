import type { OrdersState, OrdersAction, OrderFilters } from '../types/order';

export const createInitialOrdersState = (initialFilters?: OrderFilters): OrdersState => ({
  orders: [],
  stats: null,
  loading: true,
  statsLoading: true,
  error: null,
  totalElements: 0,
  totalPages: 0,
  currentPage: 1,
  filters: initialFilters || {},
});

export const ordersReducer = (state: OrdersState, action: OrdersAction): OrdersState => {
  switch (action.type) {
    case 'FETCH_START':
      return {
        ...state,
        loading: true,
        error: null,
      };
    case 'FETCH_SUCCESS':
      return {
        ...state,
        loading: false,
        orders: action.payload.orders,
        totalElements: action.payload.totalElements,
        totalPages: action.payload.totalPages,
        currentPage: action.payload.currentPage,
        error: null,
      };
    case 'FETCH_ERROR':
      return {
        ...state,
        loading: false,
        error: action.payload,
      };
    case 'STATS_FETCH_START':
      return {
        ...state,
        statsLoading: true,
      };
    case 'STATS_FETCH_SUCCESS':
      return {
        ...state,
        statsLoading: false,
        stats: action.payload,
      };
    case 'STATS_FETCH_ERROR':
      return {
        ...state,
        statsLoading: false,
      };
    case 'UPDATE_ORDER':
      return {
        ...state,
        orders: state.orders.map(order =>
          order.id === action.payload.id ? action.payload : order
        ),
      };
    case 'SET_FILTERS':
      return {
        ...state,
        filters: { ...state.filters, ...action.payload },
      };
    case 'CLEAR_ERROR':
      return {
        ...state,
        error: null,
      };
    default:
      return state;
  }
};
