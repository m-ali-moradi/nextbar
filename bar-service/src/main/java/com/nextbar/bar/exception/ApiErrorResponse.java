package com.nextbar.bar.exception;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Standard API error response structure.
 * Used by GlobalExceptionHandler to provide consistent error responses.
 */
@Getter
@Builder
public class ApiErrorResponse {
    private long timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> errors; // For validation errors
}
