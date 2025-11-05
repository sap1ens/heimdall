import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import axios from 'axios';

describe('appConfig store', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize with null value', async () => {
    // Don't resolve the promise yet
    axios.get.mockImplementation(() => new Promise(() => {}));

    const { appConfig } = await import('../../lib/stores/appConfig?t=' + Date.now());

    let currentValue;
    const unsubscribe = appConfig.subscribe(value => {
      currentValue = value;
    });

    // Initial value should be null
    expect(currentValue).toBeNull();

    unsubscribe();
  });

  it('should load configuration successfully from API', async () => {
    const mockConfig = {
      apiUrl: 'http://api.example.com',
      features: {
        darkMode: true,
        notifications: true
      },
      version: '1.0.0'
    };

    axios.get.mockResolvedValueOnce({ data: mockConfig });

    const { appConfig } = await import('../../lib/stores/appConfig?t=' + Date.now());

    let currentValue;
    const unsubscribe = appConfig.subscribe(value => {
      currentValue = value;
    });

    // Wait for promise to resolve
    await vi.waitFor(() => {
      expect(currentValue).toEqual(mockConfig);
    });

    expect(axios.get).toHaveBeenCalledWith('config');

    unsubscribe();
  });

  it('should handle API errors gracefully with fallback config', async () => {
    const mockError = new Error('Failed to fetch config');
    axios.get.mockRejectedValueOnce(mockError);

    // Spy on console.error to verify error logging
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    const { appConfig } = await import('../../lib/stores/appConfig?t=' + Date.now());

    let currentValue;
    const unsubscribe = appConfig.subscribe(value => {
      currentValue = value;
    });

    // Wait for promise to reject and fallback to be set
    await vi.waitFor(() => {
      expect(currentValue).toEqual({
        error: 'Failed to load configuration. Some features may not work correctly.',
        loaded: false
      });
    });

    // Verify error was logged
    expect(consoleErrorSpy).toHaveBeenCalledWith(
      'Failed to load application config:',
      mockError.message
    );

    consoleErrorSpy.mockRestore();
    unsubscribe();
  });

  it('should log error message when error object has message property', async () => {
    const mockError = new Error('Network timeout');
    axios.get.mockRejectedValueOnce(mockError);

    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    const { appConfig } = await import('../../lib/stores/appConfig?t=' + Date.now());

    const unsubscribe = appConfig.subscribe(() => {});

    await vi.waitFor(() => {
      expect(consoleErrorSpy).toHaveBeenCalled();
    });

    expect(consoleErrorSpy).toHaveBeenCalledWith(
      'Failed to load application config:',
      'Network timeout'
    );

    consoleErrorSpy.mockRestore();
    unsubscribe();
  });

  it('should log entire error object when no message property exists', async () => {
    const mockError = { status: 500, statusText: 'Internal Server Error' };
    axios.get.mockRejectedValueOnce(mockError);

    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    const { appConfig } = await import('../../lib/stores/appConfig?t=' + Date.now());

    const unsubscribe = appConfig.subscribe(() => {});

    await vi.waitFor(() => {
      expect(consoleErrorSpy).toHaveBeenCalled();
    });

    expect(consoleErrorSpy).toHaveBeenCalledWith(
      'Failed to load application config:',
      mockError
    );

    consoleErrorSpy.mockRestore();
    unsubscribe();
  });

  it('should be a readable store (no set method exposed)', async () => {
    axios.get.mockResolvedValueOnce({ data: {} });

    const { appConfig } = await import('../../lib/stores/appConfig?t=' + Date.now());

    // Readable stores only have subscribe
    expect(typeof appConfig.subscribe).toBe('function');
    expect(appConfig.set).toBeUndefined();
    expect(appConfig.update).toBeUndefined();
  });

  it('should handle empty response data', async () => {
    const emptyData = {};
    axios.get.mockResolvedValueOnce({ data: emptyData });

    const { appConfig } = await import('../../lib/stores/appConfig?t=' + Date.now());

    let currentValue;
    const unsubscribe = appConfig.subscribe(value => {
      currentValue = value;
    });

    await vi.waitFor(() => {
      expect(currentValue).toEqual(emptyData);
    });

    unsubscribe();
  });

  it('should call cleanup function on unsubscribe', async () => {
    axios.get.mockResolvedValueOnce({ data: {} });

    const { appConfig } = await import('../../lib/stores/appConfig?t=' + Date.now());

    const unsubscribe = appConfig.subscribe(() => {});

    // Should not throw when calling cleanup
    expect(() => unsubscribe()).not.toThrow();
  });
});
