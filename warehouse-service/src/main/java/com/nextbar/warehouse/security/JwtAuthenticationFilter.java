package com.dmsa.warehouse.security;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gateway header authentication filter for warehouse-service.
 * Trusts user identity forwarded by the API Gateway via headers.
 *
 * The Gateway validates the JWT and forwards user info via:
 * - X-User-Id: The authenticated username
 * - X-Roles: Comma-separated list of roles
 * - X-Assignments: Comma-separated list of service:role:resourceId assignments
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLES = "X-Roles";
    private static final String HEADER_ASSIGNMENTS = "X-Assignments";

    @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = request.getHeader(HEADER_USER_ID);
            if (username != null && !username.isBlank()) {
                List<String> roles = parseCsvHeader(request.getHeader(HEADER_ROLES));
                List<String> assignments = parseCsvHeader(request.getHeader(HEADER_ASSIGNMENTS));

                Set<SimpleGrantedAuthority> authorities = new HashSet<>();

                // Map roles
                for (String role : roles) {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    }
                    authorities.add(new SimpleGrantedAuthority(
                            role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase()));
                }

                // Map assignments to authorities
                for (String assignment : assignments) {
                    SimpleGrantedAuthority authority = mapAssignmentToAuthority(assignment);
                    if (authority != null) {
                        authorities.add(authority);
                    }
                }

                // Ensure at least ROLE_USER is present
                if (authorities.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, List.copyOf(authorities));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Gateway authentication successful for user: {} with roles: {}",
                        username, authorities);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator") ||
                path.startsWith("/h2-console") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs");
    }

    private SimpleGrantedAuthority mapAssignmentToAuthority(String assignment) {
        if (assignment == null || assignment.isBlank())
            return null;

        String[] parts = assignment.split(":");
        if (parts.length < 2)
            return null;

        String service = normalizeService(parts[0]);
        String role = normalizeRole(parts[1]);

        if (service.isBlank() || role.isBlank())
            return null;

        if ("ADMIN".equals(role)) {
            return new SimpleGrantedAuthority("ROLE_ADMIN");
        }

        String mappedRole;
        if (role.contains("MANAGER")) {
            mappedRole = "ROLE_" + service + "_MANAGER";
        } else if (role.contains("OPERATOR") || role.contains("BARTENDER")) {
            mappedRole = "ROLE_" + service + "_OPERATOR";
        } else if (role.startsWith("ROLE_")) {
            mappedRole = role;
        } else if (role.contains("_")) {
            mappedRole = "ROLE_" + role;
        } else {
            mappedRole = "ROLE_" + service + "_" + role;
        }

        return new SimpleGrantedAuthority(mappedRole);
    }

    private String normalizeService(String raw) {
        if (raw == null)
            return "";
        String s = raw.trim().toUpperCase();
        if (s.endsWith("_SERVICE"))
            s = s.substring(0, s.length() - "_SERVICE".length());
        if (s.endsWith("-SERVICE"))
            s = s.substring(0, s.length() - "-SERVICE".length());
        if ("DROPPOINT".equals(s) || "DROPPOINTS".equals(s))
            return "DROP_POINT";
        return s;
    }

    private String normalizeRole(String raw) {
        if (raw == null)
            return "";
        return raw.trim().toUpperCase();
    }

    private static List<String> parseCsvHeader(String headerValue) {
        if (headerValue == null || headerValue.isBlank())
            return Collections.emptyList();
        return List.of(headerValue.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
