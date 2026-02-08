import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import { barApi } from './barApi';
import { authApi } from './authApi';
import { droppointApi } from './droppointApi';
import { warehouseApi } from './warehouseApi';
import { eventApi } from './eventApi';
import { legacyWarehouseApi } from './legacyWarehouseApi';
import { ApiErrorResponse } from './types';

// Use environment variable with fallback for development
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const ApiService: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Add auth token
ApiService.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: Structured error handling
ApiService.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    // Extract error details from response
    const status = error.response?.status ?? 0;
    const responseData = error.response?.data as { message?: string; code?: string; details?: Record<string, unknown> } | undefined;

    let message: string;
    let code: string | undefined;
    let details: Record<string, unknown> | undefined;

    // Handle specific status codes
    switch (status) {
      case 400:
        message = responseData?.message || 'Validation error occurred';
        code = 'BAD_REQUEST';
        details = responseData?.details;
        break;
      case 401:
        localStorage.removeItem('authToken');
        window.location.href = '/login';
        message = 'Unauthorized: Please log in again';
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

    // Throw structured error response
    throw new ApiErrorResponse({
      status,
      message,
      code,
      details,
    });
  }
);

// Composite API call for bar details (integrating stock, usage, total served, supply)
export const getBarDetailsData = async (barId: string) => {
  const [bar, stock, usageLogs, totalServed, supplyRequests] = await Promise.all([
    barApi.getBar(barId),
    barApi.getStock(barId),
    barApi.getUsageLogs(barId),
    barApi.getTotalServed(barId),
    barApi.getSupplyRequests(barId),
  ]);
  return {
    bar: bar.data,
    stock: stock.data,
    usageLogs: usageLogs.data,
    totalServed: totalServed.data,
    supplyRequests: supplyRequests.data,
  };
};

// Export all API modules
export { authApi, barApi, droppointApi, warehouseApi, eventApi, legacyWarehouseApi };
export * from './types';
export default ApiService;
