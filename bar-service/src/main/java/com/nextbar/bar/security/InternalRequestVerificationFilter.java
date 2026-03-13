package com.nextbar.bar.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to verify internal requests from the API Gateway using HMAC
 * signatures.
 * this filter ensures that only requests with a valid signature, correct
 * timestamp, and required headers are allowed to access protected endpoints
 * only from the API Gateway.
 * It helps to secure internal communication between services and prevent
 * unauthorized access or replay attacks.
 * 
 */
@Component
public class InternalRequestVerificationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalRequestVerificationFilter.class);

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";
    private static final String INTERNAL_TIMESTAMP_HEADER = "X-Internal-Timestamp";
    private static final String INTERNAL_SIGNATURE_HEADER = "X-Internal-Signature";
    private static final long MAX_CLOCK_SKEW_MS = 5 * 60 * 1000;

    // Configuration properties
    @Value("${security.internal.enabled:false}")
    private boolean enabled;

    // The secret key used for signing and verifying internal requests.
    @Value("${security.internal.secret:}")
    private String secret;

    @PostConstruct
    void validateConfiguration() {
        if (enabled && (secret == null || secret.isBlank())) {
            throw new IllegalStateException("security.internal.secret must be set when security.internal.enabled=true");
        }
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
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
            auditReject(request, "missing-or-invalid-required-headers");
            reject(response);
            return;
        }

        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            auditReject(request, "invalid-timestamp-format");
            reject(response);
            return;
        }

        if (Math.abs(System.currentTimeMillis() - ts) > MAX_CLOCK_SKEW_MS) {
            auditReject(request, "timestamp-out-of-allowed-skew");
            reject(response);
            return;
        }

        String expected = sign(service, timestamp, request.getMethod(), request.getRequestURI(), secret);
        if (!constantTimeEquals(expected, signature)) {
            auditReject(request, "signature-mismatch");
            reject(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Invalid internal request signature");
    }

    private void auditReject(HttpServletRequest request, String reason) {
        String sourceIp = request.getHeader("X-Forwarded-For");
        if (sourceIp == null || sourceIp.isBlank()) {
            sourceIp = request.getRemoteAddr();
        }
        LOGGER.warn("Rejected internal request: reason={}, method={}, path={}, sourceIp={}",
                reason,
                request.getMethod(),
                request.getRequestURI(),
                sourceIp);
    }

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