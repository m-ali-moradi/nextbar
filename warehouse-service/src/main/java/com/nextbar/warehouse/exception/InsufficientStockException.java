package com.dmsa.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when there is insufficient stock to fulfill a request.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    private final String beverageType;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String beverageType, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for '%s': requested=%d, available=%d",
                beverageType, requestedQuantity, availableQuantity));
        this.beverageType = beverageType;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getBeverageType() {
        return beverageType;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
