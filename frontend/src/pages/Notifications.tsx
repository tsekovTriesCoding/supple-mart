import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Bell, 
  CheckCheck, 
  Trash2, 
  ArrowLeft, 
  Filter,
  RefreshCw,
  ExternalLink,
  Check
} from 'lucide-react';
import { useNotifications } from '../hooks/useNotifications';
import { formatDistanceToNow, formatDateTime } from '../utils/dateUtils';
import { LoadingSpinner } from '../components/LoadingSpinner';
import type { Notification, NotificationType } from '../types/notification';

const notificationTypeLabels: Record<NotificationType, string> = {
  ORDER_PLACED: 'Order Placed',
  ORDER_SHIPPED: 'Order Shipped',
  ORDER_DELIVERED: 'Order Delivered',
  ORDER_CANCELLED: 'Order Cancelled',
  PRICE_DROP: 'Price Drop',
  BACK_IN_STOCK: 'Back in Stock',
  LOW_STOCK: 'Low Stock',
  ACCOUNT_SECURITY: 'Security',
  PASSWORD_CHANGED: 'Password',
  PROFILE_UPDATED: 'Profile',
  PROMOTIONAL: 'Promotion',
  NEWSLETTER: 'Newsletter',
  PRODUCT_RECOMMENDATION: 'Recommendation',
  REVIEW_REMINDER: 'Review',
  REVIEW_RESPONSE: 'Review Response',
  SYSTEM_ANNOUNCEMENT: 'Announcement',
  WELCOME: 'Welcome',
};

const notificationTypeIcons: Record<string, string> = {
  ORDER_PLACED: '🎉',
  ORDER_SHIPPED: '📦',
  ORDER_DELIVERED: '✅',
  ORDER_CANCELLED: '❌',
  PRICE_DROP: '💰',
  BACK_IN_STOCK: '🔔',
  LOW_STOCK: '⚠️',
  ACCOUNT_SECURITY: '🔒',
  PASSWORD_CHANGED: '🔑',
  PROFILE_UPDATED: '👤',
  PROMOTIONAL: '🎁',
  NEWSLETTER: '📰',
  PRODUCT_RECOMMENDATION: '💡',
  REVIEW_REMINDER: '⭐',
  REVIEW_RESPONSE: '💬',
  SYSTEM_ANNOUNCEMENT: '📢',
  WELCOME: '👋',
};

type FilterType = 'all' | 'unread' | 'orders' | 'alerts' | 'promotions';

