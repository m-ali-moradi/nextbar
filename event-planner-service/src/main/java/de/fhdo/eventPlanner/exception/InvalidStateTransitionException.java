package de.fhdo.eventPlanner.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid state transition is attempted.
 */
public class InvalidStateTransitionException extends ApiException {

    public InvalidStateTransitionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public InvalidStateTransitionException(String currentState, String targetState) {
        super(String.format("Invalid state transition from '%s' to '%s'", currentState, targetState),
                HttpStatus.BAD_REQUEST);
    }
}
