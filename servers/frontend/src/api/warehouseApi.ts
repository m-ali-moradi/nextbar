import ApiService from './index';

export interface WarehouseStock {
  id: number;
  beverageType: string;
  productName?: string; // For backward compatibility with frontend
  quantity: number;
  minStockLevel?: number;
  lowStock?: boolean;
  unitPrice?: number;
  lastUpdated?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface SupplyRequest {
  id: string;
  barId: string;
  barName?: string;
  items: SupplyRequestItem[];
  status: 'REQUESTED' | 'IN_PROGRESS' | 'DELIVERED' | 'REJECTED';
  rejectionReason?: string;
  requestedAt?: string;
  updatedAt?: string;
  createdAt?: string;
}

export interface SupplyRequestItem {
  productId: string;
  productName: string;
  quantity: number;
}

export interface Bar {
  id: string;
  name: string;
  location: string;
  maxCapacity: number;
}

export interface UpdateSupplyRequestDto {
  beverageType: string;
  quantity: number;
  currentStatus: string;
}

export interface ReplenishStockDto {
  beverageType: string;
  quantity: number;
  minStockLevel?: number;
}

export interface EmptyBottleInventory {
  id: number;
  dropPointId: number;
  dropPointLocation: string;
  location?: string; // Alias for backward compatibility
  totalBottlesCollected: number;
  emptiesCount?: number; // Alias for backward compatibility
  lastCollectionAt?: string;
  collectionDate?: string; // Alias for backward compatibility
}

export interface CollectionRecord {
  id: number;
  dropPointId: number;
  location: string;
  bottleCount: number;
  status: 'PENDING' | 'ACCEPTED' | 'COLLECTED' | 'RESET';
  notifiedAt: string;
  acceptedAt?: string;
  collectedAt?: string;
}

export const warehouseApi = {
  // ===== Stock Management =====

  // Get warehouse stock/inventory
  getStock: () => ApiService.get<WarehouseStock[]>('/api/warehouse/stock'),

  // Get specific stock item by type
  getStockByType: (beverageType: string) =>
    ApiService.get<WarehouseStock>(`/api/warehouse/stock/type/${encodeURIComponent(beverageType)}`),

  // Get low stock items
  getLowStockItems: () =>
    ApiService.get<WarehouseStock[]>('/api/warehouse/stock/low'),

  // Add new stock (create new beverage type)
  createStock: (data: ReplenishStockDto) =>
    ApiService.post<WarehouseStock>('/api/warehouse/stock', data),

  // Add quantity to existing stock
  addStock: (beverageType: string, quantity: number) =>
    ApiService.put<WarehouseStock>(
      `/api/warehouse/stock/type/${encodeURIComponent(beverageType)}/add?quantity=${quantity}`,
      {}
    ),

  // Legacy replenishStock for backward compatibility
  replenishStock: (data: ReplenishStockDto) =>
    ApiService.post<WarehouseStock>('/api/warehouse/stock', data),

  // Delete stock
  deleteStock: (id: number) =>
    ApiService.delete(`/api/warehouse/stock/${id}`),

  // Legacy getStockItem
  getStockItem: (productId: number) =>
    ApiService.get<WarehouseStock>(`/api/warehouse/stock/${productId}`),

  // ===== Supply Requests =====

  // Get all bars
  getBars: () => ApiService.get<Bar[]>('/api/warehouse/bars'),

  // Get supply requests for a specific bar
  getBarSupplyRequests: (barId: string | number) =>
    ApiService.get<SupplyRequest[]>(`/api/warehouse/bars/${barId}/supply`),

  // Get a specific supply request
  getSupplyRequest: (barId: string | number, requestId: string | number) =>
    ApiService.get<SupplyRequest>(`/api/warehouse/bars/${barId}/supply/${requestId}`),

  // Process/fulfill a supply request
  processSupplyRequest: (
    barId: string | number,
    requestId: string | number,
    data: UpdateSupplyRequestDto
  ) =>
    ApiService.put<any>(
      `/api/warehouse/bars/${barId}/supply/${requestId}/fulfill`,
      data
    ),

  // Reject a supply request with a reason
  rejectSupplyRequest: (
    barId: string | number,
    requestId: string | number,
    reason: string
  ) =>
    ApiService.put<SupplyRequest>(
      `/api/warehouse/bars/${barId}/supply/${requestId}/reject`,
      { reason }
    ),

  // Legacy updateSupplyRequest for backward compatibility
  updateSupplyRequest: (
    barId: string | number,
    requestId: string | number,
    data: { status: string; quantity: number }
  ) =>
    ApiService.put<any>(
      `/api/warehouse/bars/replenish/${barId}/${requestId}?currentStatus=${data.status}`,
      { beverageType: 'Unknown', quantity: data.quantity }
    ),

  // ===== Empty Bottle Collections =====

  // Sync drop point notifications
  syncCollections: () =>
    ApiService.post<CollectionRecord[]>('/api/warehouse/collections/sync', {}),

  // Get pending collections
  getPendingCollections: () =>
    ApiService.get<CollectionRecord[]>('/api/warehouse/collections/pending'),

  // Get all collections
  getAllCollections: () =>
    ApiService.get<CollectionRecord[]>('/api/warehouse/collections'),

  // Get a specific collection
  getCollection: (id: number) =>
    ApiService.get<CollectionRecord>(`/api/warehouse/collections/${id}`),

  // Accept a collection
  acceptCollection: (id: number) =>
    ApiService.put<CollectionRecord>(`/api/warehouse/collections/${id}/accept`, {}),

  // Mark collection as complete
  completeCollection: (id: number) =>
    ApiService.put<CollectionRecord>(`/api/warehouse/collections/${id}/complete`, {}),

  // Get empty bottle inventory summary
  getInventorySummary: () =>
    ApiService.get<EmptyBottleInventory[]>('/api/warehouse/collections/inventory'),

  // Get total bottles collected
  getTotalBottlesCollected: () =>
    ApiService.get<{ totalBottlesCollected: number }>('/api/warehouse/collections/inventory/total'),

  // ===== Legacy Endpoints for Backward Compatibility =====

  // Get all supply requests (all bars) - not implemented in new backend
  getAllSupplyRequests: () =>
    ApiService.get<SupplyRequest[]>('/api/warehouse/supply'),

  // Get drop point dashboard data
  getDropPointDashboard: () =>
    ApiService.get<any>('/api/warehouse/droppoints/dashboard'),

  // Get empties collected from drop points (legacy)
  getEmptiesCollected: () =>
    ApiService.get<EmptyBottleInventory[]>('/api/warehouse/collections/inventory'),
};
