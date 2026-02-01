import { api } from './index';
import type { 
  NotificationPreferences, 
  UpdateNotificationPreferencesRequest,
  Notification,
  NotificationPageResponse,
  UnreadCountResponse
} from '../../types/notification';

export const notificationAPI = {
  getPreferences: async (): Promise<NotificationPreferences> => {
    const response = await api.get('/notification-preferences');
    return response.data;
  },

  updatePreferences: async (preferences: UpdateNotificationPreferencesRequest): Promise<NotificationPreferences> => {
    const response = await api.put('/notification-preferences', preferences);
    return response.data;
  },

  getNotifications: async (page = 0, size = 20): Promise<NotificationPageResponse> => {
    const response = await api.get('/notifications', { params: { page, size } });
    return response.data;
  },

  getUnreadNotifications: async (): Promise<Notification[]> => {
    const response = await api.get('/notifications/unread');
    return response.data;
  },

  getUnreadCount: async (): Promise<UnreadCountResponse> => {
    const response = await api.get('/notifications/unread/count');
    return response.data;
  },

  markAsRead: async (notificationId: string): Promise<Notification> => {
    const response = await api.patch(`/notifications/${notificationId}/read`);
    return response.data;
  },

  markAllAsRead: async (): Promise<void> => {
    await api.post('/notifications/mark-all-read');
  },

  deleteNotification: async (notificationId: string): Promise<void> => {
    await api.delete(`/notifications/${notificationId}`);
  },
};

