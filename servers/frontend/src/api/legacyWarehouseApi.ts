/**
 * @deprecated Legacy Warehouse API endpoints
 * This module contains deprecated endpoints that are maintained for backward compatibility.
 * These endpoints should be migrated to the new warehouse API structure.
 * 
 * DO NOT ADD NEW ENDPOINTS TO THIS MODULE.
 */

import ApiService from './index';
import type { SupplyRequest, EmptyBottleInventory } from './types';

/**
 * @deprecated Use warehouseApi methods instead
 * Legacy warehouse API methods for backward compatibility
 */
export const legacyWarehouseApi = {
    /**
     * @deprecated Use warehouseApi.fetchAllSupplyRequestsFromBars() instead
     * Get all supply requests (all bars) - legacy endpoint
     */
    getAllSupplyRequests: () =>
        ApiService.get<SupplyRequest[]>('/api/warehouse/supply'),

    /**
     * @deprecated Use warehouseApi.getInventorySummary() instead
     * Get empties collected from drop points
     */
    getEmptiesCollected: () =>
        ApiService.get<EmptyBottleInventory[]>('/api/warehouse/collections/inventory'),

    /**
     * @deprecated Use warehouseApi.getInventorySummary() or dedicated drop-point API
     * Get drop point dashboard data
     */
    getDropPointDashboard: () =>
        ApiService.get<unknown>('/api/warehouse/droppoints/dashboard'),

    /**
     * @deprecated Use warehouseApi.updateStockQuantity() instead
     * Legacy replenish using query params
     */
    updateSupplyRequestLegacy: (
        barId: string | number,
        requestId: string | number,
        data: { status: string; quantity: number }
    ) =>
        ApiService.put<unknown>(
            `/api/warehouse/bars/replenish/${barId}/${requestId}?currentStatus=${data.status}`,
            { beverageType: 'Unknown', quantity: data.quantity }
        ),
};
