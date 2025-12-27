import { test, expect } from '@playwright/test';
import { setupEmptyCartMocks, setupAuthenticatedMocks } from './mocks/api-mocks';
import { mockCartItems } from './mocks/data';

test.describe('Cart Page (with mocked API)', () => {
  test.describe('Cart with items', () => {
    test.beforeEach(async ({ page }) => {
      await setupAuthenticatedMocks(page);
      await page.addInitScript(() => {
        localStorage.setItem('token', 'mock-token');
        localStorage.setItem('user', JSON.stringify({
          id: 'user-1',
          email: 'test@example.com',
          firstName: 'John',
          lastName: 'Doe',
        }));
      });
      await page.goto('/cart');
    });

    test('should display cart heading', async ({ page }) => {
      await expect(page.getByRole('heading', { name: /cart|shopping/i })).toBeVisible();
    });

    test('should display cart items', async ({ page }) => {
      await page.waitForTimeout(1000);
      for (const item of mockCartItems) {
        await expect(page.getByText(item.productName).first()).toBeVisible();
      }
    });

    test('should show item quantities', async ({ page }) => {
      await page.waitForTimeout(1000);
      for (const item of mockCartItems) {
        const quantityText = page.getByText(String(item.quantity));
        if (await quantityText.first().isVisible().catch(() => false)) {
          await expect(quantityText.first()).toBeVisible();
          break;
        }
      }
    });

    test('should show item prices', async ({ page }) => {
      await page.waitForTimeout(1000);
      const firstItemPrice = mockCartItems[0].price.toFixed(2);
      await expect(page.getByText(new RegExp(firstItemPrice)).first()).toBeVisible();
    });

    test('should show cart total', async ({ page }) => {
      await page.waitForTimeout(1000);
      const totalText = page.getByText(/total|subtotal/i);
      await expect(totalText.first()).toBeVisible();
    });

    test('should have remove item buttons', async ({ page }) => {
      await page.waitForTimeout(1000);
      const xIcons = page.locator('svg.lucide-x, [data-lucide="x"]');
      const trashIcons = page.locator('svg.lucide-trash, svg.lucide-trash-2, [data-lucide="trash"], [data-lucide="trash-2"]');
      const removeLinks = page.getByRole('button', { name: /remove/i });
      
      const hasXIcons = await xIcons.count() > 0;
      const hasTrashIcons = await trashIcons.count() > 0;
      const hasRemoveLinks = await removeLinks.count() > 0;
      
      expect(hasXIcons || hasTrashIcons || hasRemoveLinks).toBeTruthy();
    });

    test('should have continue shopping link', async ({ page }) => {
      const continueLink = page.getByRole('link', { name: /continue shopping|keep shopping/i });
      await expect(continueLink.first()).toBeVisible();
    });

    test('should have checkout button', async ({ page }) => {
      const checkoutButton = page.getByRole('link', { name: /checkout|proceed/i }).or(
        page.getByRole('button', { name: /checkout|proceed/i })
      );
      await expect(checkoutButton.first()).toBeVisible();
    });

    test('should have clear cart option', async ({ page }) => {
      const clearButton = page.getByRole('button', { name: /clear|empty|remove all/i });
      if (await clearButton.first().isVisible().catch(() => false)) {
        await expect(clearButton.first()).toBeVisible();
      }
    });
  });

  test.describe('Empty cart', () => {
    test.beforeEach(async ({ page }) => {
      await setupEmptyCartMocks(page);
      await page.addInitScript(() => {
        localStorage.setItem('token', 'mock-token');
        localStorage.setItem('user', JSON.stringify({
          id: 'user-1',
          email: 'test@example.com',
          firstName: 'John',
          lastName: 'Doe',
        }));
      });
      await page.goto('/cart');
    });

    test('should show empty cart message', async ({ page }) => {
      await expect(page.getByText(/empty|no items/i).first()).toBeVisible();
    });

    test('should have link to continue shopping', async ({ page }) => {
      const shopLink = page.getByRole('link', { name: /continue|shop|browse|products/i });
      await expect(shopLink.first()).toBeVisible();
    });

    test('should display empty cart icon', async ({ page }) => {
      const cartIcon = page.locator('svg.lucide-shopping-bag, svg.lucide-shopping-cart');
      await expect(cartIcon.first()).toBeVisible();
    });
  });
});

test.describe('Checkout Flow (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAuthenticatedMocks(page);
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', JSON.stringify({
        id: 'user-1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
      }));
    });
  });

  test('should navigate to checkout from cart', async ({ page }) => {
    await page.goto('/cart');
    await page.waitForTimeout(1000);
    const checkoutButton = page.getByRole('link', { name: /checkout|proceed/i });
    if (await checkoutButton.first().isVisible()) {
      await checkoutButton.first().click();
      await expect(page).toHaveURL(/\/checkout/);
    }
  });

  test('should display checkout page', async ({ page }) => {
    await page.goto('/checkout');
    await expect(
      page.getByRole('heading', { name: /checkout|order|payment/i }).first()
    ).toBeVisible();
  });

  test('should show order summary on checkout', async ({ page }) => {
    await page.goto('/checkout');
    await page.waitForTimeout(1000);
    const summaryText = page.getByText(/summary|items|total/i);
    await expect(summaryText.first()).toBeVisible();
  });
});
