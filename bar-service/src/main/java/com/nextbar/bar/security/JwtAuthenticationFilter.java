package com.nextbar.bar.security;

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
import lombok.RequiredArgsConstructor;

/**
 * Gateway header authentication filter for bar-service.
 * Trusts user identity forwarded by the API Gateway.
 */
@Component
@RequiredArgsConstructor
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

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (isAdmin(roles, assignments)) {
                    authorities.add(new SimpleGrantedAuthority(RbacRole.ADMIN.value()));
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                authToken.setDetails(new RbacClaims(roles, assignments));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Gateway authentication successful for user: {}", username);
            } else if (!isPublicPath(request.getRequestURI())) {
                String sourceIp = request.getHeader("X-Forwarded-For");
                if (sourceIp == null || sourceIp.isBlank()) {
                    sourceIp = request.getRemoteAddr();
                }
                log.warn("Missing gateway identity header: header={}, method={}, path={}, sourceIp={}",
                        HEADER_USER_ID,
                        request.getMethod(),
                        request.getRequestURI(),
                        sourceIp);
            }
        }

        filterChain.doFilter(request, response);
    }

    private static boolean isAdmin(List<String> roles, List<String> assignments) {
        if (roles != null && roles.stream().anyMatch(RbacRole.ADMIN::equalsIgnoreCase)) {
            return true;
        }
        if (assignments != null) {
            return assignments.stream().anyMatch(RbacRole.ADMIN::presentInAssignment);
        }
        return false;
    }

    private static List<String> parseCsvHeader(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) return Collections.emptyList();
        return List.of(headerValue.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    // Define public paths that do not require authentication
    private static boolean isPublicPath(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
