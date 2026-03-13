package com.nextbar.warehouse.model.enums;

/**
 * Status of empty bottle collection from drop points.
 * Follows a strict state machine: PENDING → ACCEPTED → COLLECTED
 */
public enum CollectionStatus {

    /**
     * Drop point has notified warehouse about full capacity.
     * Awaiting warehouse acknowledgment.
     */
    PENDING,

    /**
     * Warehouse has accepted the collection request.
     * Collection is scheduled/in progress.
     */
    ACCEPTED,

    /**
     * Empty bottles have been collected and added to inventory.
     * Terminal state - record can be archived.
     */
    COLLECTED,

    /**
     * Drop point status has been reset (e.g., bottles emptied by other means).
     */
    RESET;

    /**
     * Validates if transition from current status to target status is allowed.
     * 
     * @param target the target status
     * @return true if transition is valid
     */
    public boolean canTransitionTo(CollectionStatus target) {
        return switch (this) {
            case PENDING -> target == ACCEPTED || target == RESET;
            case ACCEPTED -> target == COLLECTED || target == RESET;
            case COLLECTED, RESET -> false; // Terminal states
        };
    }
}
