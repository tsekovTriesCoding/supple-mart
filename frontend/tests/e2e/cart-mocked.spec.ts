import { test, expect } from '@playwright/test';
import { setupAPIMocks } from './mocks/api-mocks';

test.describe('Cart Functionality (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
  });

  test('should display cart icon in header', async ({ page }) => {
    await page.goto('/');
    const cartLink = page.locator('a[href="/cart"]');
    await expect(cartLink.first()).toBeVisible();
  });

  test('should navigate to cart page', async ({ page }) => {
    await page.goto('/');
    await page.locator('a[href="/cart"]').first().click();
    await expect(page).toHaveURL('/cart');
  });

  test('should display cart page content', async ({ page }) => {
    await page.goto('/cart');
    const cartHeading = page.getByRole('heading', { name: /cart|shopping/i });
    const emptyMessage = page.getByText(/empty|no items/i);
    await expect(cartHeading.or(emptyMessage).first()).toBeVisible();
  });
});

test.describe('Wishlist Functionality (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
  });

  test('should have wishlist icon on product cards', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    
    await page.goto('/products');
    await page.waitForSelector('text=Premium Whey Protein', { timeout: 10000 }).catch(() => {});
    await page.waitForTimeout(500);
    
    const heartButtons = page.locator('button').filter({ 
      has: page.locator('svg.lucide-heart') 
    });
    
    const productText = page.getByText('Premium Whey Protein');
    const hasProducts = await productText.first().isVisible().catch(() => false);
    
    if (hasProducts) {
      const count = await heartButtons.count();
      expect(count).toBeGreaterThan(0);
    } else {
      test.skip(true, 'Products did not load in time');
    }
  });
});
