package com.nextbar.gateway.config;

import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class SecurityHeadersWebFilter implements WebFilter {

    private final String contentSecurityPolicy;
    private final String referrerPolicy;
    private final String permissionsPolicy;
    private final long hstsMaxAge;
    private final boolean hstsIncludeSubDomains;
    private final boolean hstsPreload;

    public SecurityHeadersWebFilter(
            @Value("${security.headers.content-security-policy:default-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'}")
            String contentSecurityPolicy,
            @Value("${security.headers.referrer-policy:no-referrer}") String referrerPolicy,
            @Value("${security.headers.permissions-policy:geolocation=(), microphone=(), camera=()}")
            String permissionsPolicy,
            @Value("${security.headers.hsts.max-age:31536000}") long hstsMaxAge,
            @Value("${security.headers.hsts.include-subdomains:true}") boolean hstsIncludeSubDomains,
            @Value("${security.headers.hsts.preload:false}") boolean hstsPreload) {
        this.contentSecurityPolicy = contentSecurityPolicy;
        this.referrerPolicy = referrerPolicy;
        this.permissionsPolicy = permissionsPolicy;
        this.hstsMaxAge = Math.max(0L, hstsMaxAge);
        this.hstsIncludeSubDomains = hstsIncludeSubDomains;
        this.hstsPreload = hstsPreload;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        addIfAbsent(headers, "Content-Security-Policy", contentSecurityPolicy);
        addIfAbsent(headers, "X-Content-Type-Options", "nosniff");
        addIfAbsent(headers, "X-Frame-Options", "DENY");
        addIfAbsent(headers, "Referrer-Policy", referrerPolicy);
        addIfAbsent(headers, "Permissions-Policy", permissionsPolicy);

        if (isSecureRequest(exchange.getRequest())) {
            addIfAbsent(headers, "Strict-Transport-Security", buildHstsHeader());
        }

        return chain.filter(exchange);
    }

    private void addIfAbsent(HttpHeaders headers, String name, String value) {
        if (!headers.containsKey(name) && value != null && !value.isBlank()) {
            headers.add(Objects.requireNonNull(name), Objects.requireNonNull(value));
        }
    }

    private boolean isSecureRequest(ServerHttpRequest request) {
        String forwardedProto = request.getHeaders().getFirst("X-Forwarded-Proto");
        if (forwardedProto != null && !forwardedProto.isBlank()) {
            return "https".equalsIgnoreCase(forwardedProto.trim());
        }

        String scheme = request.getURI().getScheme();
        return scheme != null && "https".equals(scheme.toLowerCase(Locale.ROOT));
    }

    private String buildHstsHeader() {
        StringBuilder value = new StringBuilder("max-age=").append(hstsMaxAge);
        if (hstsIncludeSubDomains) {
            value.append("; includeSubDomains");
        }
        if (hstsPreload) {
            value.append("; preload");
        }
        return value.toString();
    }
}
