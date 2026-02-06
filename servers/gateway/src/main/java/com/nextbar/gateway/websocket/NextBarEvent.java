package com.nextbar.gateway.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * ============================================================
 * NextBar Real-time Event Model
 * ============================================================
 * 
 * This class represents a real-time event that is broadcast
 * to connected WebSocket clients. Events are published by
 * backend services (bar, warehouse, droppoints) and consumed
 * by the gateway, which then pushes them to the frontend.
 * 
 * Event Types:
 * - BAR_STOCK_UPDATED: Bar inventory changed
 * - SUPPLY_REQUEST_CREATED: New supply request from bar
 * - SUPPLY_REQUEST_UPDATED: Warehouse processed a request
 * - DROPPOINT_STATUS_CHANGED: Drop point fill level changed
 * - EVENT_UPDATED: Event planner data changed
 */
public class NextBarEvent {

    /**
     * Event type identifier - determines how frontend handles the event
     */
    @JsonProperty("type")
    private String type;

    /**
     * The service that originated this event
     * (bar-service, warehouse-service, droppoint-service, etc.)
     */
    @JsonProperty("source")
    private String source;

    /**
     * Resource ID that was affected (barId, droppointId, etc.)
     * Null if event affects the entire resource collection
     */
    @JsonProperty("resourceId")
    private String resourceId;

    /**
     * The actual event payload data
     * Structure depends on event type
     */
    @JsonProperty("payload")
    private Object payload;

    /**
     * ISO-8601 timestamp when the event was created
     */
    @JsonProperty("timestamp")
    private String timestamp;

    // ==================== Constructors ====================

    public NextBarEvent() {
        this.timestamp = Instant.now().toString();
    }

    public NextBarEvent(String type, String source, String resourceId, Object payload) {
        this.type = type;
        this.source = source;
        this.resourceId = resourceId;
        this.payload = payload;
        this.timestamp = Instant.now().toString();
    }

    // ==================== Factory Methods ====================

    /**
     * Create a BAR_STOCK_UPDATED event
     */
    public static NextBarEvent barStockUpdated(String barId, Object stockData) {
        return new NextBarEvent("BAR_STOCK_UPDATED", "bar-service", barId, stockData);
    }

    /**
     * Create a SUPPLY_REQUEST_CREATED event
     */
    public static NextBarEvent supplyRequestCreated(String barId, Object requestData) {
        return new NextBarEvent("SUPPLY_REQUEST_CREATED", "bar-service", barId, requestData);
    }

    /**
     * Create a SUPPLY_REQUEST_UPDATED event
     */
    public static NextBarEvent supplyRequestUpdated(String barId, Object requestData) {
        return new NextBarEvent("SUPPLY_REQUEST_UPDATED", "warehouse-service", barId, requestData);
    }

    /**
     * Create a WAREHOUSE_STOCK_UPDATED event
     */
    public static NextBarEvent warehouseStockUpdated(Object stockData) {
        return new NextBarEvent("WAREHOUSE_STOCK_UPDATED", "warehouse-service", null, stockData);
    }

    /**
     * Create a DROPPOINT_STATUS_CHANGED event
     */
    public static NextBarEvent droppointStatusChanged(String droppointId, Object droppointData) {
        return new NextBarEvent("DROPPOINT_STATUS_CHANGED", "droppoint-service", droppointId, droppointData);
    }

    // ==================== Getters & Setters ====================

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "NextBarEvent{" +
                "type='" + type + '\'' +
                ", source='" + source + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
