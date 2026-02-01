import { createContext, useEffect, useState, useCallback, type ReactNode } from 'react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';

import { useWebSocket } from '../hooks/useWebSocket';
import { notificationAPI } from '../lib/api/notification';
import type { Notification, NotificationPageResponse } from '../types/notification';

interface NotificationContextValue {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  isConnected: boolean;
  totalPages: number;
  currentPage: number;
  markAsRead: (notificationId: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  deleteNotification: (notificationId: string) => Promise<void>;
  fetchMore: () => void;
  refresh: () => void;
}

const NotificationContext = createContext<NotificationContextValue | null>(null);

// Export context for the hook in separate file
export { NotificationContext };

interface NotificationProviderProps {
  children: ReactNode;
}

export function NotificationProvider({ children }: NotificationProviderProps) {
  const queryClient = useQueryClient();
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 20;
  const isAuthenticated = !!localStorage.getItem('token');

  const { 
    data: notificationData, 
    isLoading,
    refetch 
  } = useQuery<NotificationPageResponse>({
    queryKey: ['notifications', currentPage],
    queryFn: () => notificationAPI.getNotifications(currentPage, pageSize),
    enabled: isAuthenticated,
    staleTime: 30 * 1000, // 30 seconds
    refetchOnWindowFocus: true,
  });

  const [localNotifications, setLocalNotifications] = useState<Notification[]>([]);
  const [localUnreadCount, setLocalUnreadCount] = useState(0);

  useEffect(() => {
    if (notificationData) {
      if (currentPage === 0) {
        setLocalNotifications(notificationData.notifications);
      } else {
        setLocalNotifications(prev => [...prev, ...notificationData.notifications]);
      }
      setLocalUnreadCount(notificationData.unreadCount);
    }
  }, [notificationData, currentPage]);

  const handleNewNotification = useCallback((notification: Notification) => {
    setLocalNotifications(prev => [notification, ...prev]);
    setLocalUnreadCount(prev => prev + 1);
    queryClient.invalidateQueries({ queryKey: ['notifications'] });
  }, [queryClient]);

  const handleUnreadCountUpdate = useCallback((count: number) => {
    setLocalUnreadCount(count);
  }, []);

  const { isConnected, connect, disconnect } = useWebSocket({
    onNotification: handleNewNotification,
    onUnreadCountUpdate: handleUnreadCountUpdate,
  });

  useEffect(() => {
    if (isAuthenticated) {
      connect();
    } else {
      disconnect();
      setLocalNotifications([]);
      setLocalUnreadCount(0);
    }
  }, [isAuthenticated, connect, disconnect]);

  const markAsReadMutation = useMutation({
    mutationFn: notificationAPI.markAsRead,
    onSuccess: (updatedNotification) => {
      setLocalNotifications(prev =>
        prev.map(n => n.id === updatedNotification.id ? updatedNotification : n)
      );
      setLocalUnreadCount(prev => Math.max(0, prev - 1));
    },
  });

  const markAllAsReadMutation = useMutation({
    mutationFn: notificationAPI.markAllAsRead,
    onSuccess: () => {
      setLocalNotifications(prev =>
        prev.map(n => ({ ...n, isRead: true, readAt: new Date().toISOString() }))
      );
      setLocalUnreadCount(0);
    },
  });

  const deleteNotificationMutation = useMutation({
    mutationFn: notificationAPI.deleteNotification,
    onSuccess: (_, deletedId) => {
      const deleted = localNotifications.find(n => n.id === deletedId);
      setLocalNotifications(prev => prev.filter(n => n.id !== deletedId));
      if (deleted && !deleted.isRead) {
        setLocalUnreadCount(prev => Math.max(0, prev - 1));
      }
    },
  });

  const markAsRead = useCallback(async (notificationId: string) => {
    await markAsReadMutation.mutateAsync(notificationId);
  }, [markAsReadMutation]);

  const markAllAsRead = useCallback(async () => {
    await markAllAsReadMutation.mutateAsync();
  }, [markAllAsReadMutation]);

  const deleteNotification = useCallback(async (notificationId: string) => {
    await deleteNotificationMutation.mutateAsync(notificationId);
  }, [deleteNotificationMutation]);

  const fetchMore = useCallback(() => {
    if (notificationData && currentPage < notificationData.totalPages - 1) {
      setCurrentPage(prev => prev + 1);
    }
  }, [notificationData, currentPage]);

  const refresh = useCallback(() => {
    setCurrentPage(0);
    refetch();
  }, [refetch]);

  const value: NotificationContextValue = {
    notifications: localNotifications,
    unreadCount: localUnreadCount,
    isLoading,
    isConnected,
    totalPages: notificationData?.totalPages ?? 0,
    currentPage,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    fetchMore,
    refresh,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
}
