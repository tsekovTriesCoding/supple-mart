import { api } from './index';
import type { User } from '../../types/auth';

export interface UpdateUserProfileRequest {
  firstName: string;
  lastName: string;
  // email: string; // Email changes not allowed - must contact support... for now
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UserProfileResponse extends User {
  totalOrders?: number;
  // I can add more fields if needed to display
}

export const userAPI = {
  getProfile: async (): Promise<UserProfileResponse> => {
    const { data } = await api.get<UserProfileResponse>('/user/profile');
    return data;
  },

  updateProfile: async (profileData: UpdateUserProfileRequest): Promise<UserProfileResponse> => {
    const { data } = await api.put<UserProfileResponse>('/user/profile', profileData);
    return data;
  },

  changePassword: async (passwordData: ChangePasswordRequest): Promise<void> => {
    await api.put('/user/change-password', passwordData);
  },

  deleteAccount: async (): Promise<void> => {
    await api.delete('/user/account');
  },
};
