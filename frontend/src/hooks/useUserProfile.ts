import { useState, useEffect } from 'react';

import { userAPI, type UserProfileResponse, type UpdateUserProfileRequest } from '../lib/api/user';
import type { ApiError } from '../types/error';

export const useUserProfile = () => {
  const [user, setUser] = useState<UserProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await userAPI.getProfile();
      setUser(data);
      
    } catch (err) {
      const apiError = err as ApiError;
      setError(apiError.response?.data?.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const updateProfile = async (profileData: UpdateUserProfileRequest) => {
    try {
      setError(null);
      const updatedUser = await userAPI.updateProfile(profileData);
      setUser(updatedUser);
      
      localStorage.setItem('user', JSON.stringify({
        id: updatedUser.id,
        email: updatedUser.email,
        firstName: updatedUser.firstName,
        lastName: updatedUser.lastName,
        role: updatedUser.role,
      }));
      
      return updatedUser;
    } catch (err) {
      const apiError = err as ApiError;
      const errorMsg = apiError.response?.data?.message || 'Failed to update profile';
      setError(errorMsg);
      throw new Error(errorMsg);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  return {
    user,
    loading,
    error,
    updateProfile,
    refreshProfile: fetchProfile,
  };
};
