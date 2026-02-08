package de.fhdo.eventPlanner.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an external service (UserService, WarehouseService) is
 * unavailable.
 */
public class ServiceUnavailableException extends ApiException {

    public ServiceUnavailableException(String serviceName) {
        super(String.format("%s is currently unavailable. Please try again later.", serviceName),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String serviceName, String details) {
        super(String.format("%s is unavailable: %s", serviceName, details),
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
