import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Bell, ShoppingBag, Mail, AlertCircle, Package, DollarSign, Star, Shield, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { notificationAPI } from '../lib/api/notification';
import type { UpdateNotificationPreferencesRequest } from '../types/notification';
import { LoadingSpinner } from '../components/LoadingSpinner';

interface ToggleSwitchProps {
  enabled: boolean;
  onChange: (enabled: boolean) => void;
  disabled?: boolean;
}

const ToggleSwitch = ({ enabled, onChange, disabled }: ToggleSwitchProps) => {
  return (
    <button
      type="button"
      onClick={() => !disabled && onChange(!enabled)}
      disabled={disabled}
      className={`
        relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 ease-in-out
        ${enabled ? 'bg-blue-600' : 'bg-gray-700'}
        ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:opacity-90'}
      `}
    >
      <span
        className={`
          inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200 ease-in-out
          ${enabled ? 'translate-x-6' : 'translate-x-1'}
        `}
      />
    </button>
  );
};

interface NotificationSettingProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  enabled: boolean;
  onChange: (enabled: boolean) => void;
  disabled?: boolean;
}

const NotificationSetting = ({ icon, title, description, enabled, onChange, disabled }: NotificationSettingProps) => {
  return (
    <div className="flex items-center justify-between p-4 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors">
      <div className="flex items-start space-x-3 flex-1">
        <div className="mt-0.5 text-blue-400">{icon}</div>
        <div className="flex-1">
          <h3 className="text-white font-medium">{title}</h3>
          <p className="text-gray-400 text-sm mt-1">{description}</p>
        </div>
      </div>
      <ToggleSwitch enabled={enabled} onChange={onChange} disabled={disabled} />
    </div>
  );
};
const NotificationPreferences = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: preferences, isLoading, error: queryError } = useQuery({
    queryKey: ['notification-preferences'],
    queryFn: notificationAPI.getPreferences,
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });

  const updateMutation = useMutation({
    mutationFn: (updates: UpdateNotificationPreferencesRequest) =>
      notificationAPI.updatePreferences(updates),
    onSuccess: (data) => {
      queryClient.setQueryData(['notification-preferences'], data);
    },
  });

  const [localPreferences, setLocalPreferences] = useState<UpdateNotificationPreferencesRequest | null>(null);
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    if (preferences && !localPreferences) {
      setLocalPreferences({
        orderUpdates: preferences.orderUpdates,
        shippingNotifications: preferences.shippingNotifications,
        promotionalEmails: preferences.promotionalEmails,
        newsletter: preferences.newsletter,
        productRecommendations: preferences.productRecommendations,
        priceDropAlerts: preferences.priceDropAlerts,
        backInStockAlerts: preferences.backInStockAlerts,
        accountSecurityAlerts: preferences.accountSecurityAlerts,
        passwordResetEmails: preferences.passwordResetEmails,
        reviewReminders: preferences.reviewReminders,
      });
    }
  }, [preferences, localPreferences]);

  useEffect(() => {
    if (updateMutation.isSuccess) {
      setShowSuccess(true);
      const timer = setTimeout(() => {
        setShowSuccess(false);
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [updateMutation.isSuccess]);

  const handleToggle = (key: keyof UpdateNotificationPreferencesRequest, value: boolean) => {
    if (!localPreferences) return;

    const updatedPreferences = {
      ...localPreferences,
      [key]: value,
    };
    setLocalPreferences(updatedPreferences);
    updateMutation.mutate(updatedPreferences);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <LoadingSpinner size="lg" message="Loading preferences..." />
      </div>
    );
  }

  if (queryError) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-white mb-2">Error Loading Preferences</h1>
          <p className="text-gray-400">
            {queryError instanceof Error ? queryError.message : 'Failed to load notification preferences'}
          </p>
        </div>
      </div>
    );
  }

  if (!localPreferences) return null;

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="mb-8">
          <button
            onClick={() => navigate('/account')}
            className="flex items-center space-x-2 text-gray-400 hover:text-white transition-colors mb-4"
          >
            <ArrowLeft className="w-5 h-5" />
            <span>Back to Account</span>
          </button>
          
          <div className="flex items-center space-x-3 mb-2">
            <Bell className="w-8 h-8 text-blue-400" />
            <h1 className="text-3xl font-bold text-white">Notification Preferences</h1>
          </div>
          <p className="text-gray-400">Manage how you receive updates and notifications from SuppleMart</p>
        </div>

        {showSuccess && (
          <div className="mb-6 p-4 bg-green-900/20 border border-green-700 rounded-lg animate-fade-in">
            <div className="flex items-center space-x-2">
              <AlertCircle className="w-5 h-5 text-green-400" />
              <p className="text-green-400 font-medium">Preferences saved successfully!</p>
            </div>
          </div>
        )}

        {updateMutation.isError && (
          <div className="mb-6 p-4 bg-red-900/20 border border-red-700 rounded-lg animate-fade-in">
            <div className="flex items-center space-x-2">
              <AlertCircle className="w-5 h-5 text-red-400" />
              <p className="text-red-400 font-medium">
                {updateMutation.error instanceof Error ? updateMutation.error.message : 'Failed to save preferences'}
              </p>
            </div>
          </div>
        )}

        <div className="card p-6 mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <ShoppingBag className="w-5 h-5 text-blue-400" />
            <h2 className="text-xl font-semibold text-white">Order & Shipping</h2>
          </div>
          <div className="space-y-3">
            <NotificationSetting
              icon={<Package className="w-5 h-5" />}
              title="Order Updates"
              description="Get notified about order confirmations, updates, and delivery status"
              enabled={localPreferences.orderUpdates}
              onChange={(value) => handleToggle('orderUpdates', value)}
              disabled={updateMutation.isPending}
            />
            <NotificationSetting
              icon={<Package className="w-5 h-5" />}
              title="Shipping Notifications"
              description="Receive updates when your orders are shipped and out for delivery"
              enabled={localPreferences.shippingNotifications}
              onChange={(value) => handleToggle('shippingNotifications', value)}
              disabled={updateMutation.isPending}
            />
          </div>
        </div>

        <div className="card p-6 mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <Mail className="w-5 h-5 text-blue-400" />
            <h2 className="text-xl font-semibold text-white">Marketing & Promotions</h2>
          </div>
          <div className="space-y-3">
            <NotificationSetting
              icon={<Mail className="w-5 h-5" />}
              title="Promotional Emails"
              description="Receive emails about sales, special offers, and exclusive deals"
              enabled={localPreferences.promotionalEmails}
              onChange={(value) => handleToggle('promotionalEmails', value)}
              disabled={updateMutation.isPending}
            />
            <NotificationSetting
              icon={<Mail className="w-5 h-5" />}
              title="Newsletter"
              description="Get our weekly newsletter with tips, guides, and new product announcements"
              enabled={localPreferences.newsletter}
              onChange={(value) => handleToggle('newsletter', value)}
              disabled={updateMutation.isPending}
            />
          </div>
        </div>

        <div className="card p-6 mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <Bell className="w-5 h-5 text-blue-400" />
            <h2 className="text-xl font-semibold text-white">Product Notifications</h2>
          </div>
          <div className="space-y-3">
            <NotificationSetting
              icon={<Star className="w-5 h-5" />}
              title="Product Recommendations"
              description="Get personalized product recommendations based on your preferences"
              enabled={localPreferences.productRecommendations}
              onChange={(value) => handleToggle('productRecommendations', value)}
              disabled={updateMutation.isPending}
            />
            <NotificationSetting
              icon={<DollarSign className="w-5 h-5" />}
              title="Price Drop Alerts"
              description="Be notified when items in your wishlist go on sale"
              enabled={localPreferences.priceDropAlerts}
              onChange={(value) => handleToggle('priceDropAlerts', value)}
              disabled={updateMutation.isPending}
            />
            <NotificationSetting
              icon={<Package className="w-5 h-5" />}
              title="Back in Stock Alerts"
              description="Get alerts when out-of-stock items become available again"
              enabled={localPreferences.backInStockAlerts}
              onChange={(value) => handleToggle('backInStockAlerts', value)}
              disabled={updateMutation.isPending}
            />
          </div>
        </div>

        <div className="card p-6 mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <Shield className="w-5 h-5 text-blue-400" />
            <h2 className="text-xl font-semibold text-white">Account & Security</h2>
          </div>
          <div className="space-y-3">
            <NotificationSetting
              icon={<Shield className="w-5 h-5" />}
              title="Account Security Alerts"
              description="Important notifications about account security and login activity"
              enabled={localPreferences.accountSecurityAlerts}
              onChange={(value) => handleToggle('accountSecurityAlerts', value)}
              disabled={updateMutation.isPending}
            />
            <NotificationSetting
              icon={<AlertCircle className="w-5 h-5" />}
              title="Password Reset Emails"
              description="Receive confirmation emails when you reset your password"
              enabled={localPreferences.passwordResetEmails}
              onChange={(value) => handleToggle('passwordResetEmails', value)}
              disabled={updateMutation.isPending}
            />
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center space-x-2 mb-4">
            <Star className="w-5 h-5 text-blue-400" />
            <h2 className="text-xl font-semibold text-white">Reviews & Feedback</h2>
          </div>
          <div className="space-y-3">
            <NotificationSetting
              icon={<Star className="w-5 h-5" />}
              title="Review Reminders"
              description="Get reminders to review products you've purchased"
              enabled={localPreferences.reviewReminders}
              onChange={(value) => handleToggle('reviewReminders', value)}
              disabled={updateMutation.isPending}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotificationPreferences;
