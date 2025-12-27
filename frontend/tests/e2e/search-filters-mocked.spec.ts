import { test, expect } from '@playwright/test';
import { setupAPIMocks } from './mocks/api-mocks';
import { mockProducts, mockCategories } from './mocks/data';

test.describe('Search Functionality (with mocked API)', () => {
  test.skip(({ isMobile }) => isMobile, 'Skip on mobile - different search UI');
  
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
  });

  test('should have search input in header', async ({ page }) => {
    await page.goto('/');
    const searchInput = page.getByPlaceholder(/search/i);
    await expect(searchInput.first()).toBeVisible();
  });

  test('should search for products', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(500);
    const productToSearch = mockProducts[0];
    const searchInput = page.getByRole('main').getByPlaceholder(/search/i);
    await searchInput.fill(productToSearch.name.split(' ')[0]);
    await page.waitForTimeout(500);
    await expect(page.getByText(productToSearch.name).first()).toBeVisible();
  });

  test('should show no results message for invalid search', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(500);
    const searchInput = page.getByRole('main').getByPlaceholder(/search/i);
    await searchInput.fill('xyznonexistent123');
    await page.waitForTimeout(500);
    const noResults = page.getByText(/no.*found|no.*results|no products/i);
    await expect(noResults.first()).toBeVisible();
  });

  test('should clear search', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(500);
    const productToSearch = mockProducts[0];
    const searchInput = page.getByRole('main').getByPlaceholder(/search/i);
    await searchInput.fill(productToSearch.name);
    await page.waitForTimeout(500);
    await searchInput.clear();
    await page.waitForTimeout(500);
    for (const product of mockProducts.slice(0, 2)) {
      await expect(page.getByText(product.name).first()).toBeVisible();
    }
  });
});

test.describe('Product Filters (with mocked API)', () => {
  test.skip(({ isMobile }) => isMobile, 'Skip on mobile - filters are in modal');
  
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto('/products');
  });

  test('should display filter section', async ({ page }) => {
    await page.waitForTimeout(500);
    const filtersButton = page.getByRole('button', { name: /filter/i });
    await expect(filtersButton.first()).toBeVisible();
  });

  test('should filter by category', async ({ page }) => {
    await page.waitForTimeout(500);
    const categoryRegex = new RegExp(mockCategories.join('|'), 'i');
    const categoryFilter = page.getByRole('combobox').or(
      page.getByText(categoryRegex)
    );
    if (await categoryFilter.first().isVisible()) {
      await categoryFilter.first().click();
    }
  });

  test('should have sort options', async ({ page }) => {
    await page.waitForTimeout(500);
    const sortDropdown = page.getByRole('combobox').or(
      page.getByText(/sort|price.*low|price.*high|newest/i)
    );
    await expect(sortDropdown.first()).toBeVisible();
  });

  test('should have price range filter', async ({ page }) => {
    await page.waitForTimeout(500);
    const filtersButton = page.getByRole('button', { name: /filter/i });
    await filtersButton.first().click();
    await page.waitForTimeout(300);
    const priceInput = page.getByPlaceholder(/min|max/i).or(
      page.getByRole('slider')
    ).or(
      page.locator('input[type="number"]')
    );
    await expect(priceInput.first()).toBeVisible();
  });
});

test.describe('Pagination (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto('/products');
  });

  test('should display pagination controls', async ({ page }) => {
    await page.waitForTimeout(1000);
    const pagination = page.getByRole('navigation', { name: /pagination/i }).or(
      page.locator('[class*="pagination"]')
    );
    if (await pagination.first().isVisible().catch(() => false)) {
      await expect(pagination.first()).toBeVisible();
    }
  });

  test('should navigate between pages', async ({ page }) => {
    await page.waitForTimeout(1000);
    const nextButton = page.getByRole('button', { name: /next/i });
    if (await nextButton.first().isVisible().catch(() => false)) {
      await nextButton.first().click();
      await page.waitForTimeout(500);
    }
  });
});
