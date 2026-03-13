package com.nextbar.warehouse.event;

import java.time.Instant;

/**
 * Lifecycle update emitted by warehouse-service to drop-points-service.
 */
public class WarehouseCollectionLifecycleEvent {

    private Long dropPointId;
    private String status;
    private Instant timestamp;

    public static WarehouseCollectionLifecycleEvent of(Long dropPointId, String status) {
        WarehouseCollectionLifecycleEvent event = new WarehouseCollectionLifecycleEvent();
        event.setDropPointId(dropPointId);
        event.setStatus(status);
        event.setTimestamp(Instant.now());
        return event;
    }

    public Long getDropPointId() {
        return dropPointId;
    }

    public void setDropPointId(Long dropPointId) {
        this.dropPointId = dropPointId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
