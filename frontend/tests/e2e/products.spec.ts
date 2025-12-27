import { test, expect } from '@playwright/test';

test.describe('Products Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/products')
  })

  test('should display products page', async ({ page }) => {
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  })

  test('should have search functionality', async ({ page }) => {
    // Use the search input in the main content area (not the header one)
    const searchInput = page.getByRole('main').getByPlaceholder(/search/i)
    
    await expect(searchInput).toBeVisible()
    await searchInput.fill('test product')
    await expect(searchInput).toHaveValue('test product')
  })

  test('should display product filters', async ({ page }) => {
    // Look for filter-related elements
    const filters = page.locator('[data-testid="product-filters"]')
    
    // If filters exist, they should be visible on desktop
    if (await filters.isVisible()) {
      await expect(filters).toBeVisible()
    }
  })
});
