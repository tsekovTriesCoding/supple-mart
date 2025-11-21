import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

import { userAPI, type UserProfileResponse, type UpdateUserProfileRequest } from '../lib/api/user';
import type { ApiError } from '../types/error';

const USER_PROFILE_QUERY_KEY = 'user-profile';

export const useUserProfile = () => {
  const queryClient = useQueryClient();

  const {
    data: user = null,
    isLoading: loading,
    error: queryError,
  } = useQuery({
    queryKey: [USER_PROFILE_QUERY_KEY],
    queryFn: userAPI.getProfile,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
    retry: 1,
  });

  const updateMutation = useMutation({
    mutationFn: (profileData: UpdateUserProfileRequest) => userAPI.updateProfile(profileData),
    onSuccess: (updatedUser) => {
      queryClient.setQueryData<UserProfileResponse>([USER_PROFILE_QUERY_KEY], updatedUser);

      localStorage.setItem(
        'user',
        JSON.stringify({
          id: updatedUser.id,
          email: updatedUser.email,
          firstName: updatedUser.firstName,
          lastName: updatedUser.lastName,
          role: updatedUser.role,
        })
      );
    },
  });

  const error = queryError
    ? ((queryError as ApiError).response?.data?.message || 'Failed to load profile')
    : null;

  return {
    user,
    loading,
    error,
    updateProfile: updateMutation.mutateAsync,
    refreshProfile: () => queryClient.invalidateQueries({ queryKey: [USER_PROFILE_QUERY_KEY] }),
  };
};
