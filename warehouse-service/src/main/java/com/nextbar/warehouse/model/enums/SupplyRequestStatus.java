package com.nextbar.warehouse.model.enums;

/**
 * Status of supply requests from bars.
 * Follows a strict state machine for warehouse fulfillment workflow.
 */
public enum SupplyRequestStatus {

    /**
     * Request submitted by bar, awaiting warehouse review.
     */
    REQUESTED,

    /**
     * Warehouse has started processing the request.
     * Stock has been reserved/deducted.
     */
    IN_PROGRESS,

    /**
     * Supplies have been delivered to the bar.
     */
    DELIVERED,

    /**
     * Request rejected due to insufficient stock or other reasons.
     */
    REJECTED;

    /**
     * Validates if transition from current status to target status is allowed.
     * 
     * @param target the target status
     * @return true if transition is valid
     */
    public boolean canTransitionTo(SupplyRequestStatus target) {
        return switch (this) {
            case REQUESTED -> target == IN_PROGRESS || target == REJECTED;
            case IN_PROGRESS -> target == DELIVERED;
            case DELIVERED, REJECTED -> false; // Terminal states
        };
    }

    /**
     * Parses status from string, case-insensitive.
     * 
     * @param value the string value
     * @return the parsed status
     * @throws IllegalArgumentException if value is invalid
     */
    public static SupplyRequestStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status value cannot be null or blank");
        }
        return valueOf(value.trim().toUpperCase());
    }
}
