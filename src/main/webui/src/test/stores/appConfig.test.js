import { describe, it, expect } from 'vitest';

describe('appConfig store', () => {
  it('should be importable', async () => {
    const { appConfig } = await import('../../lib/stores/appConfig');
    expect(appConfig).toBeDefined();
  });

  it('should be a readable store', async () => {
    const { appConfig } = await import('../../lib/stores/appConfig');
    expect(typeof appConfig.subscribe).toBe('function');
  });

  it('should have subscribe method', async () => {
    const { appConfig } = await import('../../lib/stores/appConfig');
    expect(appConfig.subscribe).toBeInstanceOf(Function);
  });
});
