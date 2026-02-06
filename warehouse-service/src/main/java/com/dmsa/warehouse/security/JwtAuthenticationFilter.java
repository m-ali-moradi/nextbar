package com.dmsa.warehouse.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT authentication filter.
 * Validates tokens from the Authorization header and sets up security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenValidator jwtValidator;

    public JwtAuthenticationFilter(JwtTokenValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtValidator.validateToken(jwt)) {
                String username = jwtValidator.getUsernameFromToken(jwt);
                List<String> roles = jwtValidator.getRolesFromToken(jwt);
                List<String> assignments = jwtValidator.getAssignmentsFromToken(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Set<SimpleGrantedAuthority> authorities = new HashSet<>();

                    authorities.addAll(roles.stream()
                        .filter(role -> role != null && !role.isBlank())
                        .map(role -> new SimpleGrantedAuthority(
                            role.startsWith("ROLE_") ? role : "ROLE_" + role))
                        .collect(Collectors.toSet()));

                    authorities.addAll(assignments.stream()
                        .map(this::mapAssignmentToAuthority)
                        .filter(a -> a != null)
                        .collect(Collectors.toSet()));

                    // Ensure at least ROLE_USER is present
                    if (authorities.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, List.copyOf(authorities));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated user: {} with roles: {}", username, roles);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process JWT authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip filter for public endpoints
        return path.startsWith("/actuator") ||
                path.startsWith("/h2-console") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs");
    }

    private SimpleGrantedAuthority mapAssignmentToAuthority(String assignment) {
        if (assignment == null || assignment.isBlank()) return null;

        String[] parts = assignment.split(":");
        if (parts.length < 2) return null;

        String service = normalizeService(parts[0]);
        String role = normalizeRole(parts[1]);

        if (service.isBlank() || role.isBlank()) return null;

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
        if (raw == null) return "";
        String s = raw.trim().toUpperCase();
        if (s.endsWith("_SERVICE")) s = s.substring(0, s.length() - "_SERVICE".length());
        if (s.endsWith("-SERVICE")) s = s.substring(0, s.length() - "-SERVICE".length());
        if ("DROPPOINT".equals(s) || "DROPPOINTS".equals(s)) return "DROP_POINT";
        return s;
    }

    private String normalizeRole(String raw) {
        if (raw == null) return "";
        return raw.trim().toUpperCase();
    }
}
