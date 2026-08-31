import React from 'react';
import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
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

function buildPageResponse(content: UrlRecord[]): PageResponse<UrlRecord> {
  return {
    content,
    totalPages: 1,
    totalElements: content.length,
    number: 0,
    size: 10,
    first: true,
    last: true,
    numberOfElements: content.length,
  };
}

describe('UrlList unit', () => {
  const originalFetch = global.fetch;

  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    global.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  it('renders a derived alias when alias field is missing', async () => {
    vi.mocked(global.fetch).mockResolvedValue(
      new Response(
        JSON.stringify(
          buildPageResponse([
            {
              shortUrl: 'http://localhost:8081/my-alias',
              actualUrl: 'https://example.com/articles/1',
            },
          ]),
        ),
        { status: 200, headers: { 'Content-Type': 'application/json' } },
      ),
    );

    render(<UrlList />);

    const aliasCell = await screen.findByText('my-alias');
    expect(aliasCell.textContent).toBe('my-alias');
    expect(screen.getByText('https://example.com/articles/1')).toBeTruthy();
    expect(screen.getByText('http://localhost:8081/my-alias')).toBeTruthy();
  });

  it('shows empty-state message when no records are returned', async () => {
    vi.mocked(global.fetch).mockResolvedValue(
      new Response(JSON.stringify(buildPageResponse([])), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );

    render(<UrlList />);

    const empty = await screen.findByText('No records found');
    expect(empty.textContent).toContain('No records found');
  });

  it('shows an error message when fetch fails', async () => {
    vi.mocked(global.fetch).mockResolvedValue(
      new Response('server failed', {
        status: 500,
        statusText: 'Internal Server Error',
      }),
    );

    render(<UrlList />);

    const errorText = await screen.findByText('Failed to fetch URLs');
    expect(errorText.textContent).toContain('Failed to fetch URLs');
  });
});
