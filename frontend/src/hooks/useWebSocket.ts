import { useEffect, useRef, useCallback, useState } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { Notification, UnreadCountResponse } from '../types/notification';

// WebSocket URL - in production use relative path, in dev use localhost
const WS_URL = import.meta.env.VITE_WS_URL || 
  (import.meta.env.PROD ? '/ws' : 'http://localhost:8080/ws');

interface UseWebSocketOptions {
  onNotification?: (notification: Notification) => void;
  onUnreadCountUpdate?: (count: number) => void;
  onConnected?: () => void;
  onDisconnected?: () => void;
  onError?: (error: string) => void;
}

interface UseWebSocketReturn {
  isConnected: boolean;
  connect: () => void;
  disconnect: () => void;
}

export function useWebSocket(options: UseWebSocketOptions = {}): UseWebSocketReturn {
  const [isConnected, setIsConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<StompSubscription[]>([]);
  const reconnectAttemptsRef = useRef(0);
  const maxReconnectAttempts = 5;

  // Single ref for all options - updated synchronously on every render
  // This ensures callbacks are never stale when WebSocket messages arrive
  const optionsRef = useRef(options);
  optionsRef.current = options;

  const handleNotificationMessage = useCallback((message: IMessage) => {
    try {
      const notification: Notification = JSON.parse(message.body);
      optionsRef.current.onNotification?.(notification);
    } catch (e) {
      console.error('Failed to parse notification message:', e);
    }
  }, []);

  const handleUnreadCountMessage = useCallback((message: IMessage) => {
    try {
      const response: UnreadCountResponse = JSON.parse(message.body);
      optionsRef.current.onUnreadCountUpdate?.(response.count);
    } catch (e) {
      console.error('Failed to parse unread count message:', e);
    }
  }, []);

  const subscribe = useCallback((client: Client) => {
    // Clear existing subscriptions
    subscriptionsRef.current.forEach(sub => {
      try {
        sub.unsubscribe();
      } catch {
        // Ignore errors during unsubscribe
      }
    });
    subscriptionsRef.current = [];

    // Subscribe to user-specific notification queue
    const notificationSub = client.subscribe(
      '/user/queue/notifications',
      handleNotificationMessage
    );
    subscriptionsRef.current.push(notificationSub);

    // Subscribe to unread count updates
    const unreadCountSub = client.subscribe(
      '/user/queue/notifications/unread-count',
      handleUnreadCountMessage
    );
    subscriptionsRef.current.push(unreadCountSub);

    // Subscribe to broadcast notifications (system announcements)
    const broadcastSub = client.subscribe(
      '/topic/notifications/broadcast',
      handleNotificationMessage
    );
    subscriptionsRef.current.push(broadcastSub);
  }, [handleNotificationMessage, handleUnreadCountMessage]);

  const connect = useCallback(() => {
    const token = localStorage.getItem('token');
    
    if (!token) {
      return;
    }

    if (clientRef.current?.connected) {
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: import.meta.env.DEV ? (str) => console.log('STOMP:', str) : () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      
      onConnect: () => {
        setIsConnected(true);
        reconnectAttemptsRef.current = 0;
        subscribe(client);
        optionsRef.current.onConnected?.();
      },
      
      onDisconnect: () => {
        setIsConnected(false);
        optionsRef.current.onDisconnected?.();
      },
      
      onStompError: (frame) => {
        optionsRef.current.onError?.(frame.headers['message'] || 'WebSocket error');
        reconnectAttemptsRef.current++;
        if (reconnectAttemptsRef.current >= maxReconnectAttempts) {
          client.deactivate();
        }
      },
      
      onWebSocketError: () => {
        optionsRef.current.onError?.('WebSocket connection error');
      },
    });

    clientRef.current = client;
    client.activate();
  }, [subscribe]);

  const disconnect = useCallback(() => {
    // Unsubscribe from all channels
    subscriptionsRef.current.forEach(sub => {
      try {
        sub.unsubscribe();
      } catch {
        // Ignore errors during unsubscribe
      }
    });
    subscriptionsRef.current = [];

    // Deactivate client
    if (clientRef.current) {
      clientRef.current.deactivate();
      clientRef.current = null;
    }
    
    setIsConnected(false);
  }, []);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      disconnect();
    };
  }, [disconnect]);

  return {
    isConnected,
    connect,
    disconnect,
  };
}
