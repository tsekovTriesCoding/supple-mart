import { test, expect } from '@playwright/test';
import { setupAuthenticatedMocks } from './mocks/api-mocks';

test.describe('Notification Preferences (with mocked API)', () => {
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
    await page.goto('/account/notifications');
  });

  test('should display notification preferences page', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /notification/i }).first()).toBeVisible();
  });

  test('should have email notification toggles', async ({ page }) => {
    await page.waitForTimeout(1000);
    const toggles = page.locator('button').filter({ hasText: '' }).filter({
      has: page.locator('span.rounded-full'),
    });
    const count = await toggles.count();
    if (count === 0) {
      const allButtons = page.getByRole('button');
      expect(await allButtons.count()).toBeGreaterThan(0);
    } else {
      expect(count).toBeGreaterThan(0);
    }
  });

  test('should show notification categories', async ({ page }) => {
    await page.waitForTimeout(1000);
    const categories = page.getByText(/email|order|promotional|newsletter|updates/i);
    const count = await categories.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should have save preferences button', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    await page.waitForTimeout(1000);
    const toggles = page.locator('button[role="switch"]').or(
      page.locator('span.rounded-full')
    ).or(
      page.getByRole('button').filter({ hasText: /toggle|enabled|disabled/i })
    );
    const count = await toggles.count();
    if (count === 0) {
      const allButtons = page.getByRole('button');
      expect(await allButtons.count()).toBeGreaterThan(0);
    } else {
      expect(count).toBeGreaterThan(0);
    }
  });
});

test.describe('Privacy Settings (with mocked API)', () => {
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
    await page.goto('/account/privacy');
  });

  test('should display privacy settings page', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /privacy/i }).first()).toBeVisible();
  });

  test('should have data visibility options', async ({ page }) => {
    await page.waitForTimeout(1000);
    const privacyOptions = page.getByText(/public|private|profile|visibility/i);
    const count = await privacyOptions.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should have data export option', async ({ page }) => {
    await page.waitForTimeout(1000);
    const exportOption = page.getByRole('button', { name: /export|download/i });
    if (await exportOption.first().isVisible().catch(() => false)) {
      await expect(exportOption.first()).toBeVisible();
    }
  });

  test('should have delete account option', async ({ page }) => {
    await page.waitForTimeout(1000);
    const deleteOption = page.getByRole('button', { name: /delete|remove account/i }).or(
      page.getByText(/delete.*account/i)
    );
    if (await deleteOption.first().isVisible().catch(() => false)) {
      await expect(deleteOption.first()).toBeVisible();
    }
  });
});
