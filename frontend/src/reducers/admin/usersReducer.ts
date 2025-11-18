import type { AdminUsersState, AdminUsersAction } from '../../types/admin/users';

export const initialState: AdminUsersState = {
  users: [],
  loading: true,
  currentPage: 1,
  totalPages: 0,
  totalElements: 0,
  searchQuery: '',
  roleFilter: 'all',
};

export function adminUsersReducer(
  state: AdminUsersState,
  action: AdminUsersAction
): AdminUsersState {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    case 'SET_USERS':
      return {
        ...state,
        users: action.payload.users,
        totalPages: action.payload.totalPages,
        totalElements: action.payload.totalElements,
        loading: false,
      };
    case 'SET_CURRENT_PAGE':
      return { ...state, currentPage: action.payload };
    case 'SET_SEARCH_QUERY':
      return { ...state, searchQuery: action.payload, currentPage: 1 };
    case 'SET_ROLE_FILTER':
      return { ...state, roleFilter: action.payload };
    default:
      return state;
  }
}
