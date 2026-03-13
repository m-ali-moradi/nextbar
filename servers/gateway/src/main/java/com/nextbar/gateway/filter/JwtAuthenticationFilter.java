package com.nextbar.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.net.HttpHeaders;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";
    private static final String INTERNAL_TIMESTAMP_HEADER = "X-Internal-Timestamp";
    private static final String INTERNAL_SIGNATURE_HEADER = "X-Internal-Signature";
    private static final Set<String> INTERNAL_ONLY_PATHS = Set.of(
            "/api/v1/users/token-status");
    private static final Set<String> PUBLIC_USERS_AUTH_PATHS = Set.of(
            "/api/v1/users/login",
            "/api/v1/users/refresh",
            "/api/v1/users/logout");
    private static final Set<String> LOGIN_PATHS = Set.of(
            "/api/v1/users/login");

    private final TokenStatusClient tokenStatusClient;
    private final LoginRateLimiter loginRateLimiter;

    public JwtAuthenticationFilter(TokenStatusClient tokenStatusClient, LoginRateLimiter loginRateLimiter) {
        this.tokenStatusClient = tokenStatusClient;
        this.loginRateLimiter = loginRateLimiter;
    }

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.internal.enabled:false}")
    private boolean internalSecurityEnabled;

    @Value("${security.internal.secret:}")
    private String internalSecuritySecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String requestPath = exchange.getRequest().getPath().value();

        // Apply rate limiting to login endpoints to mitigate brute-force attacks.
        if (LOGIN_PATHS.contains(requestPath)) {
            LoginRateLimiter.Decision decision = loginRateLimiter.evaluate(resolveClientAddress(exchange));
            if (!decision.allowed()) {
                return tooManyRequests(exchange, decision.retryAfter());
            }
        }

        // Block access to internal-only paths from external clients.
        if (INTERNAL_ONLY_PATHS.contains(requestPath)) {
            return unauthorized(exchange);
        }

        // Allow unauthenticated access to public auth endpoints, but still apply rate
        // limiting.
        if (PUBLIC_USERS_AUTH_PATHS.contains(requestPath)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        // Expecting Authorization header in the format "Bearer" and avoid null or
        // malformed headers to prevent unnecessary processing.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        // Validate JWT and extract claims. If invalid, respond with 401 Unauthorized.
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange);
        }

        // Extract roles claim if present, ensuring it's a list of strings. This allows
        // downstream services to make authorization decisions based on user roles.
        List<String> resolvedRoles = List.of();
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?> roleList) {
            resolvedRoles = roleList.stream().map(String::valueOf).collect(Collectors.toList());
        }
        // Extract assignments claim if present, ensuring it's a list of strings. This
        // allows downstream services to make authorization decisions based on user
        // assignments.
        List<String> resolvedAssignments = List.of();
        Object assignmentsClaim = claims.get("assignments");
        if (assignmentsClaim instanceof List<?> assignmentList) {
            resolvedAssignments = assignmentList.stream().map(String::valueOf).collect(Collectors.toList());
        }

        final List<String> roles = resolvedRoles;
        final List<String> assignments = resolvedAssignments;

        // Check if the token has been revoked by calling the Token Status Service. If
        // revoked, respond with 401 Unauthorized. This ensures that tokens that have
        // been invalidated (e.g., due to logout or security incidents) cannot be used.
        return tokenStatusClient.isRevoked(token).flatMap(revoked -> {
            if (revoked) {
                return unauthorized(exchange);
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    // Preserve Authorization for downstream services that validate JWTs
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header("X-User-Id", claims.getSubject())
                    .header("X-Roles", String.join(",", roles))
                    .header("X-Assignments", String.join(",", assignments))
                    .build();
            // If internal security is enabled and a secret is configured, sign the request
            // to allow downstream services to verify it as an internal request. This adds
            // an additional layer of trust for inter-service communication within the
            // cluster.
            if (internalSecurityEnabled && internalSecuritySecret != null && !internalSecuritySecret.isBlank()) {
                String method = exchange.getRequest().getMethod() != null
                        ? exchange.getRequest().getMethod().name()
                        : "GET";
                String path = exchange.getRequest().getPath().value();
                String timestamp = String.valueOf(System.currentTimeMillis());
                String signature = signInternalRequest("gateway", timestamp, method, path, internalSecuritySecret);

                mutatedRequest = mutatedRequest.mutate()
                        .header(INTERNAL_SERVICE_HEADER, "gateway")
                        .header(INTERNAL_TIMESTAMP_HEADER, timestamp)
                        .header(INTERNAL_SIGNATURE_HEADER, signature)
                        .build();
            }

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        });
    }

    // Helper method to create HMAC signature for internal requests. This allows
    // downstream services to verify that the request originated from the gateway
    // and has not been tampered with in transit.
    private String signInternalRequest(String service, String timestamp, String method, String path, String secretKey) {
        String payload = service + ":" + timestamp + ":" + method + ":" + path;
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(signatureBytes.length * 2);
            for (byte b : signatureBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to sign internal request", e);
        }
    }

    // Helper method to respond with 401 Unauthorized
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    // Helper method to resolve the client's IP address, considering common proxy
    // headers. This is used for rate limiting decisions to mitigate brute-force
    // attacks on login endpoints.
    private String resolveClientAddress(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        ServerHttpRequest request = exchange.getRequest();
        var remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }

    // Helper method to respond with 429 Too Many Requests and a Retry-After header
    private Mono<Void> tooManyRequests(ServerWebExchange exchange, Duration retryAfter) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        long retryAfterSeconds = Math.max(1L, retryAfter.toSeconds());
        response.getHeaders().set("Retry-After", String.valueOf(retryAfterSeconds));
        return response.setComplete();
    }
}
