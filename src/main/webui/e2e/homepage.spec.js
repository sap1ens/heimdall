import { test, expect } from '@playwright/test';

test.describe('Homepage', () => {
  test('should display the app title', async ({ page }) => {
    await page.goto('/');

    // Check for Heimdall logo/title
    await expect(page.locator('text=Heimdall')).toBeVisible();
  });

  test('should load without errors', async ({ page }) => {
    const errors = [];
    page.on('pageerror', error => errors.push(error));

    await page.goto('/');

    expect(errors.length).toBe(0);
  });

  test('should have no console errors', async ({ page }) => {
    const consoleErrors = [];
    page.on('console', msg => {
      if (msg.type() === 'error') {
        consoleErrors.push(msg.text());
      }
    });

    await page.goto('/');

    expect(consoleErrors.length).toBe(0);
  });

  test('should be responsive', async ({ page }) => {
    await page.goto('/');

    // Test mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.locator('body')).toBeVisible();

    // Test tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(page.locator('body')).toBeVisible();

    // Test desktop viewport
    await page.setViewportSize({ width: 1920, height: 1080 });
    await expect(page.locator('body')).toBeVisible();
  });
});
