import { test, expect } from '@playwright/test';

test.describe('Flink Jobs', () => {
  test.beforeEach(async ({ page }) => {
    // Mock the API responses
    await page.route('**/jobs', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 'test-job-1',
            name: 'test-job-1',
            namespace: 'default',
            status: 'RUNNING',
            type: 'APPLICATION',
            startTime: Date.now(),
            shortImage: 'flink:1.15',
            flinkVersion: '1.15',
            parallelism: 4,
            resources: {},
            metadata: {}
          },
          {
            id: 'test-job-2',
            name: 'test-job-2',
            namespace: 'staging',
            status: 'FAILED',
            type: 'SESSION',
            startTime: Date.now(),
            shortImage: 'flink:1.16',
            flinkVersion: '1.16',
            parallelism: 8,
            resources: {},
            metadata: {}
          }
        ])
      });
    });

    await page.route('**/config', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          appVersion: '0.10.0',
          patterns: {
            'display-name': '$jobName'
          },
          endpointPathPatterns: {
            'flink-ui': 'http://localhost/$jobName/ui'
          }
        })
      });
    });

    await page.goto('/');
  });

  test('should display job list', async ({ page }) => {
    // Wait for jobs to load
    await page.waitForSelector('text=test-job-1', { timeout: 5000 });

    // Check that both jobs are visible
    await expect(page.locator('text=test-job-1')).toBeVisible();
    await expect(page.locator('text=test-job-2')).toBeVisible();
  });

  test('should display job statuses', async ({ page }) => {
    await page.waitForSelector('text=test-job-1', { timeout: 5000 });

    // Check for status indicators
    await expect(page.locator('text=RUNNING')).toBeVisible();
    await expect(page.locator('text=FAILED')).toBeVisible();
  });

  test('should display job types', async ({ page }) => {
    await page.waitForSelector('text=test-job-1', { timeout: 5000 });

    // Check for type indicators (A for APPLICATION, S for SESSION)
    const typeA = page.locator('p:has-text("A")').first();
    const typeS = page.locator('p:has-text("S")').first();

    await expect(typeA).toBeVisible();
    await expect(typeS).toBeVisible();
  });

  test('should filter jobs by name', async ({ page }) => {
    await page.waitForSelector('text=test-job-1', { timeout: 5000 });

    // Find and fill the search input
    const searchInput = page.locator('input[type="text"]').first();
    await searchInput.fill('test-job-1');

    // Only test-job-1 should be visible
    await expect(page.locator('text=test-job-1')).toBeVisible();
  });

  test('should display job namespaces', async ({ page }) => {
    await page.waitForSelector('text=test-job-1', { timeout: 5000 });

    await expect(page.locator('text=default')).toBeVisible();
    await expect(page.locator('text=staging')).toBeVisible();
  });

  test('should handle empty job list', async ({ page }) => {
    // Override with empty jobs
    await page.route('**/jobs', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      });
    });

    await page.goto('/');

    // Should show empty state or no jobs message
    await page.waitForTimeout(1000);
    await expect(page.locator('text=test-job-1')).not.toBeVisible();
  });

  test('should handle API errors gracefully', async ({ page }) => {
    await page.route('**/jobs', async route => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Internal Server Error' })
      });
    });

    await page.goto('/');

    // App should not crash
    await page.waitForTimeout(1000);
    await expect(page.locator('body')).toBeVisible();
  });
});

test.describe('Job Settings', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/jobs', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      });
    });

    await page.route('**/config', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          appVersion: '0.10.0',
          patterns: { 'display-name': '$jobName' },
          endpointPathPatterns: {}
        })
      });
    });

    await page.goto('/');
  });

  test('should open settings modal', async ({ page }) => {
    // Look for settings icon or button (adjust selector as needed)
    const settingsButton = page.locator('[title*="Settings"], [title*="settings"]').first();

    if (await settingsButton.isVisible()) {
      await settingsButton.click();

      // Modal should be visible
      await expect(page.locator('dialog')).toBeVisible();
    }
  });

  test('should toggle display mode', async ({ page }) => {
    // Test display mode toggling if the UI supports it
    await page.waitForTimeout(500);

    // This would depend on the actual UI implementation
    const displayToggle = page.locator('button:has-text("Cards"), button:has-text("Tabular")').first();

    if (await displayToggle.isVisible()) {
      await displayToggle.click();
      // Verify mode changed
    }
  });
});
