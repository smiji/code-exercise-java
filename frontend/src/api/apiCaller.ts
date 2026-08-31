import type { ApiErrorResponse } from '../types/ApiErrorResponse';
import type { UrlRequest, UrlResponse, UrlItem } from '../types/url';
import type { UrlRecord } from '../types/UrlRecord';
import { API_BASE_URL } from './config';

export type PageResponse<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
};

export class ApiError extends Error {
  details: ApiErrorResponse;

  constructor(details: ApiErrorResponse) {
    super(details.message || details.error || 'Request failed');
    this.name = 'ApiError';
    this.details = details;
  }
}

function isApiErrorResponse(value: unknown): value is ApiErrorResponse {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const obj = value as Record<string, unknown>;
  return (
    typeof obj.status === 'number' &&
    typeof obj.error === 'string' &&
    typeof obj.message === 'string' &&
    typeof obj.timestamp === 'string'
  );
}

async function parseApiError(response: Response, fallbackMessage: string): Promise<ApiErrorResponse> {
  const text = await response.text().catch(() => '');

  if (text) {
    try {
      const body = JSON.parse(text) as unknown;
      if (isApiErrorResponse(body)) {
        return body;
      }
    } catch {
      // Fall through to plain text error body handling.
    }
  }

  return {
    status: response.status,
    error: response.statusText || 'Request failed',
    message: text || fallbackMessage,
    timestamp: new Date().toISOString(),
  };
}

async function handleResponse<T>(response: Response, fallbackMessage = 'Request failed'): Promise<T> {
  if (!response.ok) {
    const errorBody = await parseApiError(response, fallbackMessage);
    throw new ApiError(errorBody);
  }

  return response.json() as Promise<T>;
}

export async function shortenUrl(payload: UrlRequest): Promise<UrlResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/shorten`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });  
  return handleResponse<UrlResponse>(response);
}

export async function getAllUrls(): Promise<UrlItem[]> {
  const response = await fetch(`${API_BASE_URL}/api/v1/urls`);

  return handleResponse<UrlItem[]>(response);
}

export async function getUrlsPage(query: {
  page: number;
  size: number;
  alias?: string;
}): Promise<PageResponse<UrlRecord>> {
  const params = new URLSearchParams({
    page: String(query.page),
    size: String(query.size),
  });

  if (query.alias) {
    params.set('alias', query.alias);
  }

  const response = await fetch(`${API_BASE_URL}/api/v1/urls?${params.toString()}`);
  return handleResponse<PageResponse<UrlRecord>>(response, 'Failed to fetch URLs');
}

export async function deleteUrl(alias: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/v1/${encodeURIComponent(alias)}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    const errorBody = await parseApiError(response, 'Failed to delete URL');
    throw new ApiError(errorBody);
  }
}
