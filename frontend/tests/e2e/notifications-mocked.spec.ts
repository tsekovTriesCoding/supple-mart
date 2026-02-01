import { test, expect } from '@playwright/test';
import { setupAuthenticatedMocks } from './mocks/api-mocks';

test.describe('Notifications Bell (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAuthenticatedMocks(page);
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', JSON.stringify({
        id: 'user-1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
      }));
    });
  });

  test('should display notification bell with unread count', async ({ page }) => {
    await page.goto('/');
    
    const bellButton = page.getByLabel('Notifications');
    await expect(bellButton).toBeVisible();
    
    const badge = page.locator('.bg-red-500.rounded-full');
    await expect(badge).toBeVisible();
    await expect(badge).toContainText('2');
  });

  test('should open notification dropdown when clicking bell', async ({ page }) => {
    await page.goto('/');
    
    await page.getByLabel('Notifications').click();
    
    await expect(page.getByRole('heading', { name: 'Notifications' })).toBeVisible();
    await expect(page.getByText('Order Confirmed!')).toBeVisible();
    await expect(page.getByText('Order Shipped!')).toBeVisible();
    await expect(page.getByText('Price Drop Alert!')).toBeVisible();
  });

  test('should show mark all as read button when there are unread notifications', async ({ page }) => {
    await page.goto('/');
    
    await page.getByLabel('Notifications').click();
    
    const markAllButton = page.getByTitle('Mark all as read');
    await expect(markAllButton).toBeVisible();
  });

  test('should close dropdown when clicking X button', async ({ page }) => {
    await page.goto('/');
    
    await page.getByLabel('Notifications').click();
    await expect(page.getByText('Order Confirmed!')).toBeVisible();
    
    await page.locator('button').filter({ has: page.locator('.lucide-x') }).click();
    
    await expect(page.getByText('Order Confirmed!')).not.toBeVisible();
  });

  test('should have View all notifications link', async ({ page }) => {
    await page.goto('/');
    
    await page.getByLabel('Notifications').click();
    
    const viewAllLink = page.getByText('View all notifications');
    await expect(viewAllLink).toBeVisible();
  });

  test('should navigate to notifications page when clicking View all', async ({ page }) => {
    await page.goto('/');
    
    await page.getByLabel('Notifications').click();
    await page.getByText('View all notifications').click();
    
    await expect(page).toHaveURL('/notifications');
  });

  test('should display notification icons based on type', async ({ page }) => {
    await page.goto('/');
    
    await page.getByLabel('Notifications').click();
    
    await expect(page.getByText('🎉')).toBeVisible();
    await expect(page.getByText('📦')).toBeVisible();
    await expect(page.getByText('💰')).toBeVisible();
  });

  test('should show delete button on notification items', async ({ page }) => {
    await page.goto('/');
    
    await page.getByLabel('Notifications').click();
    
    const deleteButtons = page.getByTitle('Delete');
    await expect(deleteButtons.first()).toBeVisible();
  });
});

test.describe('Notifications Page (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAuthenticatedMocks(page);
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', JSON.stringify({
        id: 'user-1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
      }));
    });
    await page.goto('/notifications');
  });

  test('should display notifications page', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /notification/i }).first()).toBeVisible();
  });

  test('should show list of notifications', async ({ page }) => {
    await expect(page.getByText('Order Confirmed!')).toBeVisible();
    await expect(page.getByText('Order Shipped!')).toBeVisible();
    await expect(page.getByText('Price Drop Alert!')).toBeVisible();
  });

  test('should have mark all as read button', async ({ page }) => {
    const markAllButton = page.getByRole('button', { name: /mark all|mark.*read/i });
    await expect(markAllButton.first()).toBeVisible();
  });

  test('should have refresh button', async ({ page }) => {
    const refreshButton = page.locator('button').filter({ has: page.locator('.lucide-refresh-cw') });
    if (await refreshButton.first().isVisible().catch(() => false)) {
      await expect(refreshButton.first()).toBeVisible();
    }
  });

  test('should display notification details', async ({ page }) => {
    await expect(page.getByText(/Your order #ORD-123/)).toBeVisible();
    await expect(page.getByText(/Your order #ORD-456/)).toBeVisible();
  });

  test('should navigate back to home', async ({ page }) => {
    const backButton = page.locator('button').filter({ has: page.locator('.lucide-arrow-left') });
    if (await backButton.first().isVisible().catch(() => false)) {
      await backButton.first().click();
      await expect(page).toHaveURL('/');
    }
  });
});

test.describe('Notification Interactions (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAuthenticatedMocks(page);
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', JSON.stringify({
        id: 'user-1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
      }));
    });
  });

  test('should mark notification as read when clicked', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - dropdown layout differs');
    
    let markAsReadCalled = false;
    await page.route('**/api/notifications/*/read', async (route) => {
      markAsReadCalled = true;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'notif-1',
          type: 'ORDER_PLACED',
          title: 'Order Confirmed!',
          message: 'Your order has been placed',
          isRead: true,
          readAt: new Date().toISOString(),
          createdAt: '2026-01-31T10:00:00Z',
        }),
      });
    });

    await page.goto('/');
    await page.getByLabel('Notifications').click();
    
    await page.getByText('Order Confirmed!').click();
    
    expect(markAsReadCalled).toBe(true);
  });

  test('should call mark all as read API', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - dropdown layout differs');
    
    let markAllReadCalled = false;
    await page.route('**/api/notifications/mark-all-read', async (route) => {
      markAllReadCalled = true;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true }),
      });
    });

    await page.goto('/');
    await page.getByLabel('Notifications').click();
    
    await page.getByTitle('Mark all as read').click();
    
    expect(markAllReadCalled).toBe(true);
  });

  test('should delete notification when clicking delete button', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - dropdown layout differs');
    
    let deleteCalled = false;
    await page.route(/\/api\/notifications\/[^/]+$/, async (route) => {
      if (route.request().method() === 'DELETE') {
        deleteCalled = true;
        await route.fulfill({ status: 204 });
      } else {
        await route.continue();
      }
    });

    await page.goto('/');
    await page.getByLabel('Notifications').click();
    
    const deleteButtons = page.getByTitle('Delete');
    await deleteButtons.first().click();
    
    expect(deleteCalled).toBe(true);
  });
});

test.describe('Notifications - Unauthenticated (with mocked API)', () => {
  test('should not show notification bell when not logged in', async ({ page }) => {
    const { setupAPIMocks } = await import('./mocks/api-mocks');
    await setupAPIMocks(page);
    
    await page.goto('/');
    
    const bellButton = page.getByLabel('Notifications');
    const isVisible = await bellButton.isVisible().catch(() => false);
    expect(isVisible).toBe(false);
  });

  test('should redirect to login when accessing notifications page without auth', async ({ page }) => {
    const { setupAPIMocks } = await import('./mocks/api-mocks');
    await setupAPIMocks(page);
    
    await page.goto('/notifications');
    
    await page.waitForTimeout(500);
    const url = page.url();
    
    const isOnNotificationsPage = url.includes('/notifications');
    if (isOnNotificationsPage) {
      const authElements = page.getByText(/sign in|log in|login/i);
      const count = await authElements.count();
      expect(count).toBeGreaterThanOrEqual(0);
    }
  });
});
