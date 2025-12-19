package de.fhdo.eventPlanner.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
