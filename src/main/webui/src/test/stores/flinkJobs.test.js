import { describe, it, expect } from 'vitest';

describe('flinkJobs store', () => {
  it('should be importable', async () => {
    const { flinkJobs } = await import('../../lib/stores/flinkJobs');
    expect(flinkJobs).toBeDefined();
  });

  it('should have subscribe method', async () => {
    const { flinkJobs } = await import('../../lib/stores/flinkJobs');
    expect(typeof flinkJobs.subscribe).toBe('function');
  });

  it('should have setInterval method', async () => {
    const { flinkJobs } = await import('../../lib/stores/flinkJobs');
    expect(typeof flinkJobs.setInterval).toBe('function');
  });
});
