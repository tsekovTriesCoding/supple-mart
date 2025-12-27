import { test, expect } from '@playwright/test';
import { setupAuthenticatedMocks, setupEmptyWishlistMocks } from './mocks/api-mocks';
import { mockWishlistItems } from './mocks/data';

test.describe('Wishlist Page (with mocked API)', () => {
  test.describe('Authenticated with items', () => {
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
      await page.goto('/wishlist');
    });

    test('should display wishlist heading', async ({ page }) => {
      await expect(page.getByRole('heading', { name: /wishlist/i }).first()).toBeVisible();
    });

    test('should display wishlist items', async ({ page }) => {
      await page.waitForTimeout(1000);
      for (const item of mockWishlistItems) {
        await expect(page.getByText(item.productName).first()).toBeVisible();
      }
    });

    test('should show item prices', async ({ page }) => {
      await page.waitForTimeout(1000);
      await expect(page.getByText(/\$\d+\.\d{2}/).first()).toBeVisible();
    });

    test('should have add to cart buttons', async ({ page }) => {
      await page.waitForTimeout(1000);
      const addToCartButtons = page.getByRole('button', { name: /add to cart/i });
      const count = await addToCartButtons.count();
      expect(count).toBeGreaterThan(0);
    });

    test('should have remove from wishlist option', async ({ page }) => {
      await page.waitForTimeout(1000);
      const heartButtons = page.locator('button').filter({
        has: page.locator('svg.lucide-heart'),
      });
      const count = await heartButtons.count();
      expect(count).toBeGreaterThan(0);
    });
  });

  test.describe('Empty wishlist', () => {
    test.beforeEach(async ({ page }) => {
      await setupEmptyWishlistMocks(page);
      await page.addInitScript(() => {
        localStorage.setItem('token', 'mock-token');
        localStorage.setItem('user', JSON.stringify({
          id: 'user-1',
          email: 'test@example.com',
          firstName: 'John',
          lastName: 'Doe',
        }));
      });
      await page.goto('/wishlist');
    });

    test('should show empty wishlist message', async ({ page }) => {
      await expect(page.getByText(/empty|no items/i).first()).toBeVisible();
    });

    test('should have link to browse products', async ({ page }) => {
      const browseLink = page.getByRole('link', { name: /browse|shop|products/i });
      await expect(browseLink.first()).toBeVisible();
    });
  });

  test.describe('Unauthenticated', () => {
    test('should redirect or show sign in message', async ({ page }) => {
      await page.goto('/wishlist');
      const mainContent = page.getByRole('main');
      const signInMessage = mainContent.getByText(/sign in|log in|please/i);
      await expect(signInMessage.first()).toBeVisible({ timeout: 5000 });
    });
  });
});
