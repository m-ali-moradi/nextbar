package com.dmsa.warehouse.dto;

import java.util.UUID;

public class ReplenishResponse {
    private UUID barId;
    private UUID requestId;
    private int quantitySent;
    private String message;

    public ReplenishResponse(UUID barId, UUID requestId, int quantitySent, String message) {
        this.barId = barId;
        this.requestId = requestId;
        this.quantitySent = quantitySent;
        this.message = message;
    }

    public UUID getBarId() { return barId; }
    public UUID getRequestId() { return requestId; }
    public int getQuantitySent() { return quantitySent; }
    public String getMessage() { return message; }
}
