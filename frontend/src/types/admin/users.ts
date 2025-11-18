import type { AdminUser } from '../admin';

export type AdminUsersState = {
  users: AdminUser[];
  loading: boolean;
  currentPage: number;
  totalPages: number;
  totalElements: number;
  searchQuery: string;
  roleFilter: string;
};

export type AdminUsersAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_USERS'; payload: { users: AdminUser[]; totalPages: number; totalElements: number } }
  | { type: 'SET_CURRENT_PAGE'; payload: number }
  | { type: 'SET_SEARCH_QUERY'; payload: string }
  | { type: 'SET_ROLE_FILTER'; payload: string };
