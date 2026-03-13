package com.nextbar.dropPoint.exception;

/**
 * Base exception class for drop-points-service API errors.
 */
public abstract class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}