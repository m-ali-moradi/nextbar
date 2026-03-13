package com.nextbar.usersservice.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nextbar.usersservice.service.TokenLifecycleService;
import com.nextbar.usersservice.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filter to authenticate JWT tokens and set the security context.
 * This filter is applied to every request and checks for a valid JWT token
 * in the Authorization header.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenLifecycleService tokenLifecycleService;

    /**
     * Filters incoming requests to authenticate JWT tokens and set the security
     * context.
     * 
     * @param request     The HTTP request.
     * @param response    The HTTP response.
     * @param filterChain The filter chain.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateAccessToken(token)
                || tokenLifecycleService.isAccessTokenRevoked(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims;
        String username;
        try {
            claims = jwtUtil.extractAllClaimsPublic(token);
            username = claims.getSubject();
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        List<String> roles = extractStringList(claims.get("roles"));
        List<String> assignments = extractStringList(claims.get("assignments"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (isAdmin(roles, assignments)) {
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities);
        authentication.setDetails(new RbacClaims(roles, assignments));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the user is an admin based on their roles and assignments.
     * 
     * @param roles       List of roles assigned to the user.
     * @param assignments List of assignments for the user.
     * @return True if the user is an admin, false otherwise.
     */
    private static boolean isAdmin(List<String> roles, List<String> assignments) {
        if (roles != null && roles.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r))) {
            return true;
        }
        if (assignments != null) {
            return assignments.stream().anyMatch(a -> a != null && a.toUpperCase().contains(":ADMIN:"));
        }
        return false;
    }

    /**
     * Extracts a list of strings from a claim object.
     * 
     * @param claim The claim object to extract the list from.
     * @return A list of strings extracted from the claim.
     */
    private static List<String> extractStringList(Object claim) {
        if (claim == null)
            return Collections.emptyList();
        if (claim instanceof List<?> list) {
            List<String> out = new ArrayList<>(list.size());
            for (Object v : list)
                out.add(String.valueOf(v));
            return out;
        }
        return Collections.emptyList();
    }
}
