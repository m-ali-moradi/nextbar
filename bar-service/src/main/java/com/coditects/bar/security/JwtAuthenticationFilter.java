package com.coditects.bar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT authentication filter for bar-service.
 * Validates tokens from the Authorization header.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                final String jwt = authHeader.substring(7);
                
                if (jwtUtil.validateToken(jwt)) {
                    String username = jwtUtil.getUsernameFromToken(jwt);
                    
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("JWT authentication successful for user: {}", username);
                    }
                }
            } catch (Exception e) {
                logger.error("JWT authentication failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
