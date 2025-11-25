export interface NotificationPreferences {
  id: string;
  userId: string;
  orderUpdates: boolean;
  shippingNotifications: boolean;
  promotionalEmails: boolean;
  newsletter: boolean;
  productRecommendations: boolean;
  priceDropAlerts: boolean;
  backInStockAlerts: boolean;
  accountSecurityAlerts: boolean;
  passwordResetEmails: boolean;
  reviewReminders: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateNotificationPreferencesRequest {
  orderUpdates: boolean;
  shippingNotifications: boolean;
  promotionalEmails: boolean;
  newsletter: boolean;
  productRecommendations: boolean;
  priceDropAlerts: boolean;
  backInStockAlerts: boolean;
  accountSecurityAlerts: boolean;
  passwordResetEmails: boolean;
  reviewReminders: boolean;
}
