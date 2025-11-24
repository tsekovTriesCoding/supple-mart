import { api } from './index';
import type { NotificationPreferences, UpdateNotificationPreferencesRequest } from '../../types/notification';

export const notificationAPI = {
  getPreferences: async (): Promise<NotificationPreferences> => {
    const response = await api.get('/notification-preferences');
    return response.data;
  },

  updatePreferences: async (preferences: UpdateNotificationPreferencesRequest): Promise<NotificationPreferences> => {
    const response = await api.put('/notification-preferences', preferences);
    return response.data;
  },
};
