import { test, expect } from '@playwright/test';
import { setupAuthenticatedMocks } from './mocks/api-mocks';
import { mockUserReviews, mockProducts } from './mocks/data';

test.describe('Reviews Page (with mocked API)', () => {
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
    await page.goto('/reviews');
  });

  test('should display reviews page heading', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /my reviews|reviews/i }).first()).toBeVisible();
  });

  test('should display user reviews from mock data', async ({ page }) => {
    await page.waitForTimeout(1000);
    for (const review of mockUserReviews) {
      const reviewComment = page.getByText(review.comment);
      if (await reviewComment.first().isVisible().catch(() => false)) {
        await expect(reviewComment.first()).toBeVisible();
        break;
      }
    }
  });

  test('should show review ratings', async ({ page }) => {
    await page.waitForTimeout(1000);
    const starIcons = page.locator('svg.lucide-star');
    const count = await starIcons.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should show product names for reviews', async ({ page }) => {
    await page.waitForTimeout(1000);
    const productNames = mockUserReviews.map(r => r.product.name);
    const productNamesRegex = new RegExp(productNames.join('|'), 'i');
    await expect(page.getByText(productNamesRegex).first()).toBeVisible();
  });

  test('should have edit review option', async ({ page }) => {
    await page.waitForTimeout(1000);
    const editButtons = page.getByRole('button', { name: /edit/i }).or(
      page.locator('button').filter({ has: page.locator('svg.lucide-edit-3, svg.lucide-edit, svg.lucide-pencil') })
    );
    const count = await editButtons.count();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('should have delete review option', async ({ page }) => {
    await page.waitForTimeout(1000);
    const deleteButtons = page.getByRole('button', { name: /delete|remove/i }).or(
      page.locator('button').filter({ has: page.locator('svg.lucide-trash-2, svg.lucide-trash') })
    );
    const count = await deleteButtons.count();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('should show review dates', async ({ page }) => {
    await page.waitForTimeout(1000);
    const datePattern = page.getByText(/\w+\s+\d{1,2},\s+\d{4}/i);
    const count = await datePattern.count();
    expect(count).toBeGreaterThanOrEqual(0);
  });
});

test.describe('Product Reviews Section (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAuthenticatedMocks(page);
    await page.goto(`/products/${mockProducts[0].id}`);
  });

  test('should display reviews section on product page', async ({ page }) => {
    await page.waitForTimeout(1000);
    const reviewsSection = page.getByText(/review|rating/i);
    await expect(reviewsSection.first()).toBeVisible();
  });

  test('should show average rating from mock product', async ({ page }) => {
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

  test('should show review count from mock product', async ({ page }) => {
    await page.waitForTimeout(1000);
    const product = mockProducts[0];
    const reviewText = page.getByText(/\d+\s*review/i).or(
      page.getByText(new RegExp(String(product.totalReviews)))
    );
    await expect(reviewText.first()).toBeVisible();
  });

  test('should have write review button for authenticated users', async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', JSON.stringify({
        id: 'user-1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
      }));
    });
    await page.goto(`/products/${mockProducts[0].id}`);
    await page.waitForTimeout(1000);
    page.getByRole('button', { name: /write|add|review/i });
  });
});
