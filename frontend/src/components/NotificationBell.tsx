import { useState, useRef, useEffect } from 'react';
import { Bell, Check, CheckCheck, Trash2, ExternalLink, X, WifiOff } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../hooks/useNotifications';
import { formatDistanceToNow } from '../utils/dateUtils';
import type { Notification } from '../types/notification';

export function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false);
  const [hasNewNotification, setHasNewNotification] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const prevUnreadCountRef = useRef<number>(0);
  const navigate = useNavigate();
  
  const {
    notifications,
    unreadCount,
    isConnected,
    isLoading,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    fetchMore,
    totalPages,
    currentPage,
  } = useNotifications();

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Detect new notifications and trigger pulse animation
  useEffect(() => {
    if (unreadCount > prevUnreadCountRef.current && prevUnreadCountRef.current !== 0) {
      setHasNewNotification(true);
      const timer = setTimeout(() => setHasNewNotification(false), 2000);
      return () => clearTimeout(timer);
    }
    prevUnreadCountRef.current = unreadCount;
  }, [unreadCount]);

  const handleNotificationClick = async (notification: Notification) => {
    if (!notification.isRead) {
      await markAsRead(notification.id);
    }
    if (notification.actionUrl) {
      navigate(notification.actionUrl);
      setIsOpen(false);
    }
  };

  const handleMarkAllRead = async () => {
    await markAllAsRead();
  };

  const handleDelete = async (e: React.MouseEvent, notificationId: string) => {
    e.stopPropagation();
    await deleteNotification(notificationId);
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 text-gray-300 hover:text-blue-400 transition-colors cursor-pointer group"
        aria-label="Notifications"
      >
        <Bell className={`w-6 h-6 group-hover:text-blue-400 transition-colors ${
          hasNewNotification ? 'animate-bounce text-blue-400' : ''
        }`} />
        
        {unreadCount > 0 && (
          <span className={`absolute -top-1 -right-1 flex items-center justify-center min-w-5 h-5 px-1 text-xs font-bold text-white bg-red-500 rounded-full ${
            hasNewNotification ? 'animate-pulse' : ''
          }`}>
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
        
        {!isConnected && (
          <span 
            className="absolute bottom-0 right-0 w-3 h-3 bg-yellow-500 rounded-full flex items-center justify-center"
            title="Disconnected - notifications may be delayed"
          >
            <WifiOff className="w-2 h-2 text-yellow-900" />
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-96 max-h-128 bg-gray-900 border border-gray-700 rounded-lg shadow-xl overflow-hidden z-50">
          <div className="flex items-center justify-between px-4 py-3 border-b border-gray-700 bg-gray-800">
            <h3 className="text-lg font-semibold text-white">Notifications</h3>
            <div className="flex items-center space-x-2">
              {unreadCount > 0 && (
                <button
                  onClick={handleMarkAllRead}
                  className="p-1.5 text-gray-400 hover:text-white hover:bg-gray-700 rounded transition-colors cursor-pointer"
                  title="Mark all as read"
                >
                  <CheckCheck className="w-4 h-4" />
                </button>
              )}
              <button
                onClick={() => setIsOpen(false)}
                className="p-1.5 text-gray-400 hover:text-white hover:bg-gray-700 rounded transition-colors cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="overflow-y-auto max-h-96">
            {isLoading && notifications.length === 0 ? (
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500"></div>
              </div>
            ) : notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-gray-400">
                <Bell className="w-12 h-12 mb-3 opacity-50" />
                <p className="text-sm">No notifications yet</p>
              </div>
            ) : (
              <>
                {notifications.map((notification) => (
                  <NotificationItem
                    key={notification.id}
                    notification={notification}
                    onClick={() => handleNotificationClick(notification)}
                    onDelete={(e) => handleDelete(e, notification.id)}
                  />
                ))}
                

                {currentPage < totalPages - 1 && (
                  <button
                    onClick={fetchMore}
                    className="w-full py-3 text-sm text-blue-400 hover:text-blue-300 hover:bg-gray-800 transition-colors cursor-pointer"
                  >
                    Load more
                  </button>
                )}
              </>
            )}
          </div>

          <div className="px-4 py-2 border-t border-gray-700 bg-gray-800">
            <button
              onClick={() => {
                navigate('/notifications');
                setIsOpen(false);
              }}
              className="w-full text-center text-sm text-blue-400 hover:text-blue-300 py-1 cursor-pointer"
            >
              View all notifications
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

interface NotificationItemProps {
  notification: Notification;
  onClick: () => void;
  onDelete: (e: React.MouseEvent) => void;
}

function NotificationItem({ notification, onClick, onDelete }: NotificationItemProps) {
  const icon = getNotificationIcon(notification.type);
  
  return (
    <div
      onClick={onClick}
      className={`
        flex items-start p-4 border-b border-gray-800 cursor-pointer transition-colors
        ${notification.isRead ? 'bg-gray-900' : 'bg-gray-800/50'}
        hover:bg-gray-800
      `}
    >
      <div className="shrink-0 w-10 h-10 flex items-center justify-center rounded-full bg-gray-700 text-xl">
        {icon}
      </div>
      
      <div className="flex-1 ml-3 min-w-0">
        <div className="flex items-start justify-between">
          <p className={`text-sm font-medium ${notification.isRead ? 'text-gray-300' : 'text-white'}`}>
            {notification.title}
          </p>
          {!notification.isRead && (
            <span className="shrink-0 w-2 h-2 bg-blue-500 rounded-full ml-2 mt-1.5" />
          )}
        </div>
        <p className="text-sm text-gray-400 mt-1 line-clamp-2">{notification.message}</p>
        <div className="flex items-center mt-2 text-xs text-gray-500">
          <span>{formatDistanceToNow(notification.createdAt)}</span>
          {notification.actionUrl && (
            <ExternalLink className="w-3 h-3 ml-2" />
          )}
        </div>
      </div>
      
      <div className="shrink-0 ml-2 flex flex-col space-y-1">
        {notification.isRead ? (
          <Check className="w-4 h-4 text-green-500" />
        ) : (
          <span className="w-4 h-4" />
        )}
        <button
          onClick={onDelete}
          className="p-1 text-gray-500 hover:text-red-400 transition-colors cursor-pointer"
          title="Delete"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}

function getNotificationIcon(type: string): string {
  const icons: Record<string, string> = {
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
  return icons[type] || '🔔';
}
