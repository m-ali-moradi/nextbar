import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import { barApi } from './barApi';
import { authApi } from './authApi';
import { droppointApi } from './droppointApi';
import { warehouseApi } from './warehouseApi';
import { eventApi } from './eventApi';

import { ApiErrorResponse } from './types';

/**
 * Global API base URL.
 * Falls back to localhost for local development when env var is missing.
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * Endpoints that represent authentication lifecycle operations.
 * A 401 on these routes should not force redirect logic here.
 */
const AUTH_ENDPOINTS = [
  '/api/v1/users/login',
  '/api/v1/users/refresh',
  '/api/v1/users/logout',
];

type RawErrorPayload = {
  message?: string;
  code?: string;
  details?: Record<string, unknown>;
};

/**
 * Returns true when a URL belongs to auth endpoints.
 */
const isAuthRequest = (url?: string): boolean => {
  const normalizedUrl = String(url || '').toLowerCase();
  return AUTH_ENDPOINTS.some((endpoint) => normalizedUrl.includes(endpoint));
};

/**
 * Clears locally persisted auth state and redirects to login page.
 */
const clearSessionAndRedirectToLogin = () => {
  sessionStorage.removeItem('authToken');

  if (window.location.pathname !== '/login') {
    window.location.assign('/login');
  }
};

/**
 * Normalizes backend/transport errors to a single domain error object.
 * This gives UI and composables one consistent error shape.
 */
const toApiErrorResponse = (error: AxiosError) => {
  const status = error.response?.status ?? 0;
  const responseData = error.response?.data as RawErrorPayload | undefined;

  let message: string;
  let code: string | undefined;
  let details: Record<string, unknown> | undefined;

  switch (status) {
    case 400:
      message = responseData?.message || 'Validation error occurred';
      code = 'BAD_REQUEST';
      details = responseData?.details;
      break;
    case 401:
      if (!isAuthRequest(error.config?.url)) {
        clearSessionAndRedirectToLogin();
      }
      message = responseData?.message || 'Unauthorized: Please log in again';
      code = 'UNAUTHORIZED';
      break;
    case 403:
      message = 'Forbidden: You do not have permission to access this resource';
      code = 'FORBIDDEN';
      break;
    case 404:
      message = responseData?.message || 'Resource not found';
      code = 'NOT_FOUND';
      break;
    case 409:
      message = responseData?.message || 'Conflict: Resource already exists';
      code = 'CONFLICT';
      break;
    case 500:
      message = responseData?.message || 'Internal server error';
      code = 'INTERNAL_ERROR';
      break;
    default:
      message = responseData?.message || 'An error occurred while fetching data';
      code = responseData?.code;
      details = responseData?.details;
  }

  return new ApiErrorResponse({
    status,
    message,
    code,
    details,
  });
};

/**
 * Shared Axios instance used by all API modules.
 */
export const ApiService: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor:
 * Inject Bearer token for authenticated requests when available.
 */
ApiService.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Response interceptor:
 * - pass successful responses through unchanged
 * - convert axios errors into ApiErrorResponse
 */
ApiService.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    throw toApiErrorResponse(error);
  },
);

/**
 * Barrel exports for API modules and shared API types.
 */
export { authApi, barApi, droppointApi, warehouseApi, eventApi };
export * from './types';
export default ApiService;
