package com.nextbar.usersservice.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Unit tests for {@link LoginAttemptService}.
 *
 * <p>
 * These tests validate the lockout policy and reset behavior. This is important
 * because login throttling protects the authentication endpoint against brute-force
 * attacks.
 * </p>
 */
class LoginAttemptServiceTest {

    /**
     * Creates a service instance with very small lockout settings so tests remain
     * fast while still validating production logic.
     */
    private LoginAttemptService newServiceForTests() {
        return new LoginAttemptService(
                new SimpleMeterRegistry(),
                3,
                5,
                60);
    }

    @Test
    @DisplayName("verifyAllowed throws after reaching max failed attempts")
    void verifyAllowed_throwsAfterMaxFailures() {
        LoginAttemptService service = newServiceForTests();

        // Trigger exactly one lockout cycle.
        service.recordFailure("alice");
        service.recordFailure("alice");
        service.recordFailure("alice");

        TooManyLoginAttemptsException exception = assertThrows(
                TooManyLoginAttemptsException.class,
                () -> service.verifyAllowed("alice"));

        assertTrue(exception.getRetryAfter().compareTo(Duration.ZERO) > 0);
    }

    @Test
    @DisplayName("recordSuccess clears state so user can login again")
    void recordSuccess_clearsLockState() {
        LoginAttemptService service = newServiceForTests();

        service.recordFailure("alice");
        service.recordFailure("alice");
        service.recordFailure("alice");
        assertThrows(TooManyLoginAttemptsException.class, () -> service.verifyAllowed("alice"));

        // Successful authentication should reset lock/failure state.
        service.recordSuccess("alice");

        assertDoesNotThrow(() -> service.verifyAllowed("alice"));
    }

    @Test
    @DisplayName("verifyAllowed allows unknown principal without failures")
    void verifyAllowed_allowsUnknownPrincipal() {
        LoginAttemptService service = newServiceForTests();

        // No prior failures -> no lockout.
        assertDoesNotThrow(() -> service.verifyAllowed("new-user"));
    }
}
