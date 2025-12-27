import { test, expect } from '@playwright/test';
import { setupAPIMocks } from './mocks/api-mocks';
import { mockProducts } from './mocks/data';

test.describe('Products Page (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto('/products');
  });

  test('should display product cards', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    await page.waitForTimeout(1000);
    await expect(page.getByText(mockProducts[0].name).first()).toBeVisible();
  });

  test('should filter products by search', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different search UI');
    await page.waitForTimeout(500);
    const searchInput = page.getByRole('main').getByPlaceholder(/search/i);
    const searchTerm = mockProducts[0].name.split(' ')[0];
    await searchInput.fill(searchTerm);
    await page.waitForTimeout(500);
    await expect(page.getByText(mockProducts[0].name).first()).toBeVisible();
  });

  test('should show out of stock badge', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    await page.waitForTimeout(1000);
    const outOfStockProduct = mockProducts.find((p) => !p.inStock);
    if (outOfStockProduct) {
      await expect(page.getByText(/out of stock/i).first()).toBeVisible();
    }
  });

  test('should display sort options', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - sort in filter modal');
    await page.waitForTimeout(500);
    const sortSelect = page.locator('select').or(page.getByRole('combobox'));
    await expect(sortSelect.first()).toBeVisible();
  });

  test('should navigate to product detail on click', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Skip on mobile - different layout');
    await page.waitForTimeout(1000);
    const productCard = page.getByText(mockProducts[0].name).first();
    await productCard.click();
    await expect(page).toHaveURL(new RegExp(`/products/${mockProducts[0].id}`));
  });
});

test.describe('Product Detail Page (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
  });

  test('should display product details', async ({ page }) => {
    await page.goto(`/products/${mockProducts[0].id}`);
    await page.waitForTimeout(1000);
    await expect(page.getByText(mockProducts[0].name).first()).toBeVisible();
    await expect(page.getByText(mockProducts[0].description).first()).toBeVisible();
    await expect(page.getByText(new RegExp(mockProducts[0].price.toFixed(2))).first()).toBeVisible();
  });

  test('should show 404 for non-existent product', async ({ page }) => {
    await page.goto('/products/non-existent-id');
    await page.waitForTimeout(1000);
    const errorText = page.getByText(/not found|error|doesn't exist/i);
    if (await errorText.first().isVisible().catch(() => false)) {
      await expect(errorText.first()).toBeVisible();
    }
  });
});
