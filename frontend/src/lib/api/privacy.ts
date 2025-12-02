import { api } from './index';
import type { PrivacySettings, UpdatePrivacySettingsRequest } from '../../types/privacy';

export const privacyAPI = {
  getSettings: async (): Promise<PrivacySettings> => {
    const response = await api.get('/privacy-settings');
    return response.data;
  },

  updateSettings: async (settings: UpdatePrivacySettingsRequest): Promise<PrivacySettings> => {
    const response = await api.put('/privacy-settings', settings);
    return response.data;
  },
};
