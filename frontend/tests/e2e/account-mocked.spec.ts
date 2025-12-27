import { test, expect } from '@playwright/test';
import { setupAuthenticatedMocks, setupAPIMocks } from './mocks/api-mocks';

test.describe('Account Page (with mocked API)', () => {
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
    await page.goto('/account');
    await page.waitForTimeout(1000);
  });

  test('should display account page heading', async ({ page }) => {
    const heading = page.getByRole('heading').first();
    await expect(heading).toBeVisible();
  });

  test('should show user information', async ({ page }) => {
    const mainContent = page.getByRole('main');
    const userContent = mainContent.getByText(/john|doe|test@example.com|profile|account/i);
    await expect(userContent.first()).toBeVisible();
  });

  test('should have editable profile fields', async ({ page }) => {
    const inputs = page.locator('input');
    const editButton = page.getByRole('button', { name: /edit/i });
    
    const hasInputs = await inputs.count() > 0;
    const hasEditButton = await editButton.first().isVisible().catch(() => false);
    
    expect(hasInputs || hasEditButton).toBeTruthy();
  });

  test('should have save/update button', async ({ page }) => {
    const actionButton = page.getByRole('button', { name: /save|update|edit/i });
    await expect(actionButton.first()).toBeVisible();
  });

  test('should have password change option', async ({ page }) => {
    const passwordOption = page.getByRole('button', { name: /password/i }).or(
      page.getByRole('link', { name: /password/i })
    ).or(
      page.getByText(/change password/i)
    );
    await expect(passwordOption.first()).toBeVisible();
  });
});

test.describe('Account Page - Unauthenticated', () => {
  test('should redirect to home when not authenticated', async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto('/account');
    await page.waitForTimeout(500);
    
    await expect(page).toHaveURL('/');
    await expect(page.getByRole('heading', { name: /premium health supplements/i })).toBeVisible();
  });
});
