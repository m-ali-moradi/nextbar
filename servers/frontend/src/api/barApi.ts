import { AxiosResponse } from 'axios';
import { authHeader } from "./authHeader";
import ApiService from './index';

// Bar APIs (integrated from api.js)
export const barApi = {
  getProducts(): Promise<AxiosResponse> {
    return ApiService.get('/api/bars/products');
  },
  getBars(): Promise<AxiosResponse> {
    return ApiService.get('/api/bars');
  },
  addBar(name: string, location: string, maxCapacity: number): Promise<AxiosResponse> {
    return ApiService.post('/api/bars', null, { params: { name, location, maxCapacity } });
  },
  getBar(barId: string): Promise<AxiosResponse> {
    return ApiService.get(`/api/bars/${barId}`);
  },
  getStock(barId: string): Promise<AxiosResponse> {
    return ApiService.get(`/api/bars/${barId}/stock`);
  },
  addStock(barId: string, productId: string, quantity: number): Promise<AxiosResponse> {
    return ApiService.post(`/api/bars/${barId}/stock/add`, null, { params: { productId, quantity } });
  },
  reduceStock(barId: string, productId: string, quantity: number): Promise<AxiosResponse> {
    return ApiService.post(`/api/bars/${barId}/stock/reduce`, null, { params: { productId, quantity } });
  },
  logDrink(barId: string, productId: string, quantity: number): Promise<AxiosResponse> {
    return ApiService.post(`/api/bars/${barId}/usage`, null, { params: { productId, quantity } });
  },
  getUsageLogs(barId: string): Promise<AxiosResponse> {
    return ApiService.get(`/api/bars/${barId}/usage`);
  },
  getTotalServed(barId: string): Promise<AxiosResponse> {
    return ApiService.get(`/api/bars/${barId}/usage/total-served`);
  },
  createSupplyRequest(barId: string, items: Array<{ productId: string, quantity: number }>): Promise<AxiosResponse> {
    return ApiService.post(`/api/bars/${barId}/supply`, items);
  },
  updateSupplyRequest(barId: string, requestId: string, status: string): Promise<AxiosResponse> {
    return ApiService.put(`/api/bars/${barId}/supply/${requestId}/status`, null, { params: { status } });
  },
  getSupplyRequests(barId: string): Promise<AxiosResponse> {
    return ApiService.get(`/api/bars/${barId}/supply`);
  },
  cancelSupplyRequest(barId: string, requestId: string): Promise<AxiosResponse> {
    return ApiService.delete(`/api/bars/${barId}/supply/${requestId}`);
  },
  deleteBar(barId: string): Promise<AxiosResponse> {
    return ApiService.delete(`/api/bars/${barId}`);
  },
};

export function getBar(barId: string): any {
  throw new Error('Function not implemented.');
}
