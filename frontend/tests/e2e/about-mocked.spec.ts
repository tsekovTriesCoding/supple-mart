import { test, expect } from '@playwright/test';
import { setupAPIMocks } from './mocks/api-mocks';

test.describe('About Page (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto('/about');
  });

  test('should display about page heading', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /about/i })).toBeVisible();
  });

  test('should display company statistics', async ({ page }) => {
    await expect(page.getByText(/happy customers/i).first()).toBeVisible();
    await expect(page.getByText(/premium products/i).first()).toBeVisible();
  });

  test('should display company values section', async ({ page }) => {
    const valuesHeading = page.getByRole('heading', { name: /values/i }).or(
      page.getByText(/health first|quality assurance|personalized/i)
    );
    await expect(valuesHeading.first()).toBeVisible();
  });

  test('should display team members section', async ({ page }) => {
    await expect(page.getByText(/nutritionist|manager|advisor/i).first()).toBeVisible();
  });

  test('should display company milestones/timeline', async ({ page }) => {
    await expect(page.getByText(/2018|2019|2021|2023|2024/i).first()).toBeVisible();
  });
});
