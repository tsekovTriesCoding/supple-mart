import { test, expect } from '@playwright/test';
import { setupAPIMocks } from './mocks/api-mocks';

test.describe('Contact Page (with mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAPIMocks(page);
    await page.goto('/contact');
  });

  test('should display contact page heading', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /touch|contact/i })).toBeVisible();
  });

  test('should display contact information', async ({ page }) => {
    await expect(page.getByText(/email us/i).first()).toBeVisible();
    await expect(page.getByText(/call us/i).first()).toBeVisible();
  });

  test('should display contact form', async ({ page }) => {
    const nameField = page.getByLabel(/full name/i).or(page.getByPlaceholder(/full name/i));
    await expect(nameField).toBeVisible();
  });

  test('should have subject dropdown', async ({ page }) => {
    const subjectSelect = page.getByRole('combobox').first();
    await expect(subjectSelect).toBeVisible();
  });

  test('should have message textarea', async ({ page }) => {
    const messageField = page.locator('textarea').first();
    await expect(messageField).toBeVisible();
  });

  test('should have submit button', async ({ page }) => {
    const submitButton = page.getByRole('button', { name: /send message|send|submit/i });
    await expect(submitButton).toBeVisible();
  });

  test('should fill and submit contact form', async ({ page }) => {
    const nameField = page.getByLabel(/full name/i).or(page.getByPlaceholder(/full name/i));
    await nameField.fill('John Doe');
    
    const emailField = page.getByLabel(/email/i).or(page.getByPlaceholder(/email/i));
    await emailField.fill('john@example.com');
    
    const subjectSelect = page.getByRole('combobox').first();
    await subjectSelect.selectOption({ index: 1 });
    
    const messageField = page.locator('textarea').first();
    await messageField.fill('This is a test message for the contact form.');
    
    const submitButton = page.getByRole('button', { name: /send message|send|submit/i });
    await submitButton.click();
    
    await expect(page.getByText(/success|sent|thank/i).first()).toBeVisible({ timeout: 5000 });
  });
});
