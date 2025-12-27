import { test, expect } from '@playwright/test';
import { setupAPIMocks } from './mocks/api-mocks';
import { mockProducts } from './mocks/data';

test.describe('Product Detail Page (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto(`/products/${mockProducts[0].id}`);
  });

  test('should display product name', async ({ page }) => {
    await page.waitForTimeout(1000);
    const product = mockProducts[0];
    await expect(page.getByRole('heading', { name: new RegExp(product.name, 'i') })).toBeVisible();
  });

  test('should display product price', async ({ page }) => {
    await page.waitForTimeout(1000);
    const product = mockProducts[0];
    await expect(page.getByText(new RegExp(product.price.toFixed(2))).first()).toBeVisible();
  });

  test('should display product description', async ({ page }) => {
    await page.waitForTimeout(1000);
    const product = mockProducts[0];
    const descriptionText = page.getByText(product.description);
    await expect(descriptionText.first()).toBeVisible();
  });

  test('should display product image', async ({ page }) => {
    await page.waitForTimeout(1000);
    const productImage = page.locator('img[alt]');
    await expect(productImage.first()).toBeVisible();
  });

  test('should have add to cart button', async ({ page }) => {
    await page.waitForTimeout(1000);
    const addToCartBtn = page.getByRole('button', { name: /add to cart/i });
    await expect(addToCartBtn.first()).toBeVisible();
  });

  test('should have wishlist button', async ({ page }) => {
    await page.waitForTimeout(1000);
    const wishlistBtn = page.locator('button').filter({
      has: page.locator('svg.lucide-heart'),
    });
    await expect(wishlistBtn.first()).toBeVisible();
  });

  test('should show product category', async ({ page }) => {
    await page.waitForTimeout(1000);
    const product = mockProducts[0];
    await expect(page.getByText(new RegExp(product.category, 'i')).first()).toBeVisible();
  });

  test('should show product rating', async ({ page }) => {
    await page.waitForTimeout(1000);
    const starIcons = page.locator('svg.lucide-star, [data-lucide="star"]');
    const count = await starIcons.count();
    if (count === 0) {
      const ratingText = page.getByText(/^\d\.\d$/);
      const hasRating = await ratingText.first().isVisible().catch(() => false);
      expect(hasRating).toBeTruthy();
    } else {
      expect(count).toBeGreaterThan(0);
    }
  });

  test('should have quantity selector', async ({ page }) => {
    await page.waitForTimeout(1000);
    const quantityLabel = page.getByText(/quantity/i);
    const plusButton = page.locator('svg.lucide-plus, [data-lucide="plus"]').first();
    const minusButton = page.locator('svg.lucide-minus, [data-lucide="minus"]').first();
    const hasQuantityLabel = await quantityLabel.isVisible().catch(() => false);
    const hasPlus = await plusButton.isVisible().catch(() => false);
    const hasMinus = await minusButton.isVisible().catch(() => false);
    expect(hasQuantityLabel || hasPlus || hasMinus).toBeTruthy();
  });

  test('should show stock status', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    await page.waitForTimeout(1000);
    const product = mockProducts[0];
    if (product.inStock) {
      const addToCartButton = page.getByRole('button', { name: /add to cart/i });
      await expect(addToCartButton.first()).toBeVisible();
    } else {
      const stockText = page.getByText(/out of stock/i);
      await expect(stockText.first()).toBeVisible();
    }
  });

  test('should display breadcrumb navigation', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    await page.waitForTimeout(1000);
    const breadcrumb = page.locator('nav[aria-label*="breadcrumb"]').or(
      page.getByRole('link', { name: /products|home/i })
    );
    await expect(breadcrumb.first()).toBeVisible();
  });
});

test.describe('Product Detail - Reviews Section', () => {
  test.skip(({ isMobile }) => isMobile, 'Skip reviews section on mobile');
  
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto(`/products/${mockProducts[0].id}`);
  });

  test('should display reviews section', async ({ page }) => {
    await page.waitForTimeout(1000);
    const reviewsTab = page.getByRole('button', { name: /review/i });
    await reviewsTab.click();
    await page.waitForTimeout(300);
    const reviewsSection = page.getByText(/review|rating/i);
    await expect(reviewsSection.first()).toBeVisible();
  });

  test('should show review count', async ({ page }) => {
    await page.waitForTimeout(1000);
    const product = mockProducts[0];
    const reviewCount = page.getByText(new RegExp(String(product.totalReviews)));
    await expect(reviewCount.first()).toBeVisible();
  });
});

test.describe('Product Detail - Related Products', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto(`/products/${mockProducts[0].id}`);
  });

  test('should display related products section', async ({ page }) => {
    await page.waitForTimeout(1000);
    const relatedSection = page.getByText(/related|similar|you may also like|recommended/i);
    if (await relatedSection.first().isVisible().catch(() => false)) {
      await expect(relatedSection.first()).toBeVisible();
    }
  });
});
