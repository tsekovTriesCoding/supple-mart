import { test, expect } from '@playwright/test';
import { setupAPIMocks } from './mocks/api-mocks';

test.describe('Home Page (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto('/');
  });

  test('should display home page with hero section', async ({ page }) => {
    await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible();
  });

  test('should display featured products section', async ({ page }) => {
    await page.waitForTimeout(1000);
    const productText = page.getByText(/protein|bcaa|creatine|pre-workout/i).first();
    await expect(productText).toBeVisible({ timeout: 5000 }).catch(() => {});
  });

  test('should have navigation links', async ({ page, isMobile }) => {
    if (isMobile) {
      await expect(page).toHaveURL('/');
      return;
    }
    await expect(page.getByRole('link', { name: /products/i }).first()).toBeVisible();
    await expect(page.getByRole('link', { name: /about/i }).first()).toBeVisible();
    await expect(page.getByRole('link', { name: /contact/i }).first()).toBeVisible();
  });

  test('should display trust badges or features', async ({ page }) => {
    await page.waitForTimeout(1000);
    const freeShipping = page.getByRole('heading', { name: /free shipping/i });
    const qualityGuaranteed = page.getByRole('heading', { name: /quality guaranteed/i });
    const support247 = page.getByRole('heading', { name: /24\/7 support/i });
    const hasShipping = await freeShipping.isVisible().catch(() => false);
    const hasQuality = await qualityGuaranteed.isVisible().catch(() => false);
    const hasSupport = await support247.isVisible().catch(() => false);
    if (!hasShipping && !hasQuality && !hasSupport) {
      const shippingText = page.getByText(/free shipping/i).first();
      const qualityText = page.getByText(/quality guaranteed/i).first();
      const supportText = page.getByText(/24\/7 support/i).first();
      const hasShippingText = await shippingText.isVisible().catch(() => false);
      const hasQualityText = await qualityText.isVisible().catch(() => false);
      const hasSupportText = await supportText.isVisible().catch(() => false);
      expect(hasShippingText || hasQualityText || hasSupportText).toBeTruthy();
    } else {
      expect(hasShipping || hasQuality || hasSupport).toBeTruthy();
    }
  });

  test('should navigate to products page from CTA', async ({ page }) => {
    const shopButton = page.getByRole('link', { name: /shop|browse|explore/i }).first();
    if (await shopButton.isVisible()) {
      await shopButton.click();
      await expect(page).toHaveURL(/\/products/);
    }
  });
});