const Notifications = () => {
  const navigate = useNavigate();
  const [filter, setFilter] = useState<FilterType>('all');
  
  const {
    notifications,
    unreadCount,
    isLoading,
    isConnected,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    fetchMore,
    refresh,
    totalPages,
    currentPage,
  } = useNotifications();

  const filteredNotifications = notifications.filter(notification => {
    switch (filter) {
      case 'unread':
        return !notification.isRead;
      case 'orders':
        return ['ORDER_PLACED', 'ORDER_SHIPPED', 'ORDER_DELIVERED', 'ORDER_CANCELLED'].includes(notification.type);
      case 'alerts':
        return ['ACCOUNT_SECURITY', 'PASSWORD_CHANGED', 'LOW_STOCK', 'BACK_IN_STOCK', 'PRICE_DROP'].includes(notification.type);
      case 'promotions':
        return ['PROMOTIONAL', 'NEWSLETTER', 'PRODUCT_RECOMMENDATION'].includes(notification.type);
      default:
        return true;
    }
  });

  const handleNotificationClick = async (notification: Notification) => {
    if (!notification.isRead) {
      await markAsRead(notification.id);
    }
    if (notification.actionUrl) {
      navigate(notification.actionUrl);
    }
  };

  const handleDelete = async (e: React.MouseEvent, notificationId: string) => {
    e.stopPropagation();
    await deleteNotification(notificationId);
  };

  const filters: { value: FilterType; label: string; count?: number }[] = [
    { value: 'all', label: 'All', count: notifications.length },
    { value: 'unread', label: 'Unread', count: unreadCount },
    { value: 'orders', label: 'Orders' },
    { value: 'alerts', label: 'Alerts' },
    { value: 'promotions', label: 'Promotions' },
  ];

  if (isLoading && notifications.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <LoadingSpinner size="lg" message="Loading notifications..." />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center text-gray-400 hover:text-white mb-4 transition-colors cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5 mr-2" />
          Back
        </button>

        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <Bell className="w-8 h-8 text-blue-400" />
            <div>
              <h1 className="text-3xl font-bold text-white">Notifications</h1>
              <p className="text-gray-400 mt-1">
                {unreadCount > 0 ? `${unreadCount} unread` : 'All caught up!'}
                {!isConnected && (
                  <span className="ml-2 inline-flex items-center text-yellow-400">
                    <span className="w-2 h-2 rounded-full mr-1 bg-yellow-400 animate-pulse" />
                    Offline - reconnecting...
                  </span>
                )}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <button
              onClick={refresh}
              className="p-2 text-gray-400 hover:text-white hover:bg-gray-800 rounded-lg transition-colors cursor-pointer"
              title="Refresh"
            >
              <RefreshCw className="w-5 h-5" />
            </button>
            {unreadCount > 0 && (
              <button
                onClick={markAllAsRead}
                className="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors cursor-pointer"
              >
                <CheckCheck className="w-4 h-4" />
                <span>Mark all read</span>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="flex items-center space-x-2 mb-6 overflow-x-auto pb-2">
        <Filter className="w-5 h-5 text-gray-400 shrink-0" />
        {filters.map(f => (
          <button
            key={f.value}
            onClick={() => setFilter(f.value)}
            className={`
              px-4 py-2 rounded-full text-sm font-medium transition-colors shrink-0 cursor-pointer
              ${filter === f.value 
                ? 'bg-blue-600 text-white' 
                : 'bg-gray-800 text-gray-400 hover:bg-gray-700 hover:text-white'}
            `}
          >
            {f.label}
            {f.count !== undefined && (
              <span className="ml-2 px-2 py-0.5 bg-black/20 rounded-full text-xs">
                {f.count}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Notification List */}
      <div className="space-y-3">
        {filteredNotifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-gray-400">
            <Bell className="w-16 h-16 mb-4 opacity-30" />
            <p className="text-lg">No notifications found</p>
            <p className="text-sm text-gray-500 mt-1">
              {filter !== 'all' ? 'Try changing the filter' : 'You\'re all caught up!'}
            </p>
          </div>
        ) : (
          <>
            {filteredNotifications.map(notification => (
              <NotificationCard
                key={notification.id}
                notification={notification}
                onClick={() => handleNotificationClick(notification)}
                onDelete={(e) => handleDelete(e, notification.id)}
              />
            ))}

            {/* Load More */}
            {currentPage < totalPages - 1 && (
              <button
                onClick={fetchMore}
                className="w-full py-4 text-blue-400 hover:text-blue-300 hover:bg-gray-800 rounded-lg transition-colors cursor-pointer"
              >
                Load more notifications
              </button>
            )}
          </>
        )}
      </div>
    </div>
  );
};

interface NotificationCardProps {
  notification: Notification;
  onClick: () => void;
  onDelete: (e: React.MouseEvent) => void;
}

function NotificationCard({ notification, onClick, onDelete }: NotificationCardProps) {
  const icon = notificationTypeIcons[notification.type] || '🔔';
  const typeLabel = notificationTypeLabels[notification.type as NotificationType] || notification.type;

  return (
    <div
      onClick={onClick}
      className={`
        flex items-start p-4 rounded-xl border cursor-pointer transition-all
        ${notification.isRead 
          ? 'bg-gray-900 border-gray-800 hover:border-gray-700' 
          : 'bg-gray-800/50 border-blue-900/50 hover:border-blue-800/50'}
      `}
    >
      {/* Icon */}
      <div className="shrink-0 w-12 h-12 flex items-center justify-center rounded-full bg-gray-700 text-2xl">
        {icon}
      </div>

      {/* Content */}
      <div className="flex-1 ml-4 min-w-0">
        <div className="flex items-start justify-between">
          <div>
            <span className="inline-block px-2 py-0.5 text-xs font-medium rounded bg-gray-700 text-gray-300 mb-2">
              {typeLabel}
            </span>
            <h3 className={`font-medium ${notification.isRead ? 'text-gray-300' : 'text-white'}`}>
              {notification.title}
            </h3>
          </div>
          {!notification.isRead && (
            <span className="shrink-0 w-3 h-3 bg-blue-500 rounded-full ml-2" />
          )}
        </div>
        
        <p className="text-gray-400 mt-1">{notification.message}</p>
        
        <div className="flex items-center justify-between mt-3">
          <div className="flex items-center text-sm text-gray-500">
            <span>{formatDistanceToNow(notification.createdAt)}</span>
            <span className="mx-2">•</span>
            <span>{formatDateTime(notification.createdAt)}</span>
          </div>
          
          <div className="flex items-center space-x-2">
            {notification.actionUrl && (
              <span className="text-blue-400">
                <ExternalLink className="w-4 h-4" />
              </span>
            )}
            {notification.isRead && (
              <Check className="w-4 h-4 text-green-500" />
            )}
            <button
              onClick={onDelete}
              className="p-1.5 text-gray-500 hover:text-red-400 hover:bg-gray-800 rounded transition-colors cursor-pointer"
              title="Delete"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Notifications;
