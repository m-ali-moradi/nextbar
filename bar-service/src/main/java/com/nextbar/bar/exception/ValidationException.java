package com.nextbar.bar.exception;

// Custom exception for validation errors
public class ValidationException extends RuntimeException {

    // Constructor with a message
    public ValidationException(String message) {
        super(message);
    }

    // Constructor with a message and cause
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}