import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { shortenUrl, deleteUrl, ApiError } from './apiCaller';

describe('apiCaller integration', () => {
  const originalFetch = global.fetch;

  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    global.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  it('returns URL response for successful shortenUrl call', async () => {
    vi.mocked(global.fetch).mockResolvedValue(
      new Response(
        JSON.stringify({
          customAlias: 'abc',
          fullUrl: 'https://example.com',
          shortUrl: 'http://localhost:8081/abc',
        }),
        {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        },
      ),
    );

    const response = await shortenUrl({ fullUrl: 'https://example.com', customAlias: 'abc' });

    expect(response.shortUrl).toBe('http://localhost:8081/abc');
  });

  it('throws ApiError with ApiErrorResponse details for JSON error body', async () => {
    vi.mocked(global.fetch).mockResolvedValue(
      new Response(
        JSON.stringify({
          status: 409,
          error: 'Conflict',
          message: 'Alias already exists',
          timestamp: '2026-08-31T00:00:00.000Z',
        }),
        {
          status: 409,
          statusText: 'Conflict',
          headers: { 'Content-Type': 'application/json' },
        },
      ),
    );

    await expect(shortenUrl({ fullUrl: 'https://example.com', customAlias: 'abc' })).rejects.toMatchObject({
      name: 'ApiError',
      details: {
        status: 409,
        error: 'Conflict',
        message: 'Alias already exists',
      },
    });
  });

  it('throws fallback ApiError when deleteUrl receives non-JSON error response', async () => {
    vi.mocked(global.fetch).mockResolvedValue(
      new Response('Delete failed on server', {
        status: 500,
        statusText: 'Internal Server Error',
        headers: { 'Content-Type': 'text/plain' },
      }),
    );

    try {
      await deleteUrl('abc');
      throw new Error('Expected deleteUrl to throw');
    } catch (error) {
      expect(error).toBeInstanceOf(ApiError);
      const apiError = error as ApiError;
      expect(apiError.details.status).toBe(500);
      expect(apiError.details.error).toBe('Internal Server Error');
      expect(apiError.details.message).toBe('Failed to delete URL');
      expect(apiError.details.timestamp.length).toBeGreaterThan(0);
    }
  });
});
