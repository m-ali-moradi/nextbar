package de.fhdo.eventPlanner.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Filter that validates tokens and sets up Spring Security
 * context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && validateToken(token)) {
                Claims claims = parseToken(token);
                String username = claims.getSubject();
                List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
                        null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: {} with roles: {}", username,
                        authorities.stream().map(a -> a.getAuthority()).collect(Collectors.toList()));
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token expired\"}");
            return;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Extract roles from rbac claim structure
        Object rbacClaim = claims.get("rbac");
        if (rbacClaim instanceof Map) {
            Map<String, Object> rbac = (Map<String, Object>) rbacClaim;
            Object rolesObj = rbac.get("roles");

            if (rolesObj instanceof List) {
                List<String> roles = (List<String>) rolesObj;
                for (String role : roles) {
                    // Add ROLE_ prefix for Spring Security
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                }
            }
        }

        // Fallback: Check for simple roles claim
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List) {
            List<String> roles = (List<String>) rolesClaim;
            for (String role : roles) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                if (!authorities.contains(authority)) {
                    authorities.add(authority);
                }
            }
        }

        // Support assignments claims formatted as SERVICE:ROLE:RESOURCE
        Object assignmentsClaim = claims.get("assignments");
        if (assignmentsClaim instanceof List) {
            List<String> assignments = (List<String>) assignmentsClaim;
            for (String assignment : assignments) {
                if (assignment == null || assignment.isBlank()) {
                    continue;
                }

                String[] parts = assignment.split(":");
                if (parts.length < 2) {
                    continue;
                }

                String service = normalizeService(parts[0]);
                String role = normalizeRole(parts[1]);

                if ("ADMIN".equals(role)) {
                    addAuthority(authorities, "ROLE_ADMIN");
                }

                if ("EVENT".equals(service) && "MANAGER".equals(role)) {
                    addAuthority(authorities, "ROLE_EVENTPLANNER_MANAGER");
                    addAuthority(authorities, "ROLE_EVENT_PLANNER_MANAGER");
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

    private static String normalizeService(String raw) {
        String service = raw == null ? "" : raw.trim().toUpperCase();
        if (service.endsWith("_SERVICE")) {
            service = service.substring(0, service.length() - "_SERVICE".length());
        }
        if (service.endsWith("-SERVICE")) {
            service = service.substring(0, service.length() - "-SERVICE".length());
        }
        if ("EVENTPLANNER".equals(service) || "EVENT_PLANNER".equals(service)) {
            return "EVENT";
        }
        return service;
    }

    private static String normalizeRole(String raw) {
        String role = raw == null ? "" : raw.trim().toUpperCase();
        if (role.contains("ADMIN")) {
            return "ADMIN";
        }
        if (role.contains("MANAGER")) {
            return "MANAGER";
        }
        return role;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/swagger-ui.html");
    }
}
