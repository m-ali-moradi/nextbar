import ApiService from './index';

export interface WarehouseStock {
  id: number;
  productName: string;
  quantity: number;
  unitPrice?: number;
  lastUpdated?: string;
}

export interface SupplyRequest {
  id: number;
  barId: number;
  items: SupplyRequestItem[];
  status: 'REQUESTED' | 'IN_PROGRESS' | 'DELIVERED' | 'REJECTED';
  requestedAt?: string;
  updatedAt?: string;
}

export interface SupplyRequestItem {
  productId: number;
  productName: string;
  quantity: number;
}

export interface Bar {
  id: number;
  name: string;
  location: string;
  maxCapacity: number;
}

export interface UpdateSupplyRequestDto {
  status: 'REQUESTED' | 'IN_PROGRESS' | 'DELIVERED' | 'REJECTED';
  quantity: number;
}

export interface ReplenishStockDto {
  productName: string;
  quantity: number;
}

export const warehouseApi = {
  // Get warehouse stock/inventory
  getStock: () => ApiService.get<WarehouseStock[]>('/api/warehouse/stock'),

  // Get specific stock item
  getStockItem: (productId: number) => 
    ApiService.get<WarehouseStock>(`/api/warehouse/stock/${productId}`),

  // Replenish warehouse stock
  replenishStock: (data: ReplenishStockDto) =>
    ApiService.post<WarehouseStock>('/api/warehouse/stock/replenish', data),

  // Get all bars
  getBars: () => ApiService.get<Bar[]>('/api/warehouse/bars'),

  // Get supply requests for a specific bar
  getBarSupplyRequests: (barId: number) =>
    ApiService.get<SupplyRequest[]>(`/api/warehouse/bars/${barId}/supply`),

  // Get all supply requests (all bars)
  getAllSupplyRequests: () =>
    ApiService.get<SupplyRequest[]>('/api/warehouse/supply/all'),

  // Update supply request status
  updateSupplyRequest: (barId: number, requestId: number, data: UpdateSupplyRequestDto) =>
    ApiService.put<SupplyRequest>(
      `/api/warehouse/bars/${barId}/supply/${requestId}`,
      data
    ),

  // Get drop point dashboard data (empties collected)
  getDropPointDashboard: () =>
    ApiService.get<any>('/api/warehouse/droppoints/dashboard'),

  // Get empties collected from drop points
  getEmptiesCollected: () =>
    ApiService.get<any[]>('/api/warehouse/empties'),
};
