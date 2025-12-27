import { test, expect } from '@playwright/test';

test.describe('Home Page', () => {
  test('should load the home page', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveTitle(/SuppleMart/i)
  })

  test('should display the header with navigation', async ({ page }) => {
    await page.goto('/')
    
    // Check header is visible
    const header = page.locator('header')
    await expect(header).toBeVisible()
  })

  test('should navigate to products page', async ({ page }) => {
    await page.goto('/')
    
    // Find and click products link
    await page.getByRole('link', { name: /products/i }).first().click()
    
    // Verify navigation
    await expect(page).toHaveURL(/\/products/)
  })
});
