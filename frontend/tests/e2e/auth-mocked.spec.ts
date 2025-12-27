import { test, expect } from '@playwright/test';
import { setupAPIMocks, setupAuthenticatedMocks } from './mocks/api-mocks';

async function openAuthModal(page: import('@playwright/test').Page) {
  const userButton = page.locator('button').filter({ has: page.locator('svg.lucide-user, [data-lucide="user"]') }).first();
  await userButton.hover();
  await page.waitForTimeout(300);
  
  const signInButton = page.getByText(/sign in \/ create account/i);
  await signInButton.click();
  await page.waitForTimeout(300);
}

test.describe('Authentication Flow (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
  });

  test('should show auth modal when clicking Sign In', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different navigation');
    
    await page.goto('/');
    await page.waitForTimeout(500);
    await openAuthModal(page);

    await expect(page.getByText(/welcome back|sign in to/i).first()).toBeVisible();
  });

  test('should show login form in auth modal', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different navigation');
    
    await page.goto('/');
    await page.waitForTimeout(500);
    await openAuthModal(page);

    const emailInput = page.getByPlaceholder(/email/i);
    await expect(emailInput.first()).toBeVisible();
  });

  test('should toggle between login and register forms', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different navigation');
    
    await page.goto('/');
    await page.waitForTimeout(500);
    await openAuthModal(page);

    const registerLink = page.getByText(/sign up|don't have an account/i).first();
    await expect(registerLink).toBeVisible();
    await registerLink.click();
    await page.waitForTimeout(500);

    const firstNameInput = page.getByPlaceholder(/first name/i);
    await expect(firstNameInput.first()).toBeVisible();
  });

  test('should close modal on X button click', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different UI');
    
    await page.goto('/');
    await page.waitForTimeout(500);
    await openAuthModal(page);
    await expect(page.getByText(/welcome back/i)).toBeVisible();

    const closeButton = page.locator('button').filter({ has: page.locator('svg.lucide-x, [data-lucide="x"]') }).first();
    await closeButton.click();
    await page.waitForTimeout(300);
    
    await expect(page.getByText(/welcome back/i)).not.toBeVisible();
  });
});

test.describe('Authenticated User (with mocked API)', () => {
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
  });

  test('should show user menu when authenticated', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different UI');
    
    await page.goto('/');
    await page.waitForTimeout(500);

    const userButton = page.locator('button').filter({ has: page.locator('svg.lucide-user, img') }).first();
    await userButton.hover();
    await page.waitForTimeout(300);

    const userContent = page.getByText(/john|my account|account/i);
    await expect(userContent.first()).toBeVisible();
  });
});
