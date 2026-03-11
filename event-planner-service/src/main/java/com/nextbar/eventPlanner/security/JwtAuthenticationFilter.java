package de.fhdo.eventPlanner.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * Gateway header authentication filter for event-planner-service.
 * Trusts user identity forwarded by the API Gateway via headers.
 * 
 * The Gateway validates the JWT and forwards user info via:
 * - X-User-Id: The authenticated username
 * - X-Roles: Comma-separated list of roles (e.g., "ADMIN", "MANAGER")
 * - X-Assignments: Comma-separated list of service:role:resourceId assignments
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLES = "X-Roles";
    private static final String HEADER_ASSIGNMENTS = "X-Assignments";
    private static final String EVENT_SERVICE = "EVENT";
    private static final String ROLE_MANAGER = "MANAGER";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String username = request.getHeader(HEADER_USER_ID);

        if (path.startsWith("/api/v1/") && (username == null || username.isBlank())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Gateway authentication required");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            if (username != null && !username.isBlank()) {
                List<String> roles = parseCsvHeader(request.getHeader(HEADER_ROLES));
                List<String> assignments = parseCsvHeader(request.getHeader(HEADER_ASSIGNMENTS));

                List<SimpleGrantedAuthority> authorities = extractAuthorities(roles, assignments);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
                        authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Gateway authentication successful for user: {} with authorities: {}",
                        username,
                        authorities.stream().map(SimpleGrantedAuthority::getAuthority).collect(Collectors.toList()));
            } else {
                log.debug("No X-User-Id header found - request will be unauthenticated");
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract Spring Security authorities from roles and assignments.
     *
     * Keeps authorization service-scoped: event access is granted via EVENT:MANAGER
     * assignments, not by generic MANAGER role names from other services.
     */
    private List<SimpleGrantedAuthority> extractAuthorities(List<String> roles, List<String> assignments) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Admin authority
        if (roles != null) {
            for (String role : roles) {
                if ("ADMIN".equalsIgnoreCase(role)) {
                    addAuthority(authorities, "ROLE_ADMIN");
                }
            }
        }

        // Event planner manager authority (service-scoped)
        if (assignments != null) {
            for (String assignment : assignments) {
                if (assignment == null || assignment.isBlank())
                    continue;

                String[] parts = assignment.split(":");
                if (parts.length < 2)
                    continue;

                String service = parts[0].trim().toUpperCase();
                String role = parts[1].trim().toUpperCase();

                // Admin in any service gets ROLE_ADMIN
                if ("ADMIN".equals(role)) {
                    addAuthority(authorities, "ROLE_ADMIN");
                }

                if (EVENT_SERVICE.equals(service) && ROLE_MANAGER.equals(role)) {
                    addAuthority(authorities, "ROLE_EVENT_MANAGER");
                }
            }
        }

        return authorities;
    }

    private static void addAuthority(List<SimpleGrantedAuthority> authorities, String value) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(value);
        if (!authorities.contains(authority)) {
            authorities.add(authority);
        }
    }

    private static List<String> parseCsvHeader(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(headerValue.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip filter for actuator and swagger endpoints
        return path.startsWith("/actuator") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs");
    }
}
