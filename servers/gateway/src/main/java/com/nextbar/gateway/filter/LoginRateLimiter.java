package com.nextbar.gateway.filter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * A simple in-memory rate limiter for login attempts, keyed by client IP or username.
 * This is a basic implementation.
 * such as distributed storage, more efficient data structures, and cleanup of old entries.
 */

@Component
public class LoginRateLimiter {

    private final ConcurrentHashMap<String, ClientWindow> windows = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int maxRequests;
    private final Duration windowDuration;
    private final Duration blockDuration;
    private final Counter blockedRequestsCounter;

    // Constructor with configuration properties for rate limiting parameters
    public LoginRateLimiter(
            MeterRegistry meterRegistry,
            @Value("${security.rate-limit.login.max-requests:10}") int maxRequests,
            @Value("${security.rate-limit.login.window-seconds:60}") long windowSeconds,
            @Value("${security.rate-limit.login.block-seconds:300}") long blockSeconds) {
        this.clock = Clock.systemUTC();
        this.maxRequests = Math.max(1, maxRequests);
        this.windowDuration = Duration.ofSeconds(Math.max(1L, windowSeconds));
        this.blockDuration = Duration.ofSeconds(Math.max(1L, blockSeconds));
        this.blockedRequestsCounter = Counter.builder("gateway.auth.login.rate_limit.blocked")
                .description("Number of blocked login requests due to gateway rate limiting")
                .register(meterRegistry);
    }

    // Evaluates whether a login attempt should be allowed or blocked based on the client's request history
    public Decision evaluate(String key) {
        String normalizedKey = normalizeKey(key);
        Instant now = Instant.now(clock);
        ClientWindow state = windows.computeIfAbsent(normalizedKey, ignored -> new ClientWindow(now));

        synchronized (state) {
            if (state.blockedUntil != null && now.isBefore(state.blockedUntil)) {
                blockedRequestsCounter.increment();
                return Decision.deny(Duration.between(now, state.blockedUntil));
            }

            if (state.windowStart == null || now.isAfter(state.windowStart.plus(windowDuration))) {
                state.windowStart = now;
                state.requestCount = 0;
            }

            state.requestCount++;
            if (state.requestCount > maxRequests) {
                state.blockedUntil = now.plus(blockDuration);
                state.requestCount = 0;
                state.windowStart = now;
                blockedRequestsCounter.increment();
                return Decision.deny(blockDuration);
            }

            return Decision.allow();
        }
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank()) {
            return "unknown";
        }
        return key.trim();
    }

    private static final class ClientWindow {
        private Instant windowStart;
        private int requestCount;
        private Instant blockedUntil;

        private ClientWindow(Instant now) {
            this.windowStart = now;
            this.requestCount = 0;
            this.blockedUntil = null;
        }
    }

    public record Decision(boolean allowed, Duration retryAfter) {
        public static Decision allow() {
            return new Decision(true, Duration.ZERO);
        }

        public static Decision deny(Duration retryAfter) {
            Duration safeRetryAfter = retryAfter == null || retryAfter.isNegative() ? Duration.ofSeconds(1) : retryAfter;
            return new Decision(false, safeRetryAfter);
        }
    }
}
