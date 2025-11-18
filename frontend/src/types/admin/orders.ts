import type { AdminOrder } from "../admin";

export type AdminOrdersState = {
  orders: AdminOrder[];
  loading: boolean;
  currentPage: number;
  totalPages: number;
  totalElements: number;
  selectedStatus: string;
  selectedOrder: AdminOrder | null;
  showDetailsModal: boolean;
};

export type AdminOrdersAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ORDERS'; payload: { orders: AdminOrder[]; totalPages: number; totalElements: number } }
  | { type: 'SET_CURRENT_PAGE'; payload: number }
  | { type: 'SET_SELECTED_STATUS'; payload: string }
  | { type: 'OPEN_DETAILS_MODAL'; payload: AdminOrder }
  | { type: 'CLOSE_DETAILS_MODAL' }
  | { type: 'RESET_TO_PAGE_ONE' };
