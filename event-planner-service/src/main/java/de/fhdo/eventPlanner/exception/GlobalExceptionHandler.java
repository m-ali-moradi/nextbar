package de.fhdo.eventPlanner.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * Handle custom API exceptions.
         */
        @ExceptionHandler(ApiException.class)
        public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
                log.warn("API Exception: {} - {}", ex.getStatus(), ex.getMessage());

                ApiErrorResponse response = ApiErrorResponse.of(
                                ex.getStatus().value(),
                                ex.getStatus().getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(ex.getStatus()).body(response);
        }

        /**
         * Handle validation errors.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                BindingResult bindingResult = ex.getBindingResult();

                List<ApiErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                                .map(error -> ApiErrorResponse.FieldError.builder()
                                                .field(error.getField())
                                                .message(error.getDefaultMessage())
                                                .rejectedValue(error.getRejectedValue())
                                                .build())
                                .collect(Collectors.toList());

                ApiErrorResponse response = ApiErrorResponse.builder()
                                .timestamp(java.time.LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Validation Failed")
                                .message("One or more fields have validation errors")
                                .path(request.getRequestURI())
                                .fieldErrors(fieldErrors)
                                .build();

                log.warn("Validation failed for request to {}: {}",
                                request.getRequestURI(), fieldErrors);

                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Handle illegal argument exceptions.
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex, HttpServletRequest request) {

                log.warn("Illegal argument: {}", ex.getMessage());

                ApiErrorResponse response = ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Handle all other exceptions.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {

                log.error("Unexpected error processing request to {}: {}",
                                request.getRequestURI(), ex.getMessage(), ex);

                ApiErrorResponse response = ApiErrorResponse.of(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred. Please try again later.",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
}
