package com.nextbar.usersservice.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nextbar.usersservice.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtUtil.extractAllClaimsPublic(token);
        String username = claims.getSubject();

        List<String> roles = extractStringList(claims.get("roles"));
        List<String> assignments = extractStringList(claims.get("assignments"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (isAdmin(roles, assignments)) {
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );
        authentication.setDetails(new RbacClaims(roles, assignments));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private static boolean isAdmin(List<String> roles, List<String> assignments) {
        if (roles != null && roles.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r))) {
            return true;
        }
        if (assignments != null) {
            return assignments.stream().anyMatch(a -> a != null && a.toUpperCase().contains(":ADMIN:"));
        }
        return false;
    }

    private static List<String> extractStringList(Object claim) {
        if (claim == null) return Collections.emptyList();
        if (claim instanceof List<?> list) {
            List<String> out = new ArrayList<>(list.size());
            for (Object v : list) out.add(String.valueOf(v));
            return out;
        }
        return Collections.emptyList();
    }
}
