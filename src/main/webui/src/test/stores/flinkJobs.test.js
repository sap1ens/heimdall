import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { get } from 'svelte/store';
import axios from 'axios';

describe('flinkJobs store', () => {
  let flinkJobs;

  beforeEach(async () => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    // Import the store once for all tests
    const module = await import('../../lib/stores/flinkJobs');
    flinkJobs = module.flinkJobs;
  });

  afterEach(() => {
    vi.clearAllMocks();
    vi.useRealTimers();
  });

  it('should have subscribe method', () => {
    expect(typeof flinkJobs.subscribe).toBe('function');
  });

  it('should have setInterval method', () => {
    expect(typeof flinkJobs.setInterval).toBe('function');
  });

  it('should provide store data through subscription', async () => {
    let currentValue;
    const unsubscribe = flinkJobs.subscribe(value => {
      currentValue = value;
    });

    // Should have data structure
    expect(currentValue).toHaveProperty('data');
    expect(currentValue).toHaveProperty('loaded');
    expect(Array.isArray(currentValue.data)).toBe(true);

    unsubscribe();
  });

  it('should setup interval polling when setInterval is called', async () => {
    const callCountBefore = axios.get.mock.calls.length;

    // Set interval to 1 second
    flinkJobs.setInterval(1);

    // Advance 1 second
    await vi.advanceTimersByTimeAsync(1000);

    // Should have made at least one more call
    expect(axios.get.mock.calls.length).toBeGreaterThan(callCountBefore);
  });

  it('should clear interval when setInterval is called with 0', async () => {
    // Set interval first
    flinkJobs.setInterval(1);

    // Clear axios mock
    const callCountAfterSet = axios.get.mock.calls.length;

    // Disable interval
    flinkJobs.setInterval(0);

    // Advance time
    await vi.advanceTimersByTimeAsync(5000);

    // Should not have made new calls
    expect(axios.get.mock.calls.length).toBe(callCountAfterSet);
  });

  it('should clear previous interval when setting a new one', async () => {
    // Set first interval (1 second)
    flinkJobs.setInterval(1);

    const callCountBefore = axios.get.mock.calls.length;

    // Immediately set second interval (2 seconds)
    flinkJobs.setInterval(2);

    // Advance 1 second - should not trigger (old interval cleared)
    await vi.advanceTimersByTimeAsync(1000);
    expect(axios.get.mock.calls.length).toBe(callCountBefore);

    // Advance another second (total 2) - should trigger new interval
    await vi.advanceTimersByTimeAsync(1000);
    expect(axios.get.mock.calls.length).toBe(callCountBefore + 1);
  });

  it('should parse string interval values to integers', async () => {
    const callCountBefore = axios.get.mock.calls.length;

    // Pass string instead of number
    flinkJobs.setInterval('2');

    await vi.advanceTimersByTimeAsync(2000);

    // Should have made a call
    expect(axios.get.mock.calls.length).toBeGreaterThan(callCountBefore);
  });

  it('should handle negative and invalid interval values', () => {
    // These should not throw
    expect(() => flinkJobs.setInterval(-1)).not.toThrow();
    expect(() => flinkJobs.setInterval('invalid')).not.toThrow();
    expect(() => flinkJobs.setInterval(null)).not.toThrow();
  });
});
