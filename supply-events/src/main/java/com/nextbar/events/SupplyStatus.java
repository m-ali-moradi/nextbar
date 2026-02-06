package com.nextbar.events;

/**
 * Status of a supply request.
 * Shared between bar-service and warehouse-service.
 */
public enum SupplyStatus {
    /** Request has been submitted by bar, waiting for warehouse */
    PENDING,

    /** Warehouse is processing the request */
    IN_PROGRESS,

    /** Request has been fulfilled and delivered */
    DELIVERED,

    /** Request was rejected by warehouse */
    REJECTED,

    /** Bar has confirmed receipt (final state) */
    COMPLETED
}
