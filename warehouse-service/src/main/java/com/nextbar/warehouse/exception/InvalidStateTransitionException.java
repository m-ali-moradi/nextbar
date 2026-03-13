package com.nextbar.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an invalid state transition is attempted.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidStateTransitionException extends RuntimeException {

    private final String currentState;
    private final String targetState;

    public InvalidStateTransitionException(String currentState, String targetState) {
        super(String.format("Invalid state transition from '%s' to '%s'", currentState, targetState));
        this.currentState = currentState;
        this.targetState = targetState;
    }

    public InvalidStateTransitionException(String message) {
        super(message);
        this.currentState = null;
        this.targetState = null;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getTargetState() {
        return targetState;
    }
}
