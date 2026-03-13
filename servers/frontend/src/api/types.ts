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
    eventName?: string;
    eventStatus?: 'SCHEDULED' | 'RUNNING' | 'COMPLETED' | 'CANCELLED' | string;
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
    productName: string;
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
    name: string;
    total: number;
}

export interface SupplyRequestItem {
    productName: string;
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

// =====================================================
// Drop Point Types
// =====================================================

export interface DropPoint {
    id?: number;
    location: string;
    capacity: number;
    current_empties: number;
    status: 'EMPTY' | 'FULL' | 'FULL_AND_NOTIFIED_TO_WAREHOUSE';
    eventStatus?: 'SCHEDULED' | 'RUNNING' | 'COMPLETED' | 'CANCELLED' | string;
}

// =====================================================
// Event Planner Types
// =====================================================

export type EventStatus = 'SCHEDULED' | 'RUNNING' | 'COMPLETED' | 'CANCELLED';
export type ResourceMode = 'NEW' | 'EXISTING';

export interface EventBar {
    id: number;
    name: string;
    location?: string;
    capacity?: number;
    assignedStaff?: string;
    active: boolean;
    eventId: number;
    eventName?: string;
    stockCount?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface BarStock {
    id: number;
    barId: number;
    barName?: string;
    itemName: string;
    quantity: number;
    productId?: number;
    productName?: string;
    allocatedQuantity?: number;
    usedQuantity?: number;
    remainingQuantity?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface EventDropPoint {
    id: number;
    name: string;
    location?: string;
    capacity?: number;
    eventId: number;
    eventName?: string;
    eventOccupancy?: number;
    assignedStaff?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface EventSummary {
    id: number;
    name: string;
    date: string;
    location?: string;
    status: EventStatus;
    organizerName?: string;
    barCount: number;
    dropPointCount: number;
    isPublic: boolean;
    createdAt?: string;
    updatedAt?: string;
}

export interface EventDetail extends EventSummary {
    description?: string;
    organizerEmail?: string;
    organizerPhone?: string;
    attendeesCount?: number;
    maxAttendees?: number;
    expectedAttendees?: number;
    maxCapacity?: number;
    bars: EventBar[];
    dropPoints: EventDropPoint[];
}

export interface CreateEventRequest {
    name: string;
    date: string;
    location?: string;
    description?: string;
    organizerName?: string;
    organizerEmail?: string;
    organizerPhone?: string;
    attendeesCount?: number;
    maxAttendees?: number;
    isPublic?: boolean;
}

export interface UpdateEventRequest extends Partial<CreateEventRequest> {
    status?: EventStatus;
}

export interface CreateBarRequest {
    name?: string;
    location?: string;
    eventId: number;
    capacity?: number;
    assignedStaff?: string[];
    resourceMode?: ResourceMode;
    existingResourceId?: string;
}

export interface UpdateBarRequest {
    name?: string;
    location?: string;
    capacity?: number;
    assignedStaff?: string[];
    active?: boolean;
}

export interface CreateDropPointRequest {
    name?: string;
    location?: string;
    eventId: number;
    assignedStaff?: string;
    capacity?: number;
    resourceMode?: ResourceMode;
    existingResourceId?: string;
}

export interface UpdateDropPointRequest {
    name?: string;
    location?: string;
    assignedStaff?: string;
    capacity?: number;
}

export interface CreateBarStockRequest {
    barId?: number;
    itemName: string;
    quantity: number;
}

export interface UserDto {
    id: string;
    username: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    fullName?: string;
    // Newer backend returns structured assignments; keep legacy roleAssignments for compatibility
    assignments?: Array<{
        assignmentId?: string;
        service?: string;
        role?: string;
        resourceId?: string;
    }>;
    roleAssignments?: string[];
}

export interface WarehouseStockDto {
    id: number;
    productId: number;
    productName: string;
    quantity: number;
    availableQuantity: number;
    beverageType?: string;
    minStockLevel?: number;
    lowStock?: boolean;
    unit?: string;
}
