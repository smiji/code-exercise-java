import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import UrlList from './UrlList';
import type { UrlRecord } from '../types/UrlRecord';

type PageResponse<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
};

function buildPageResponse(content: UrlRecord[], page = 0, totalPages = 1, last = true): PageResponse<UrlRecord> {
  return {
    content,
    totalPages,
    totalElements: content.length,
    number: page,
    size: 10,
    first: page === 0,
    last,
    numberOfElements: content.length,
  };
}

describe('UrlList integration', () => {
  const originalFetch = global.fetch;

  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    global.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  it('re-fetches data when refreshToken prop changes', async () => {
    vi.mocked(global.fetch).mockResolvedValue(
      new Response(
        JSON.stringify(
          buildPageResponse([
            {
              alias: 'one',
              shortUrl: 'http://localhost:8081/one',
              actualUrl: 'https://example.com/one',
            },
          ]),
        ),
        { status: 200, headers: { 'Content-Type': 'application/json' } },
      ),
    );

    const { rerender } = render(<UrlList refreshToken={0} />);
    await screen.findByText('one');

    expect(global.fetch).toHaveBeenCalledTimes(1);

    rerender(<UrlList refreshToken={1} />);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(2);
    });
  });

  it('fetches with filter and next-page parameters', async () => {
    vi.mocked(global.fetch).mockImplementation(async (input) => {
      const requestUrl = String(input);

      if (requestUrl.includes('page=1')) {
        return new Response(
          JSON.stringify(
            buildPageResponse(
              [
                {
                  alias: 'app-2',
                  shortUrl: 'http://localhost:8081/app-2',
                  actualUrl: 'https://example.com/app-2',
                },
              ],
              1,
              2,
              true,
            ),
          ),
          { status: 200, headers: { 'Content-Type': 'application/json' } },
        );
      }

      return new Response(
        JSON.stringify(
          buildPageResponse(
            [
              {
                alias: 'app-1',
                shortUrl: 'http://localhost:8081/app-1',
                actualUrl: 'https://example.com/app-1',
              },
            ],
            0,
            2,
            false,
          ),
        ),
        { status: 200, headers: { 'Content-Type': 'application/json' } },
      );
    });

    render(<UrlList />);

    await screen.findByText('app-1');

    fireEvent.change(screen.getByPlaceholderText('Filter by alias'), {
      target: { value: 'app' },
    });

    await waitFor(() => {
      expect(vi.mocked(global.fetch).mock.calls.some((call) => String(call[0]).includes('alias=app'))).toBe(true);
    });

    fireEvent.click(screen.getByRole('button', { name: 'Next' }));

    await screen.findByText('app-2');

    expect(vi.mocked(global.fetch).mock.calls.some((call) => String(call[0]).includes('page=1'))).toBe(true);
  });

  it('opens delete dialog and sends delete request on confirmation', async () => {
    vi.mocked(global.fetch).mockImplementation(async (input, init) => {
      const requestUrl = String(input);
      const method = init?.method ?? 'GET';

      if (method === 'DELETE' && requestUrl.includes('/api/v1/remove-me')) {
        return new Response(null, { status: 204 });
      }

      return new Response(
        JSON.stringify(
          buildPageResponse([
            {
              alias: 'remove-me',
              shortUrl: 'http://localhost:8081/remove-me',
              actualUrl: 'https://example.com/remove-me',
            },
          ]),
        ),
        { status: 200, headers: { 'Content-Type': 'application/json' } },
      );
    });

    render(<UrlList />);

    await screen.findByText('remove-me');

    fireEvent.click(screen.getByRole('button', { name: 'Delete' }));

    const confirm = await screen.findByRole('button', { name: 'Confirm Delete' });
    fireEvent.click(confirm);

    await waitFor(() => {
      expect(
        vi.mocked(global.fetch).mock.calls.some((call) => {
          const method = (call[1] as RequestInit | undefined)?.method;
          return method === 'DELETE' && String(call[0]).includes('/api/v1/remove-me');
        }),
      ).toBe(true);
    });

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).toBeNull();
    });
  });
});
