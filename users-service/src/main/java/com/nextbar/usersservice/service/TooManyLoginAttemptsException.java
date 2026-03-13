package com.nextbar.usersservice.service;

import java.time.Duration;

/**
 * Exception thrown when a user has made too many failed login attempts and is temporarily locked out.
 * The exception includes information about how long the user should wait before trying again.
 */
public class TooManyLoginAttemptsException extends RuntimeException {

    private final Duration retryAfter;

    public TooManyLoginAttemptsException(Duration retryAfter) {
        super("Too many login attempts. Please try again later.");
        Duration safeRetryAfter = retryAfter == null || retryAfter.isNegative() ? Duration.ofSeconds(1) : retryAfter;
        this.retryAfter = safeRetryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
