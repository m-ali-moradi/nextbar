import ApiService from './index';
import type {
  Bar,
  WarehouseStock,
  SupplyRequest,
  ReplenishStockPayload,
  UpdateStockQuantityPayload,
  FulfillSupplyRequestPayload,
  RejectSupplyRequestPayload,
  CollectionRecord,
  EmptyBottleInventory,
  TotalBottlesCollected,
} from './types';

/**
 * Warehouse API module
 * All methods use typed DTOs and JSON body payloads for consistency.
 * Methods follow REST-aligned naming conventions.
 */
const WAREHOUSE_BASE_PATH = '/api/v1/warehouse';

export const warehouseApi = {
  // =====================================================
  // Stock Management
  // =====================================================

  /**
   * Get all warehouse stock/inventory
   */
  getStock: () => ApiService.get<WarehouseStock[]>(`${WAREHOUSE_BASE_PATH}/stock`),

  /**
   * Get specific stock item by beverage type
   */
  getStockByType: (beverageType: string) =>
    ApiService.get<WarehouseStock>(
      `${WAREHOUSE_BASE_PATH}/stock/type/${encodeURIComponent(beverageType)}`
    ),

  /**
   * Get low stock items
   */
  getLowStockItems: () =>
    ApiService.get<WarehouseStock[]>(`${WAREHOUSE_BASE_PATH}/stock/low`),

  /**
   * Create new stock (add new beverage type)
   * @param payload - Stock creation data
   */
  createStock: (payload: ReplenishStockPayload) =>
    ApiService.post<WarehouseStock>(`${WAREHOUSE_BASE_PATH}/stock`, payload),

  /**
   * Update stock quantity for a beverage type
   * Uses PUT with JSON body instead of query params
   * @param beverageType - The beverage type to update
   * @param payload - Quantity update payload
   */
  updateStockQuantity: (beverageType: string, payload: UpdateStockQuantityPayload) =>
    ApiService.put<WarehouseStock>(
      `${WAREHOUSE_BASE_PATH}/stock/type/${encodeURIComponent(beverageType)}`,
      payload
    ),

  /**
   * Add quantity to existing stock (alias for updateStockQuantity with add semantics)
   * @deprecated Use updateStockQuantity instead
   */
  addStock: (beverageType: string, quantity: number) =>
    ApiService.put<WarehouseStock>(
      `${WAREHOUSE_BASE_PATH}/stock/type/${encodeURIComponent(beverageType)}/add`,
      { quantity }
    ),

  /**
   * Delete a stock item
   */
  deleteStock: (id: number) =>
    ApiService.delete(`${WAREHOUSE_BASE_PATH}/stock/${id}`),

  /**
   * Get a specific stock item by ID
   */
  getStockItem: (productId: number) =>
    ApiService.get<WarehouseStock>(`${WAREHOUSE_BASE_PATH}/stock/${productId}`),

  // =====================================================
  // Bars Management (from warehouse perspective)
  // =====================================================

  /**
   * Get all bars
   */
  getBars: () => ApiService.get<Bar[]>(`${WAREHOUSE_BASE_PATH}/bars`),

  // =====================================================
  // Supply Requests - REST-aligned naming
  // =====================================================

  /**
   * Get supply requests for a specific bar
   */
  getBarSupplyRequests: (barId: string | number) =>
    ApiService.get<SupplyRequest[]>(`${WAREHOUSE_BASE_PATH}/bars/${barId}/supply`),

  /**
   * Get a specific supply request
   */
  getSupplyRequest: (barId: string | number, requestId: string | number) =>
    ApiService.get<SupplyRequest>(
      `${WAREHOUSE_BASE_PATH}/bars/${barId}/supply/${requestId}`
    ),

  /**
   * Fulfill a supply request (process and complete)
   * @param barId - Bar ID
   * @param requestId - Supply request ID
   * @param payload - Fulfillment data
   */
  fulfillSupplyRequest: (
    barId: string | number,
    requestId: string | number,
    payload: FulfillSupplyRequestPayload
  ) =>
    ApiService.put<SupplyRequest>(
      `${WAREHOUSE_BASE_PATH}/bars/${barId}/supply/${requestId}/fulfill`,
      payload
    ),

  /**
   * Reject a supply request with a reason
   * @param barId - Bar ID
   * @param requestId - Supply request ID
   * @param payload - Rejection reason
   */
  rejectSupplyRequest: (
    barId: string | number,
    requestId: string | number,
    payload: RejectSupplyRequestPayload
  ) =>
    ApiService.put<SupplyRequest>(
      `${WAREHOUSE_BASE_PATH}/bars/${barId}/supply/${requestId}/reject`,
      payload
    ),

  /**
   * Update supply request status
   * Generic status update method
   * @param barId - Bar ID
   * @param requestId - Supply request ID
   * @param payload - New status
   */
  updateSupplyRequestStatus: (
    barId: string | number,
    requestId: string | number,
    payload: { status: string }
  ) =>
    ApiService.put<SupplyRequest>(
      `${WAREHOUSE_BASE_PATH}/bars/${barId}/supply/${requestId}/status`,
      payload
    ),

  /**
   * Get all supply requests across all bars
   * Note: This endpoint may not be implemented in the new backend
   */
  getAllSupplyRequests: () =>
    ApiService.get<SupplyRequest[]>(`${WAREHOUSE_BASE_PATH}/supply`),

  // =====================================================
  // Empty Bottle Collections
  // =====================================================

  /**
   * Sync drop point notifications
   */
  syncCollections: () =>
    ApiService.post<CollectionRecord[]>(`${WAREHOUSE_BASE_PATH}/collections/sync`, {}),

  /**
   * Get pending collections
   */
  getPendingCollections: () =>
    ApiService.get<CollectionRecord[]>(`${WAREHOUSE_BASE_PATH}/collections/pending`),

  /**
   * Get all collections
   */
  getAllCollections: () =>
    ApiService.get<CollectionRecord[]>(`${WAREHOUSE_BASE_PATH}/collections`),

  /**
   * Get a specific collection
   */
  getCollection: (id: number) =>
    ApiService.get<CollectionRecord>(`${WAREHOUSE_BASE_PATH}/collections/${id}`),

  /**
   * Accept a collection
   */
  acceptCollection: (id: number) =>
    ApiService.put<CollectionRecord>(
      `${WAREHOUSE_BASE_PATH}/collections/${id}/accept`,
      {}
    ),

  /**
   * Mark collection as complete
   */
  completeCollection: (id: number) =>
    ApiService.put<CollectionRecord>(
      `${WAREHOUSE_BASE_PATH}/collections/${id}/complete`,
      {}
    ),

  /**
   * Get empty bottle inventory summary
   */
  getInventorySummary: () =>
    ApiService.get<EmptyBottleInventory[]>(`${WAREHOUSE_BASE_PATH}/collections/inventory`),

  /**
   * Get total bottles collected
   */
  getTotalBottlesCollected: () =>
    ApiService.get<TotalBottlesCollected>(
      `${WAREHOUSE_BASE_PATH}/collections/inventory/total`
    ),
};

// Re-export types for backward compatibility
export type {
  WarehouseStock,
  SupplyRequest,
  Bar,
  ReplenishStockPayload as ReplenishStockDto,
  CollectionRecord,
  EmptyBottleInventory,
} from './types';

// Re-export SupplyRequestItem for backward compatibility
export type { SupplyRequestItem } from './types';

// Alias for UpdateSupplyRequestDto
export interface UpdateSupplyRequestDto {
  beverageType: string;
  quantity: number;
  currentStatus: string;
}
