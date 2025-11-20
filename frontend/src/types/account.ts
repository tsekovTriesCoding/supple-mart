export interface AccountFormData {
  firstName: string;
  lastName: string;
}

export interface AccountState {
  isEditing: boolean;
  formData: AccountFormData;
  updateError: string | null;
  updateSuccess: boolean;
  isUpdating: boolean;
  showPasswordModal: boolean;
}

export type AccountAction =
  | { type: 'START_EDITING'; payload: AccountFormData }
  | { type: 'CANCEL_EDITING' }
  | { type: 'UPDATE_FORM'; payload: Partial<AccountFormData> }
  | { type: 'UPDATE_START' }
  | { type: 'UPDATE_SUCCESS' }
  | { type: 'UPDATE_ERROR'; payload: string }
  | { type: 'CLEAR_SUCCESS' }
  | { type: 'OPEN_PASSWORD_MODAL' }
  | { type: 'CLOSE_PASSWORD_MODAL' };
