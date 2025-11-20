import type { AccountState, AccountAction } from '../types/account';

export const initialAccountState: AccountState = {
  isEditing: false,
  formData: {
    firstName: '',
    lastName: '',
  },
  updateError: null,
  updateSuccess: false,
  isUpdating: false,
  showPasswordModal: false,
};

export const accountReducer = (state: AccountState, action: AccountAction): AccountState => {
  switch (action.type) {
    case 'START_EDITING':
      return {
        ...state,
        isEditing: true,
        formData: action.payload,
        updateError: null,
        updateSuccess: false,
      };
    case 'CANCEL_EDITING':
      return {
        ...state,
        isEditing: false,
        updateError: null,
        updateSuccess: false,
      };
    case 'UPDATE_FORM':
      return {
        ...state,
        formData: { ...state.formData, ...action.payload },
      };
    case 'UPDATE_START':
      return {
        ...state,
        isUpdating: true,
        updateError: null,
        updateSuccess: false,
      };
    case 'UPDATE_SUCCESS':
      return {
        ...state,
        isUpdating: false,
        updateSuccess: true,
        isEditing: false,
      };
    case 'UPDATE_ERROR':
      return {
        ...state,
        isUpdating: false,
        updateError: action.payload,
      };
    case 'CLEAR_SUCCESS':
      return {
        ...state,
        updateSuccess: false,
      };
    case 'OPEN_PASSWORD_MODAL':
      return {
        ...state,
        showPasswordModal: true,
      };
    case 'CLOSE_PASSWORD_MODAL':
      return {
        ...state,
        showPasswordModal: false,
      };
    default:
      return state;
  }
};
