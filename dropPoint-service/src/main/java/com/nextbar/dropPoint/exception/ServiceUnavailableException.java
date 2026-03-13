package com.nextbar.dropPoint.exception;

/**
 * Exception thrown when a service is unavailable.
 */
public class ServiceUnavailableException extends ApiException {

    public ServiceUnavailableException(String service) {
        super(String.format("Service '%s' is currently unavailable", service));
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}