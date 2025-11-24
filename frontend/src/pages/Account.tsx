import { useState } from 'react';
import { User, Mail, Calendar, AlertCircle } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

import { userAPI, type UpdateUserProfileRequest } from '../lib/api/user';
import type { ApiError } from '../types/error';
import { PasswordChangeModal } from '../components/PasswordChangeModal';
import { LoadingSpinner } from '../components/LoadingSpinner';

const Account = () => {
  const queryClient = useQueryClient();
  
  const {
    data: user = null,
    isLoading,
    error: queryError,
  } = useQuery({
    queryKey: ['user-profile'],
    queryFn: userAPI.getProfile,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
    retry: 1,
  });

  const updateProfileMutation = useMutation({
    mutationFn: (profileData: UpdateUserProfileRequest) => userAPI.updateProfile(profileData),
    onSuccess: (updatedUser) => {
      queryClient.setQueryData(['user-profile'], updatedUser);
      
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
      
      setIsEditing(false);
    }
  });

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({ firstName: '', lastName: '' });
  const [showPasswordModal, setShowPasswordModal] = useState(false);

  const error = queryError
    ? ((queryError as ApiError).response?.data?.message || 'Failed to load profile')
    : null;
  const updateError = updateProfileMutation.error
    ? ((updateProfileMutation.error as ApiError).response?.data?.message || 'Failed to update profile')
    : null;

  const handleEdit = () => {
    if (user) {
      setFormData({
        firstName: user.firstName,
        lastName: user.lastName,
      });
      setIsEditing(true);
      updateProfileMutation.reset();
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    updateProfileMutation.reset();
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfileMutation.mutate(formData);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <LoadingSpinner size="lg" message="Loading your profile..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-white mb-2">Error Loading Profile</h1>
          <p className="text-gray-400">{error}</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-white mb-4">Please Sign In</h1>
          <p className="text-gray-400">You need to be logged in to access your account.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center justify-between mb-8">
            <h1 className="text-3xl font-bold text-white">My Account</h1>
            {user.totalOrders !== undefined && (
              <div className="text-gray-400 text-sm">
                Total Orders: <span className="text-white font-semibold">{user.totalOrders}</span>
              </div>
            )}
          </div>

          {updateProfileMutation.isSuccess && !isEditing && (
            <div className="mb-6 p-4 bg-green-900/20 border border-green-900 rounded-lg text-green-400 flex items-center">
              <AlertCircle className="w-5 h-5 mr-2" />
              Profile updated successfully!
            </div>
          )}

          {updateError && (
            <div className="mb-6 p-4 bg-red-900/20 border border-red-900 rounded-lg text-red-400 flex items-center">
              <AlertCircle className="w-5 h-5 mr-2" />
              {updateError}
            </div>
          )}
          
          <div className="grid gap-6 md:grid-cols-2">
            <div className="card p-6">
              <h2 className="text-xl font-semibold text-white mb-4 flex items-center">
                <User className="w-5 h-5 mr-2 text-blue-400" />
                Profile Information
              </h2>

              {!isEditing ? (
                <>
                  <div className="space-y-4">
                    <div className="flex items-center space-x-3">
                      <User className="w-5 h-5 text-gray-400" />
                      <div>
                        <p className="text-sm text-gray-400">Full Name</p>
                        <p className="text-white">{user.firstName} {user.lastName}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-center space-x-3">
                      <Mail className="w-5 h-5 text-gray-400" />
                      <div>
                        <p className="text-sm text-gray-400">Email</p>
                        <p className="text-white">{user.email}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-center space-x-3">
                      <User className="w-5 h-5 text-gray-400" />
                      <div>
                        <p className="text-sm text-gray-400">Role</p>
                        <p className="text-white">{user.role}</p>
                      </div>
                    </div>

                    {user.createdAt && (
                      <div className="flex items-center space-x-3">
                        <Calendar className="w-5 h-5 text-gray-400" />
                        <div>
                          <p className="text-sm text-gray-400">Member Since</p>
                          <p className="text-white">{new Date(user.createdAt).toLocaleDateString()}</p>
                        </div>
                      </div>
                    )}
                  </div>
                  
                  <button 
                    onClick={handleEdit}
                    className="mt-6 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
                  >
                    Edit Profile
                  </button>
                </>
              ) : (
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <label className="block text-sm text-gray-400 mb-2">First Name</label>
                    <input
                      type="text"
                      value={formData.firstName}
                      onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                      className="input w-full"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm text-gray-400 mb-2">Last Name</label>
                    <input
                      type="text"
                      value={formData.lastName}
                      onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                      className="input w-full"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm text-gray-400 mb-2">Email</label>
                    <input
                      type="email"
                      value={user.email}
                      className="input w-full bg-gray-800 cursor-not-allowed opacity-60"
                      disabled
                    />
                    <p className="text-xs text-gray-400 mt-1 flex items-center">
                      <AlertCircle className="w-3 h-3 mr-1" />
                      Email cannot be changed. Contact support if you need to update your email address.
                    </p>
                  </div>

                  <div className="flex space-x-3">
                    <button
                      type="submit"
                      disabled={updateProfileMutation.isPending}
                      className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                    >
                      {updateProfileMutation.isPending ? 'Saving...' : 'Save Changes'}
                    </button>
                    <button
                      type="button"
                      onClick={handleCancel}
                      disabled={updateProfileMutation.isPending}
                      className="flex-1 px-4 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              )}
            </div>
            
            <div className="card p-6">
              <h2 className="text-xl font-semibold text-white mb-4">Account Settings</h2>
              
              <div className="space-y-3">
                <button 
                  onClick={() => setShowPasswordModal(true)}
                  className="w-full text-left p-3 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors cursor-pointer"
                >
                  <h3 className="text-white font-medium">Change Password</h3>
                  <p className="text-gray-400 text-sm">Update your account password</p>
                </button>
                
                <button 
                  onClick={() => window.location.href = '/account/notifications'}
                  className="w-full text-left p-3 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors cursor-pointer"
                >
                  <h3 className="text-white font-medium">Notification Preferences</h3>
                  <p className="text-gray-400 text-sm">Manage email and push notifications</p>
                </button>
                
                <button className="w-full text-left p-3 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors cursor-pointer">
                  <h3 className="text-white font-medium">Privacy Settings</h3>
                  <p className="text-gray-400 text-sm">Control your privacy preferences</p>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <PasswordChangeModal 
        isOpen={showPasswordModal}
        onClose={() => setShowPasswordModal(false)}
      />
    </div>
  );
};

export default Account;
