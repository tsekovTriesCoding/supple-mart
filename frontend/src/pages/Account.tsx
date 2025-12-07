import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Mail, Calendar, AlertCircle, Camera, Trash2 } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';

import { userAPI, type UpdateUserProfileRequest } from '../lib/api/user';
import { PasswordChangeModal } from '../components/PasswordChangeModal';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { FormInput } from '../components/Form';

const Account = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  
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
          imageUrl: updatedUser.imageUrl,
        })
      );
      
      setIsEditing(false);
      toast.success(`Welcome back, ${updatedUser.firstName}! Your profile has been updated.`);
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to update profile'
        : 'Failed to update profile';
      toast.error(message);
    },
  });

  const uploadPictureMutation = useMutation({
    mutationFn: (file: File) => userAPI.updateProfilePicture(file),
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
          imageUrl: updatedUser.imageUrl,
        })
      );
      
      toast.success('Profile picture updated successfully!');
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to upload profile picture'
        : 'Failed to upload profile picture';
      toast.error(message);
    },
  });

  const deletePictureMutation = useMutation({
    mutationFn: () => userAPI.deleteProfilePicture(),
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
          imageUrl: updatedUser.imageUrl,
        })
      );
      
      toast.success('Profile picture removed successfully!');
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to remove profile picture'
        : 'Failed to remove profile picture';
      toast.error(message);
    },
  });

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({ firstName: '', lastName: '' });
  const [showPasswordModal, setShowPasswordModal] = useState(false);

  const error = queryError instanceof AxiosError
    ? queryError.response?.data?.message || 'Failed to load profile'
    : queryError
    ? 'Failed to load profile'
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

  const handlePictureUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handlePictureChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        toast.error('File size must be less than 5MB');
        return;
      }

      const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
      if (!validTypes.includes(file.type)) {
        toast.error('Invalid file type. Please upload JPG, PNG, GIF, or WebP');
        return;
      }

      uploadPictureMutation.mutate(file);
    }
    // Reset input so the same file can be selected again
    e.target.value = '';
  };

  const handleDeletePicture = () => {
    if (window.confirm('Are you sure you want to remove your profile picture?')) {
      deletePictureMutation.mutate();
    }
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

          <div className="card p-6 mb-6">
            <h2 className="text-xl font-semibold text-white mb-4 flex items-center">
              <Camera className="w-5 h-5 mr-2 text-blue-400" />
              Profile Picture
            </h2>
            
            <div className="flex items-center space-x-6">
              <div className="relative">
                {user.imageUrl ? (
                  <img 
                    src={user.imageUrl} 
                    alt="Profile" 
                    className="w-24 h-24 rounded-full object-cover ring-4 ring-gray-700"
                  />
                ) : (
                  <div className="w-24 h-24 rounded-full bg-gray-700 flex items-center justify-center">
                    <User className="w-12 h-12 text-gray-400" />
                  </div>
                )}
                
                {(uploadPictureMutation.isPending || deletePictureMutation.isPending) && (
                  <div className="absolute inset-0 bg-black/50 rounded-full flex items-center justify-center">
                    <LoadingSpinner size="sm" />
                  </div>
                )}
              </div>

              <div className="flex-1">
                <p className="text-gray-400 text-sm mb-4">
                  Upload a new profile picture. Max file size: 5MB. Supported formats: JPG, PNG, GIF, WebP.
                </p>
                
                <div className="flex flex-wrap gap-3">
                  <button
                    onClick={handlePictureUploadClick}
                    disabled={uploadPictureMutation.isPending || deletePictureMutation.isPending}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer flex items-center space-x-2"
                  >
                    <Camera className="w-4 h-4" />
                    <span>Upload Picture</span>
                  </button>
                  
                  {user.imageUrl && (
                    <button
                      onClick={handleDeletePicture}
                      disabled={uploadPictureMutation.isPending || deletePictureMutation.isPending}
                      className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer flex items-center space-x-2"
                    >
                      <Trash2 className="w-4 h-4" />
                      <span>Remove Picture</span>
                    </button>
                  )}
                </div>
                
                {/* Hidden File Input */}
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                  onChange={handlePictureChange}
                  className="hidden"
                />
              </div>
            </div>
          </div>

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
                  <FormInput
                    label="First Name"
                    type="text"
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    required
                  />

                  <FormInput
                    label="Last Name"
                    type="text"
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    required
                  />

                  <FormInput
                    label="Email"
                    type="email"
                    value={user.email}
                    className="bg-gray-800 cursor-not-allowed opacity-60"
                    disabled
                    hint="Email cannot be changed. Contact support if you need to update your email address."
                  />

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
                  onClick={() => navigate('/account/notifications')}
                  className="w-full text-left p-3 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors cursor-pointer"
                >
                  <h3 className="text-white font-medium">Notification Preferences</h3>
                  <p className="text-gray-400 text-sm">Manage email and push notifications</p>
                </button>
                
                <button 
                  onClick={() => navigate('/account/privacy')}
                  className="w-full text-left p-3 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors cursor-pointer"
                >
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
