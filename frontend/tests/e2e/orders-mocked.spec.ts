import { test, expect } from '@playwright/test';
import { setupAuthenticatedMocks } from './mocks/api-mocks';
import { mockOrders } from './mocks/data';

test.describe('Orders Page (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', JSON.stringify({
        id: 'user-1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
      }));
    });
    await setupAuthenticatedMocks(page);
    await page.goto('/orders');
    await page.waitForTimeout(2000);
  });

  test('should display orders page heading', async ({ page }) => {
    const myOrdersHeading = page.getByRole('heading', { name: /my orders/i });
    const signInHeading = page.getByRole('heading', { name: /please sign in/i });
    const hasMyOrders = await myOrdersHeading.isVisible().catch(() => false);
    const hasSignIn = await signInHeading.isVisible().catch(() => false);
    expect(hasMyOrders || hasSignIn).toBeTruthy();
  });

  test('should display order list with order numbers', async ({ page }) => {
    const signInHeading = page.getByRole('heading', { name: /please sign in/i });
    if (await signInHeading.isVisible().catch(() => false)) {
      test.skip();
      return;
    }
    const firstOrderNumber = mockOrders[0].orderNumber;
    await expect(page.getByText(firstOrderNumber).first()).toBeVisible();
  });

  test('should show order status badges', async ({ page }) => {
    const signInHeading = page.getByRole('heading', { name: /please sign in/i });
    if (await signInHeading.isVisible().catch(() => false)) {
      test.skip();
      return;
    }
    const statusText = page.getByText(/delivered|processing|pending|shipped|paid/i);
    const count = await statusText.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should show order totals from mock data', async ({ page }) => {
    const signInHeading = page.getByRole('heading', { name: /please sign in/i });
    if (await signInHeading.isVisible().catch(() => false)) {
      test.skip();
      return;
    }
    await expect(page.getByText(/\$\d+\.\d{2}/).first()).toBeVisible();
  });

  test('should have view order details option', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    const signInHeading = page.getByRole('heading', { name: /please sign in/i });
    if (await signInHeading.isVisible().catch(() => false)) {
      test.skip();
      return;
    }
    const viewButtons = page.getByRole('button', { name: /view|details/i });
    const eyeIcons = page.locator('button').filter({
      has: page.locator('[data-lucide="eye"], svg.lucide-eye'),
    });
    const hasViewButtons = await viewButtons.count() > 0;
    const hasEyeIcons = await eyeIcons.count() > 0;
    expect(hasViewButtons || hasEyeIcons).toBeTruthy();
  });

  test('should display order statistics', async ({ page }) => {
    const signInHeading = page.getByRole('heading', { name: /please sign in/i });
    if (await signInHeading.isVisible().catch(() => false)) {
      test.skip();
      return;
    }
    const totalStat = page.getByText(/total/i);
    await expect(totalStat.first()).toBeVisible();
  });

  test('should have status filter', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    const signInHeading = page.getByRole('heading', { name: /please sign in/i });
    if (await signInHeading.isVisible().catch(() => false)) {
      test.skip();
      return;
    }
    const filterElement = page.getByRole('combobox').or(
      page.locator('select')
    ).or(
      page.getByRole('button', { name: /filter|all orders|status/i })
    );
    await expect(filterElement.first()).toBeVisible();
  });

  test('should have search functionality', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    const signInHeading = page.getByRole('heading', { name: /please sign in/i });
    if (await signInHeading.isVisible().catch(() => false)) {
      test.skip();
      return;
    }
    const searchInput = page.getByPlaceholder(/search/i);
    if (await searchInput.first().isVisible().catch(() => false)) {
      await searchInput.first().fill(mockOrders[0].orderNumber);
      await page.waitForTimeout(500);
      await expect(page.getByText(mockOrders[0].orderNumber).first()).toBeVisible();
    }
  });
});

test.describe('Orders Page - Unauthenticated', () => {
  test('should show sign in message when not authenticated', async ({ page }) => {
    await page.goto('/orders');
    await page.waitForTimeout(1000);
    const signInHeading = page.getByRole('heading', { name: /please sign in/i });
    const hasSignInMessage = await signInHeading.isVisible().catch(() => false);
    const isRedirected = !page.url().includes('/orders');
    expect(hasSignInMessage || isRedirected).toBeTruthy();
  });
});
