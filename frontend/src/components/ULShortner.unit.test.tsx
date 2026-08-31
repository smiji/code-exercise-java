import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import URLShortner from './ULShortner';
import { ApiError, shortenUrl } from '../api/apiCaller';

vi.mock('../api/apiCaller', async () => {
  const actual = await vi.importActual<typeof import('../api/apiCaller')>('../api/apiCaller');
  return {
    ...actual,
    shortenUrl: vi.fn(),
  };
});

describe('URLShortner unit', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows validation error when URL is empty', async () => {
    render(<URLShortner />);

    fireEvent.click(screen.getByRole('button', { name: 'Shorten URL' }));

    const alert = await screen.findByRole('alert');
    expect(alert.textContent).toContain('Original URL is required.');
    expect(shortenUrl).not.toHaveBeenCalled();
  });

  it('shows validation error for invalid URL', async () => {
    render(<URLShortner />);

    fireEvent.change(screen.getByLabelText('Enter URL to shorten:'), {
      target: { value: 'not-a-valid-url' },
    });

    fireEvent.click(screen.getByRole('button', { name: 'Shorten URL' }));

    const alert = await screen.findByRole('alert');
    expect(alert.textContent).toContain('Please enter a valid URL.');
    expect(shortenUrl).not.toHaveBeenCalled();
  });

  it('renders shortened URL on success', async () => {
    vi.mocked(shortenUrl).mockResolvedValue({
      customAlias: 'my-alias',
      fullUrl: 'https://example.com/path',
      shortUrl: 'http://localhost:8081/my-alias',
    });

    render(<URLShortner />);

    fireEvent.change(screen.getByLabelText('Enter URL to shorten:'), {
      target: { value: 'https://example.com/path' },
    });

    fireEvent.change(screen.getByLabelText('Custom alias (optional):'), {
      target: { value: 'my-alias' },
    });

    fireEvent.click(screen.getByRole('button', { name: 'Shorten URL' }));

    await waitFor(() => {
      expect(shortenUrl).toHaveBeenCalledWith({
        fullUrl: 'https://example.com/path',
        customAlias: 'my-alias',
      });
    });

    const link = await screen.findByRole('link', { name: 'http://localhost:8081/my-alias' });
    expect(link.getAttribute('href')).toBe('http://localhost:8081/my-alias');
  });

  it('shows ErrorModal when ApiError is thrown and closes it', async () => {
    vi.mocked(shortenUrl).mockRejectedValue(
      new ApiError({
        status: 409,
        error: 'Conflict',
        message: 'Alias already exists',
        timestamp: '2026-08-31T00:00:00.000Z',
      }),
    );

    render(<URLShortner />);

    fireEvent.change(screen.getByLabelText('Enter URL to shorten:'), {
      target: { value: 'https://example.com/path' },
    });

    fireEvent.click(screen.getByRole('button', { name: 'Shorten URL' }));

    const alert = await screen.findByRole('alert');
    expect(alert.textContent).toContain('Alias already exists');

    fireEvent.click(screen.getByRole('button', { name: 'Close' }));

    await waitFor(() => {
      expect(screen.queryByRole('alert')).toBeNull();
    });
  });
});
