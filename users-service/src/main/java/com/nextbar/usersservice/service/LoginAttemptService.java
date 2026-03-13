package com.nextbar.usersservice.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for tracking login attempts and enforcing temporary lockouts after too many failures.
 * This is a simple in-memory implementation. for robust in production use, higher-performance and distributed storage solutions should be considered,
 */

@Service
@Slf4j
public class LoginAttemptService {

    private final Map<String, PrincipalState> states = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int maxAttempts;
    private final Duration baseLockDuration;
    private final Duration maxLockDuration;
    private final Counter loginFailuresCounter;
    private final Counter lockoutsCounter;

    public LoginAttemptService(
            MeterRegistry meterRegistry,
            @Value("${security.login-protection.max-attempts:5}") int maxAttempts,
            @Value("${security.login-protection.base-lock-seconds:30}") long baseLockSeconds,
            @Value("${security.login-protection.max-lock-seconds:900}") long maxLockSeconds) {
        this.clock = Clock.systemUTC();
        this.maxAttempts = Math.max(1, maxAttempts);
        this.baseLockDuration = Duration.ofSeconds(Math.max(1L, baseLockSeconds));
        this.maxLockDuration = Duration.ofSeconds(Math.max(1L, maxLockSeconds));

        this.loginFailuresCounter = Counter.builder("users.auth.login.failures")
                .description("Total failed login attempts")
                .register(meterRegistry);

        this.lockoutsCounter = Counter.builder("users.auth.login.lockouts")
                .description("Total login lockout events")
                .register(meterRegistry);

        Gauge.builder("users.auth.login.locked.principals", states, map ->
                map.values().stream().filter(PrincipalState::isCurrentlyLocked).count())
                .description("Number of principals currently in temporary lockout")
                .register(meterRegistry);
    }

    public void verifyAllowed(String username) {
        String key = normalizePrincipal(username);
        PrincipalState state = states.get(key);
        if (state == null) {
            return;
        }

        Instant now = Instant.now(clock);
        synchronized (state) {
            if (state.lockedUntil != null && now.isBefore(state.lockedUntil)) {
                throw new TooManyLoginAttemptsException(Duration.between(now, state.lockedUntil));
            }
        }
    }

    public void recordFailure(String username) {
        String key = normalizePrincipal(username);
        Instant now = Instant.now(clock);
        PrincipalState state = states.computeIfAbsent(key, ignored -> new PrincipalState());

        synchronized (state) {
            loginFailuresCounter.increment();
            state.failedAttempts++;

            if (state.failedAttempts >= maxAttempts) {
                state.lockoutCount++;
                state.failedAttempts = 0;

                Duration lockDuration = calculateLockDuration(state.lockoutCount);
                state.lockedUntil = now.plus(lockDuration);
                lockoutsCounter.increment();

                log.warn("Temporary login lockout engaged for principal '{}' for {} seconds", key,
                        lockDuration.toSeconds());
            }
        }
    }

    public void recordSuccess(String username) {
        String key = normalizePrincipal(username);
        states.remove(key);
    }

    private Duration calculateLockDuration(int lockoutCount) {
        int exponent = Math.min(20, Math.max(0, lockoutCount - 1));
        long multiplier = 1L << exponent;
        long durationSeconds = baseLockDuration.toSeconds() * multiplier;
        return Duration.ofSeconds(Math.min(durationSeconds, maxLockDuration.toSeconds()));
    }

    private String normalizePrincipal(String username) {
        if (username == null || username.isBlank()) {
            return "unknown";
        }
        return username.trim().toLowerCase();
    }

    private final class PrincipalState {
        private int failedAttempts;
        private int lockoutCount;
        private Instant lockedUntil;

        private boolean isCurrentlyLocked() {
            return lockedUntil != null && Instant.now(clock).isBefore(lockedUntil);
        }
    }
}
