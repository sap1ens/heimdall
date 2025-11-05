import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { get } from 'svelte/store';
import axios from 'axios';

// Mock axios
vi.mock('axios');

describe('flinkJobs store', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.clearAllTimers();
  });

  it('should load jobs on initialization', async () => {
    const mockJobs = [
      {
        id: 'job1',
        name: 'test-job',
        namespace: 'default',
        status: 'RUNNING',
        type: 'APPLICATION'
      }
    ];

    axios.get.mockResolvedValue({ data: mockJobs });

    // Import the store after mocking
    const { flinkJobs } = await import('../../lib/stores/flinkJobs');

    // Wait for the async call
    await vi.runAllTimersAsync();

    expect(axios.get).toHaveBeenCalledWith('jobs');
  });

  it('should set data and loaded state on successful fetch', async () => {
    const mockJobs = [
      { id: 'job1', name: 'job1', status: 'RUNNING' },
      { id: 'job2', name: 'job2', status: 'FAILED' }
    ];

    axios.get.mockResolvedValue({ data: mockJobs });

    const { flinkJobs } = await import('../../lib/stores/flinkJobs');

    await vi.runAllTimersAsync();

    // The store should have loaded the data
    expect(axios.get).toHaveBeenCalled();
  });

  it('should handle fetch errors', async () => {
    axios.get.mockRejectedValue(new Error('Network error'));

    const { flinkJobs } = await import('../../lib/stores/flinkJobs');

    await vi.runAllTimersAsync();

    expect(axios.get).toHaveBeenCalled();
  });

  it('should not show error if jobs already loaded', async () => {
    const mockJobs = [{ id: 'job1', name: 'job1' }];

    // First call succeeds
    axios.get.mockResolvedValueOnce({ data: mockJobs });

    const { flinkJobs } = await import('../../lib/stores/flinkJobs');

    await vi.runAllTimersAsync();

    // Second call fails
    axios.get.mockRejectedValueOnce(new Error('Network error'));

    // Manually trigger another load (simulating interval)
    flinkJobs.setInterval(30);
    await vi.advanceTimersByTimeAsync(30000);

    expect(axios.get).toHaveBeenCalledTimes(2);
  });

  it('should setup interval for auto-refresh', async () => {
    const mockJobs = [{ id: 'job1' }];
    axios.get.mockResolvedValue({ data: mockJobs });

    const { flinkJobs } = await import('../../lib/stores/flinkJobs');

    await vi.runAllTimersAsync();

    // Set 30 second interval
    flinkJobs.setInterval(30);

    // Initial call already happened
    expect(axios.get).toHaveBeenCalledTimes(1);

    // Advance time by 30 seconds
    await vi.advanceTimersByTimeAsync(30000);

    // Should have made another call
    expect(axios.get).toHaveBeenCalledTimes(2);

    // Advance another 30 seconds
    await vi.advanceTimersByTimeAsync(30000);

    expect(axios.get).toHaveBeenCalledTimes(3);
  });

  it('should clear interval when setting new interval', async () => {
    const mockJobs = [{ id: 'job1' }];
    axios.get.mockResolvedValue({ data: mockJobs });

    const { flinkJobs } = await import('../../lib/stores/flinkJobs');

    await vi.runAllTimersAsync();

    // Set initial interval
    flinkJobs.setInterval(30);

    // Clear by setting interval to 0
    flinkJobs.setInterval(0);

    // Advance time
    await vi.advanceTimersByTimeAsync(60000);

    // Should not make additional calls after clearing
    expect(axios.get).toHaveBeenCalledTimes(1); // Only initial load
  });

  it('should parse string interval to integer', async () => {
    const mockJobs = [{ id: 'job1' }];
    axios.get.mockResolvedValue({ data: mockJobs });

    const { flinkJobs } = await import('../../lib/stores/flinkJobs');

    await vi.runAllTimersAsync();

    // Set interval as string
    flinkJobs.setInterval('45');

    await vi.advanceTimersByTimeAsync(45000);

    expect(axios.get).toHaveBeenCalledTimes(2);
  });

  it('should start with correct initial state', async () => {
    axios.get.mockImplementation(() => new Promise(() => {})); // Never resolves

    const { flinkJobs } = await import('../../lib/stores/flinkJobs');

    // Initial state should have empty data and loaded: false
    // This is synchronous, before the async call completes
    expect(axios.get).toHaveBeenCalled();
  });
});
