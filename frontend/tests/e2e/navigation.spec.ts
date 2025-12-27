import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('should navigate between main pages', async ({ page, isMobile }) => {
    await page.goto('/')

    // On mobile, open the hamburger menu first
    if (isMobile) {
      const menuButton = page.locator('button.md\\:hidden')
      await menuButton.click()
      await page.waitForTimeout(300) // Wait for animation
    }

    // Navigate to About page
    await page.getByRole('link', { name: /about/i }).first().click()
    await expect(page).toHaveURL(/\/about/)

    // On mobile, re-open the menu for next navigation
    if (isMobile) {
      const menuButton = page.locator('button.md\\:hidden')
      await menuButton.click()
      await page.waitForTimeout(300)
    }

    // Navigate to Contact page
    await page.getByRole('link', { name: /contact/i }).first().click()
    await expect(page).toHaveURL(/\/contact/)
  })

  test('should show cart link', async ({ page }) => {
    await page.goto('/')
    
    // Cart link has a shopping cart icon, find by href
    const cartLink = page.locator('a[href="/cart"]')
    await expect(cartLink.first()).toBeVisible()
  })
})

test.describe('Authentication UI', () => {
  test('should show login/signup buttons when not authenticated', async ({ page, isMobile }) => {
    await page.goto('/')
    
    if (isMobile) {
      // On mobile, open the hamburger menu first
      const menuButton = page.locator('button.md\\:hidden')
      await menuButton.click()
      await page.waitForTimeout(300)
      
      // Mobile has "Sign In" button in the menu
      const signInButton = page.getByRole('button', { name: /sign in/i })
      await expect(signInButton).toBeVisible()
    } else {
      // Desktop: Look for authentication-related UI elements
      const loginButton = page.getByRole('button', { name: /login|sign in/i })
      const signupButton = page.getByRole('button', { name: /signup|sign up|register/i })
      
      // At least one auth button should be visible
      const loginVisible = await loginButton.first().isVisible().catch(() => false)
      const signupVisible = await signupButton.first().isVisible().catch(() => false)
      
      expect(loginVisible || signupVisible).toBeTruthy()
    }
  })
});
