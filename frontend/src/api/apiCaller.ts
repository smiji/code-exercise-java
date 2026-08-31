import type { ApiErrorResponse } from '../types/ApiErrorResponse';
import type { UrlRequest, UrlResponse, UrlItem } from '../types/url';
import { API_BASE_URL } from './config';

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
  try {
    const body = (await response.json()) as unknown;
    if (isApiErrorResponse(body)) {
      return body;
    }
  } catch {
    // fall through to generic payload below
  }

  const text = await response.text().catch(() => '');
  return {
    status: response.status,
    error: response.statusText || 'Request failed',
    message: text || fallbackMessage,
    timestamp: new Date().toISOString(),
  };
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorBody = await parseApiError(response, 'Request failed');
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

export async function deleteUrl(alias: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/v1/${alias}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    const errorBody = await parseApiError(response, 'Failed to delete URL');
    throw new ApiError(errorBody);
  }
}
