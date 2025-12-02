import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
  Shield, 
  Eye, 
  EyeOff, 
  Users, 
  BarChart3, 
  Mail, 
  Share2, 
  Search, 
  MessageSquare,
  AlertCircle, 
  ArrowLeft,
  Info
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';

import { privacyAPI } from '../lib/api/privacy';
import type { UpdatePrivacySettingsRequest } from '../types/privacy';
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

interface PrivacySettingProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  enabled: boolean;
  onChange: (enabled: boolean) => void;
  disabled?: boolean;
  info?: string;
}

const PrivacySetting = ({ icon, title, description, enabled, onChange, disabled, info }: PrivacySettingProps) => {
  const [showInfo, setShowInfo] = useState(false);

  return (
    <div className="flex items-center justify-between p-4 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors">
      <div className="flex items-start space-x-3 flex-1">
        <div className="mt-0.5 text-blue-400">{icon}</div>
        <div className="flex-1">
          <div className="flex items-center space-x-2">
            <h3 className="text-white font-medium">{title}</h3>
            {info && (
              <button
                type="button"
                onClick={() => setShowInfo(!showInfo)}
                className="text-gray-500 hover:text-gray-400 transition-colors"
              >
                <Info className="w-4 h-4" />
              </button>
            )}
          </div>
          <p className="text-gray-400 text-sm mt-1">{description}</p>
          {showInfo && info && (
            <p className="text-blue-400 text-xs mt-2 p-2 bg-blue-900/20 rounded">{info}</p>
          )}
        </div>
      </div>
      <ToggleSwitch enabled={enabled} onChange={onChange} disabled={disabled} />
    </div>
  );
};

const PrivacySettingsPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [localSettings, setLocalSettings] = useState<UpdatePrivacySettingsRequest | null>(null);

  const {
    data: settings,
    isLoading,
    error: queryError,
  } = useQuery({
    queryKey: ['privacy-settings'],
    queryFn: privacyAPI.getSettings,
  });

  const mutation = useMutation({
    mutationFn: (data: UpdatePrivacySettingsRequest) => privacyAPI.updateSettings(data),
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to save settings'
        : 'Failed to save settings';
      toast.error(message);
    },
  });

  useEffect(() => {
    if (settings && !localSettings) {
      setLocalSettings({
        showProfile: settings.showProfile,
        showActivity: settings.showActivity,
        showOnlineStatus: settings.showOnlineStatus,
        shareAnalytics: settings.shareAnalytics,
        shareMarketing: settings.shareMarketing,
        shareThirdParty: settings.shareThirdParty,
        searchable: settings.searchable,
        allowMessages: settings.allowMessages,
      });
    }
  }, [settings, localSettings]);

  const getSettingLabel = (key: keyof UpdatePrivacySettingsRequest): string => {
    const labels: Record<keyof UpdatePrivacySettingsRequest, string> = {
      showProfile: 'Show Profile',
      showActivity: 'Show Activity',
      showOnlineStatus: 'Online Status',
      shareAnalytics: 'Analytics Data',
      shareMarketing: 'Marketing Personalization',
      shareThirdParty: 'Third-Party Sharing',
      searchable: 'Searchable Profile',
      allowMessages: 'Allow Messages',
    };
    return labels[key];
  };

  const handleToggle = (key: keyof UpdatePrivacySettingsRequest, value: boolean) => {
    if (!localSettings) return;

    const updatedSettings = {
      ...localSettings,
      [key]: value,
    };
    setLocalSettings(updatedSettings);

    const settingLabel = getSettingLabel(key);
    const action = value ? 'enabled' : 'disabled';

    mutation.mutate(updatedSettings, {
      onSuccess: (data) => {
        queryClient.setQueryData(['privacy-settings'], data);
        toast.success(`${settingLabel} ${action}`);
      },
    });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <LoadingSpinner size="lg" message="Loading privacy settings..." />
      </div>
    );
  }

  if (queryError) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-white mb-2">Error Loading Settings</h1>
          <p className="text-gray-400">
            {queryError instanceof Error ? queryError.message : 'Failed to load privacy settings'}
          </p>
        </div>
      </div>
    );
  }

  if (!localSettings) return null;

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="mb-8">
          <button
            onClick={() => navigate('/account')}
            className="flex items-center space-x-2 text-gray-400 hover:text-white transition-colors mb-4 cursor-pointer"
          >
            <ArrowLeft className="w-5 h-5" />
            <span>Back to Account</span>
          </button>

          <div className="flex items-center space-x-3 mb-2">
            <Shield className="w-8 h-8 text-blue-400" />
            <h1 className="text-3xl font-bold text-white">Privacy Settings</h1>
          </div>
          <p className="text-gray-400">Control your privacy and how your information is shared</p>
        </div>

        <div className="card p-6 mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <Eye className="w-5 h-5 text-blue-400" />
            <h2 className="text-xl font-semibold text-white">Profile Visibility</h2>
          </div>
          <div className="space-y-3">
            <PrivacySetting
              icon={<Users className="w-5 h-5" />}
              title="Show Profile"
              description="Allow other users to view your profile page"
              enabled={localSettings.showProfile ?? true}
              onChange={(value) => handleToggle('showProfile', value)}
              disabled={mutation.isPending}
              info="When disabled, your profile page will show a 'Profile not available' message to others."
            />
            <PrivacySetting
              icon={<BarChart3 className="w-5 h-5" />}
              title="Show Activity"
              description="Display your recent orders and reviews on your profile"
              enabled={localSettings.showActivity ?? false}
              onChange={(value) => handleToggle('showActivity', value)}
              disabled={mutation.isPending}
              info="This includes your purchase history and reviews you've written."
            />
            <PrivacySetting
              icon={<EyeOff className="w-5 h-5" />}
              title="Show Online Status"
              description="Let others see when you're online"
              enabled={localSettings.showOnlineStatus ?? false}
              onChange={(value) => handleToggle('showOnlineStatus', value)}
              disabled={mutation.isPending}
            />
          </div>
        </div>

        <div className="card p-6 mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <Share2 className="w-5 h-5 text-blue-400" />
            <h2 className="text-xl font-semibold text-white">Data Sharing</h2>
          </div>
          <div className="space-y-3">
            <PrivacySetting
              icon={<BarChart3 className="w-5 h-5" />}
              title="Analytics Data"
              description="Share anonymous usage data to help improve our services"
              enabled={localSettings.shareAnalytics ?? false}
              onChange={(value) => handleToggle('shareAnalytics', value)}
              disabled={mutation.isPending}
              info="This data is anonymized and used to improve product recommendations and site performance."
            />
            <PrivacySetting
              icon={<Mail className="w-5 h-5" />}
              title="Marketing Personalization"
              description="Allow personalized marketing based on your activity"
              enabled={localSettings.shareMarketing ?? false}
              onChange={(value) => handleToggle('shareMarketing', value)}
              disabled={mutation.isPending}
              info="Enables personalized product recommendations and targeted offers based on your browsing history."
            />
            <PrivacySetting
              icon={<Share2 className="w-5 h-5" />}
              title="Third-Party Sharing"
              description="Share data with trusted third-party partners"
              enabled={localSettings.shareThirdParty ?? false}
              onChange={(value) => handleToggle('shareThirdParty', value)}
              disabled={mutation.isPending}
              info="We only share data with verified partners for improving services. No personal data is sold."
            />
          </div>
        </div>

        <div className="card p-6 mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <MessageSquare className="w-5 h-5 text-blue-400" />
            <h2 className="text-xl font-semibold text-white">Communication</h2>
          </div>
          <div className="space-y-3">
            <PrivacySetting
              icon={<Search className="w-5 h-5" />}
              title="Searchable Profile"
              description="Allow your profile to appear in search results"
              enabled={localSettings.searchable ?? true}
              onChange={(value) => handleToggle('searchable', value)}
              disabled={mutation.isPending}
              info="When disabled, other users won't find your profile through the search feature."
            />
            <PrivacySetting
              icon={<MessageSquare className="w-5 h-5" />}
              title="Allow Messages"
              description="Let other users send you direct messages"
              enabled={localSettings.allowMessages ?? true}
              onChange={(value) => handleToggle('allowMessages', value)}
              disabled={mutation.isPending}
            />
          </div>
        </div>

        <div className="card p-6 bg-blue-900/10 border-blue-800">
          <div className="flex items-start space-x-3">
            <Shield className="w-6 h-6 text-blue-400 mt-0.5" />
            <div>
              <h3 className="text-white font-medium mb-1">Your Privacy Matters</h3>
              <p className="text-gray-400 text-sm">
                We use privacy-first defaults to protect your information. Your data is encrypted 
                and we never sell your personal information to third parties. For more details, 
                please review our{' '}
                <a href="/privacy-policy" className="text-blue-400 hover:underline">
                  Privacy Policy
                </a>.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PrivacySettingsPage;
