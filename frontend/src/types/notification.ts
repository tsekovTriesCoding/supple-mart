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

// Real-time notification types
export enum NotificationType {
  ORDER_PLACED = 'ORDER_PLACED',
  ORDER_SHIPPED = 'ORDER_SHIPPED',
  ORDER_DELIVERED = 'ORDER_DELIVERED',
  ORDER_CANCELLED = 'ORDER_CANCELLED',
  PRICE_DROP = 'PRICE_DROP',
  BACK_IN_STOCK = 'BACK_IN_STOCK',
  LOW_STOCK = 'LOW_STOCK',
  ACCOUNT_SECURITY = 'ACCOUNT_SECURITY',
  PASSWORD_CHANGED = 'PASSWORD_CHANGED',
  PROFILE_UPDATED = 'PROFILE_UPDATED',
  PROMOTIONAL = 'PROMOTIONAL',
  NEWSLETTER = 'NEWSLETTER',
  PRODUCT_RECOMMENDATION = 'PRODUCT_RECOMMENDATION',
  REVIEW_REMINDER = 'REVIEW_REMINDER',
  REVIEW_RESPONSE = 'REVIEW_RESPONSE',
  SYSTEM_ANNOUNCEMENT = 'SYSTEM_ANNOUNCEMENT',
  WELCOME = 'WELCOME',
}

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  actionUrl?: string;
  referenceId?: string;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
}

export interface NotificationPageResponse {
  notifications: Notification[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  unreadCount: number;
  unreadByType: Record<string, number>;
}

export interface UnreadCountResponse {
  count: number;
}

