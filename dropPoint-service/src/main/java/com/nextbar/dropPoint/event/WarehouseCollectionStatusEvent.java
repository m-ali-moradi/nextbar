package com.nextbar.dropPoint.event;

import java.time.Instant;

/**
 * Event emitted by warehouse-service to acknowledge or complete a drop-point collection.
 */
public class WarehouseCollectionStatusEvent {

    private Long dropPointId;
    private String status;
    private Instant timestamp;

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
