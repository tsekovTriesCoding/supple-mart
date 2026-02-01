import { describe, it, expect, vi, beforeEach, type Mock } from 'vitest';
import { render, screen, waitFor } from '../test/test-utils';

import { NotificationBell } from './NotificationBell';
import { useNotifications } from '../hooks/useNotifications';

vi.mock('../hooks/useNotifications');

const mockNotifications = [
  {
    id: 'notif-1',
    type: 'ORDER_PLACED',
    title: 'Order Confirmed!',
    message: 'Your order #ORD-123 has been placed successfully',
    isRead: false,
    createdAt: new Date().toISOString(),
    actionUrl: '/orders',
  },
  {
    id: 'notif-2',
    type: 'ORDER_SHIPPED',
    title: 'Order Shipped!',
    message: 'Your order #ORD-456 is on its way',
    isRead: true,
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    actionUrl: '/orders',
  },
];

const defaultMockReturn = {
  notifications: mockNotifications,
  unreadCount: 1,
  isConnected: true,
  isLoading: false,
  markAsRead: vi.fn(),
  markAllAsRead: vi.fn(),
  deleteNotification: vi.fn(),
  fetchMore: vi.fn(),
  refresh: vi.fn(),
  totalPages: 1,
  currentPage: 0,
};

describe('NotificationBell', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (useNotifications as Mock).mockReturnValue(defaultMockReturn);
  });

  it('renders the bell icon', () => {
    render(<NotificationBell />);
    expect(screen.getByLabelText('Notifications')).toBeInTheDocument();
  });

  it('displays unread count badge when there are unread notifications', () => {
    render(<NotificationBell />);
    expect(screen.getByText('1')).toBeInTheDocument();
  });

  it('does not display badge when unread count is 0', () => {
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      unreadCount: 0,
    });
    render(<NotificationBell />);
    expect(screen.queryByText('0')).not.toBeInTheDocument();
  });

  it('displays 99+ when unread count exceeds 99', () => {
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      unreadCount: 150,
    });
    render(<NotificationBell />);
    expect(screen.getByText('99+')).toBeInTheDocument();
  });

  it('shows disconnected indicator when not connected', () => {
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      isConnected: false,
    });
    render(<NotificationBell />);
    const disconnectedIndicator = document.querySelector('[title*="Disconnected"]');
    expect(disconnectedIndicator).toBeInTheDocument();
  });

  it('opens dropdown when bell is clicked', async () => {
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    
    expect(screen.getByText('Notifications')).toBeInTheDocument();
    expect(screen.getByText('Order Confirmed!')).toBeInTheDocument();
    expect(screen.getByText('Order Shipped!')).toBeInTheDocument();
  });

  it('closes dropdown when clicking close button', async () => {
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    expect(screen.getByText('Order Confirmed!')).toBeInTheDocument();
    
    const closeButtons = screen.getAllByRole('button');
    const closeButton = closeButtons.find(btn => btn.querySelector('.lucide-x'));
    if (closeButton) {
      await user.click(closeButton);
    }
    
    await waitFor(() => {
      expect(screen.queryByText('Order Confirmed!')).not.toBeInTheDocument();
    });
  });

  it('shows empty state when no notifications', async () => {
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      notifications: [],
      unreadCount: 0,
    });
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    
    expect(screen.getByText('No notifications yet')).toBeInTheDocument();
  });

  it('shows loading spinner when loading', async () => {
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      notifications: [],
      isLoading: true,
    });
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    
    expect(document.querySelector('.animate-spin')).toBeInTheDocument();
  });

  it('calls markAsRead when clicking unread notification', async () => {
    const markAsRead = vi.fn();
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      markAsRead,
    });
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    await user.click(screen.getByText('Order Confirmed!'));
    
    expect(markAsRead).toHaveBeenCalledWith('notif-1');
  });

  it('does not call markAsRead when clicking already read notification', async () => {
    const markAsRead = vi.fn();
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      markAsRead,
    });
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    await user.click(screen.getByText('Order Shipped!'));
    
    expect(markAsRead).not.toHaveBeenCalled();
  });

  it('calls markAllAsRead when clicking mark all button', async () => {
    const markAllAsRead = vi.fn();
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      markAllAsRead,
    });
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    const markAllButton = screen.getByTitle('Mark all as read');
    await user.click(markAllButton);
    
    expect(markAllAsRead).toHaveBeenCalled();
  });

  it('calls deleteNotification when clicking delete button', async () => {
    const deleteNotification = vi.fn();
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      deleteNotification,
    });
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    const deleteButtons = screen.getAllByTitle('Delete');
    await user.click(deleteButtons[0]);
    
    expect(deleteNotification).toHaveBeenCalledWith('notif-1');
  });

  it('shows Load more button when there are more pages', async () => {
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      totalPages: 3,
      currentPage: 0,
    });
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    
    expect(screen.getByText('Load more')).toBeInTheDocument();
  });

  it('calls fetchMore when clicking Load more', async () => {
    const fetchMore = vi.fn();
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      fetchMore,
      totalPages: 3,
      currentPage: 0,
    });
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    await user.click(screen.getByText('Load more'));
    
    expect(fetchMore).toHaveBeenCalled();
  });

  it('hides Load more when on last page', async () => {
    (useNotifications as Mock).mockReturnValue({
      ...defaultMockReturn,
      totalPages: 2,
      currentPage: 1,
    });
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    
    expect(screen.queryByText('Load more')).not.toBeInTheDocument();
  });

  it('has View all notifications link', async () => {
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    
    expect(screen.getByText('View all notifications')).toBeInTheDocument();
  });

  it('displays correct icon for notification types', async () => {
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    
    expect(screen.getByText('🎉')).toBeInTheDocument();
    expect(screen.getByText('📦')).toBeInTheDocument();
  });

  it('shows unread indicator dot for unread notifications', async () => {
    const { user } = render(<NotificationBell />);
    
    await user.click(screen.getByLabelText('Notifications'));
    
    const unreadDots = document.querySelectorAll('.bg-blue-500.rounded-full.w-2.h-2');
    expect(unreadDots.length).toBeGreaterThan(0);
  });
});
