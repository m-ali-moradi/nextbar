package com.nextbar.dropPoint.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resource, Object id) {
        super(String.format("%s with id %s not found", resource, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}