import ApiService from './index';
import type {
  Bar,
  CreateBarPayload,
  Product,
  StockItem,
  StockOperationPayload,
  BarUsageLog,
  TotalServed,
  SupplyRequest,
  CreateSupplyRequestPayload,
  UpdateSupplyRequestPayload,
} from './types';

/**
 * Bar API module
 * All methods use typed DTOs and JSON body payloads for consistency
 */
export const barApi = {
  // =====================================================
  // Products
  // =====================================================

  /**
   * Get all available products/beverages
   */
  getProducts: () => ApiService.get<Product[]>('/api/bars/products'),

  // =====================================================
  // Bars CRUD
  // =====================================================

  /**
   * Get all bars
   */
  getBars: () => ApiService.get<Bar[]>('/api/bars'),

  /**
   * Get a specific bar by ID
   */
  getBar: (barId: string) => ApiService.get<Bar>(`/api/bars/${barId}`),

  /**
   * Create a new bar
   * @param payload - Bar creation data (name, location, maxCapacity)
   */
  addBar: (payload: CreateBarPayload) =>
    ApiService.post<Bar>('/api/bars', payload),

  /**
   * Delete a bar
   */
  deleteBar: (barId: string) => ApiService.delete(`/api/bars/${barId}`),

  // =====================================================
  // Stock Management
  // =====================================================

  /**
   * Get stock inventory for a specific bar
   */
  getStock: (barId: string) => ApiService.get<StockItem[]>(`/api/bars/${barId}/stock`),

  /**
   * Add stock to a bar
   * @param barId - Bar ID
   * @param payload - Stock operation data (productId, quantity)
   */
  addStock: (barId: string, payload: StockOperationPayload) =>
    ApiService.post<StockItem>(`/api/bars/${barId}/stock/add`, payload),

  /**
   * Reduce stock from a bar
   * @param barId - Bar ID
   * @param payload - Stock operation data (productId, quantity)
   */
  reduceStock: (barId: string, payload: StockOperationPayload) =>
    ApiService.post<StockItem>(`/api/bars/${barId}/stock/reduce`, payload),

  // =====================================================
  // Usage Logging
  // =====================================================

  /**
   * Log a drink sale/usage
   * @param barId - Bar ID
   * @param payload - Usage data (productId, quantity)
   */
  logDrink: (barId: string, payload: StockOperationPayload) =>
    ApiService.post<BarUsageLog>(`/api/bars/${barId}/usage`, payload),

  /**
   * Get usage logs for a bar
   */
  getUsageLogs: (barId: string) =>
    ApiService.get<BarUsageLog[]>(`/api/bars/${barId}/usage`),

  /**
   * Get total served statistics for a bar
   */
  getTotalServed: (barId: string) =>
    ApiService.get<TotalServed[]>(`/api/bars/${barId}/usage/total-served`),

  // =====================================================
  // Supply Requests
  // =====================================================

  /**
   * Create a new supply request for a bar
   * @param barId - Bar ID
   * @param payload - Supply request items
   */
  createSupplyRequest: (barId: string, payload: CreateSupplyRequestPayload) =>
    ApiService.post<SupplyRequest>(`/api/bars/${barId}/supply`, payload),

  /**
   * Get all supply requests for a bar
   */
  getSupplyRequests: (barId: string) =>
    ApiService.get<SupplyRequest[]>(`/api/bars/${barId}/supply`),

  /**
   * Update supply request status
   * @param barId - Bar ID
   * @param requestId - Supply request ID
   * @param payload - Status update payload
   */
  updateSupplyRequest: (
    barId: string,
    requestId: string,
    payload: UpdateSupplyRequestPayload
  ) =>
    ApiService.patch<SupplyRequest>(
      `/api/bars/${barId}/supply/${requestId}/status`,
      payload
    ),

  /**
   * Cancel a pending supply request
   */
  cancelSupplyRequest: (barId: string, requestId: string) =>
    ApiService.delete(`/api/bars/${barId}/supply/${requestId}`),
};
