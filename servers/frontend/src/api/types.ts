/**
 * Shared DTOs and types for all API modules
 * This file defines consistent typing for API responses and payloads
 */

// =====================================================
// Error Types
// =====================================================

export interface ApiError {
    status: number;
    message: string;
    code?: string;
    details?: Record<string, unknown>;
}

export class ApiErrorResponse extends Error {
    status: number;
    code?: string;
    details?: Record<string, unknown>;

    constructor(error: ApiError) {
        super(error.message);
        this.name = 'ApiErrorResponse';
        this.status = error.status;
        this.code = error.code;
        this.details = error.details;
    }
}

// =====================================================
// Bar Types
// =====================================================

export interface Bar {
    id: string;
    name: string;
    location: string;
    maxCapacity: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface CreateBarPayload {
    name: string;
    location: string;
    maxCapacity: number;
}

export interface Product {
    id: string;
    name: string;
    unitType: string;
    category?: string;
    price?: number;
}

export interface StockItem {
    id: string;
    barId: string;
    productId: string;
    productName: string;
    quantity: number;
    minStockLevel?: number;
    lowStock?: boolean;
    createdAt?: string;
    updatedAt?: string;
}

export interface StockOperationPayload {
    productId: string;
    quantity: number;
}

export interface BarUsageLog {
    id: string;
    barId: string;
    productId: string;
    productName: string;
    quantity: number;
    timestamp: string;
}

export interface TotalServed {
    productId: string;
    productName: string;
    totalQuantity: number;
}

export interface SupplyRequestItem {
    productId: string;
    productName?: string;
    quantity: number;
}

export interface SupplyRequest {
    id: string;
    barId: string;
    barName?: string;
    items: SupplyRequestItem[];
    status: SupplyRequestStatus;
    rejectionReason?: string;
    requestedAt?: string;
    createdAt?: string;
    updatedAt?: string;
}

export type SupplyRequestStatus = 'REQUESTED' | 'IN_PROGRESS' | 'DELIVERED' | 'REJECTED' | 'COMPLETED';

export interface CreateSupplyRequestPayload {
    items: SupplyRequestItem[];
}

export interface UpdateSupplyRequestPayload {
    status: SupplyRequestStatus;
}

// =====================================================
// Warehouse Types
// =====================================================

export interface WarehouseStock {
    id: number;
    beverageType: string;
    productName?: string;
    quantity: number;
    minStockLevel?: number;
    lowStock?: boolean;
    unitPrice?: number;
    lastUpdated?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface ReplenishStockPayload {
    beverageType: string;
    quantity: number;
    minStockLevel?: number;
}

export interface UpdateStockQuantityPayload {
    quantity: number;
}

export interface FulfillSupplyRequestPayload {
    beverageType: string;
    quantity: number;
    currentStatus: string;
}

export interface RejectSupplyRequestPayload {
    reason: string;
}

export interface EmptyBottleInventory {
    id: number;
    dropPointId: number;
    dropPointLocation: string;
    location?: string;
    totalBottlesCollected: number;
    emptiesCount?: number;
    lastCollectionAt?: string;
    collectionDate?: string;
}

export interface CollectionRecord {
    id: number;
    dropPointId: number;
    location: string;
    bottleCount: number;
    status: CollectionRecordStatus;
    notifiedAt: string;
    acceptedAt?: string;
    collectedAt?: string;
}

export type CollectionRecordStatus = 'PENDING' | 'ACCEPTED' | 'COLLECTED' | 'RESET';

export interface TotalBottlesCollected {
    totalBottlesCollected: number;
}
