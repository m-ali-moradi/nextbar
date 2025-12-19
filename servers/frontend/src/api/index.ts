import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import { barApi } from './barApi';
import { authApi } from './authApi';
import { droppointApi } from './droppointApi';
import { warehouseApi } from './warehouseApi';
import { eventApi } from './eventApi';

export const ApiService: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080', // Gateway URL
  headers: {
    'Content-Type': 'application/json',
  },
});

ApiService.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

ApiService.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    if (error.response?.status === 400) {
      const errorMessage =
        (error.response.data as { message?: string })?.message || 'Validation error occurred';
      throw new Error(errorMessage);
    }
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/login';
      throw new Error('Unauthorized: Please log in again');
    }
    if (error.response?.status === 403) {
      throw new Error('Forbidden: You do not have permission to access this resource');
    }
    const errorMessage =
      typeof error.response?.data === 'object' && error.response?.data !== null
        ? (error.response.data as { message?: string }).message
        : undefined;
    throw new Error(errorMessage || 'An error occurred while fetching data');
  }
);

// Composite API call for bar details (integrating stock, usage, total served, supply)
export const getBarDetailsData = async (barId: string) => {
  try {
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
  } catch (error) {
    console.error('Error fetching bar details data:', error);
    throw error;
  }
};

export { authApi, barApi, droppointApi, warehouseApi, eventApi };
export default ApiService;


