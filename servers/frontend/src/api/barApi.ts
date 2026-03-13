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
 * All methods use typed DTOs and JSON body payloads for consistency.
 */
const BARS_BASE_PATH = '/api/v1/bars';

export const barApi = {
  // =====================================================
  // Products
  // =====================================================

  /**
   * Get all available products/beverages
   */
  getProducts: () => ApiService.get<Product[]>(`${BARS_BASE_PATH}/products`),

  // =====================================================
  // Bars (read-only configuration)
  // =====================================================

  /**
   * Get all bars
   */
  getBars: () => ApiService.get<Bar[]>(BARS_BASE_PATH),

  /**
   * Get a specific bar by ID
   */
  getBar: (barId: string) => ApiService.get<Bar>(`${BARS_BASE_PATH}/${barId}`),

  /**
   * Create a local operational bar
   */
  addBar: (payload: CreateBarPayload) => ApiService.post<Bar>(`${BARS_BASE_PATH}/local`, payload),

  // =====================================================
  // Stock Management
  // =====================================================

  /**
   * Get stock inventory for a specific bar
   */
  getStock: (barId: string) => ApiService.get<StockItem[]>(`${BARS_BASE_PATH}/${barId}/stock`),

  /**
   * Add stock to a bar
   * @param barId - Bar ID
   * @param payload - Stock operation data (productId, quantity)
   */
  addStock: (barId: string, payload: StockOperationPayload) =>
    ApiService.post<StockItem>(`${BARS_BASE_PATH}/${barId}/stock/add`, payload),

  /**
   * Reduce stock from a bar
   * @param barId - Bar ID
   * @param payload - Stock operation data (productId, quantity)
   */
  reduceStock: (barId: string, payload: StockOperationPayload) =>
    ApiService.post<StockItem>(`${BARS_BASE_PATH}/${barId}/stock/reduce`, payload),

  // =====================================================
  // Usage Logging
  // =====================================================

  /**
   * Log a drink sale/usage
   * @param barId - Bar ID
   * @param payload - Usage data (productId, quantity)
   */
  logDrink: (barId: string, payload: StockOperationPayload) =>
    ApiService.post<BarUsageLog>(`${BARS_BASE_PATH}/${barId}/usage`, payload),

  /**
   * Get usage logs for a bar
   */
  getUsageLogs: (barId: string) =>
    ApiService.get<BarUsageLog[]>(`${BARS_BASE_PATH}/${barId}/usage`),

  /**
   * Get total served statistics for a bar
   */
  getTotalServed: (barId: string) =>
    ApiService.get<TotalServed[]>(`${BARS_BASE_PATH}/${barId}/usage/total-served`),

  // =====================================================
  // Supply Requests
  // =====================================================

  /**
   * Create a new supply request for a bar
   * @param barId - Bar ID
   * @param payload - Supply request items
   */
  createSupplyRequest: (barId: string, payload: CreateSupplyRequestPayload) =>
    ApiService.post<SupplyRequest>(`${BARS_BASE_PATH}/${barId}/supply`, payload),

  /**
   * Get all supply requests for a bar
   */
  getSupplyRequests: (barId: string) =>
    ApiService.get<SupplyRequest[]>(`${BARS_BASE_PATH}/${barId}/supply`),

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
      `${BARS_BASE_PATH}/${barId}/supply/${requestId}/status`,
      payload
    ),

  /**
   * Cancel a pending supply request
   */
  cancelSupplyRequest: (barId: string, requestId: string) =>
    ApiService.delete(`${BARS_BASE_PATH}/${barId}/supply/${requestId}`),
};
