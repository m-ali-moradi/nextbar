package com.nextbar.eventPlanner.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/****
 * Filter to verify internal requests from trusted services like the API Gateway.
 * It checks for a valid HMAC signature in the request headers to ensure authenticity.
 * This is important to prevent unauthorized access to internal endpoints that are not exposed publicly.
 */
@Component
public class InternalRequestVerificationFilter extends OncePerRequestFilter {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";
    private static final String INTERNAL_TIMESTAMP_HEADER = "X-Internal-Timestamp";
    private static final String INTERNAL_SIGNATURE_HEADER = "X-Internal-Signature";
    private static final long MAX_CLOCK_SKEW_MS = 5 * 60 * 1000;

    // Whether the internal request verification is enabled. Can be turned off for testing or if not needed.
    @Value("${security.internal.enabled}")
    private boolean enabled;
    // Secret key used for HMAC signing. 
    @Value("${security.internal.secret:}")
    private String secret;

    @PostConstruct
    @SuppressWarnings("unused")
    void validateConfiguration() {
        if (enabled && (secret == null || secret.isBlank())) {
            throw new IllegalStateException("security.internal.secret must be set when security.internal.enabled=true");
        }
    }

    @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled || isPublicPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String service = request.getHeader(INTERNAL_SERVICE_HEADER);
        String timestamp = request.getHeader(INTERNAL_TIMESTAMP_HEADER);
        String signature = request.getHeader(INTERNAL_SIGNATURE_HEADER);

        if (!"gateway".equals(service) || timestamp == null || signature == null || signature.isBlank()) {
            reject(response);
            return;
        }

        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            reject(response);
            return;
        }

        if (Math.abs(System.currentTimeMillis() - ts) > MAX_CLOCK_SKEW_MS) {
            reject(response);
            return;
        }

        String expected = sign(service, timestamp, request.getMethod(), request.getRequestURI(), secret);
        if (!constantTimeEquals(expected, signature)) {
            reject(response);
            return;
        }

        filterChain.doFilter(request, response);
    }
    // Check if the request URI is for a public path that does not require internal verification
    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
    // Reject the request with a 401 Unauthorized status and a message
    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Invalid internal request signature");
    }
    // Generate an HMAC signature for the request using the service name, timestamp, HTTP method, and path
    private String sign(String service, String timestamp, String method, String path, String secretKey) {
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
            throw new IllegalStateException("Failed to verify internal request", e);
        }
    }
    // Compare two strings in constant time to prevent timing attacks
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}