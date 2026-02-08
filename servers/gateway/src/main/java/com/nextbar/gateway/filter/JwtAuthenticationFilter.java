package com.nextbar.gateway.filter;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.net.HttpHeaders;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    @Value("${security.jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return unauthorized(exchange);
        }

        // Forward user info to downstream services
        List<String> roles = List.of();
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?> roleList) {
            roles = roleList.stream().map(String::valueOf).collect(Collectors.toList());
        }

        List<String> assignments = List.of();
        Object assignmentsClaim = claims.get("assignments");
        if (assignmentsClaim instanceof List<?> assignmentList) {
            assignments = assignmentList.stream().map(String::valueOf).collect(Collectors.toList());
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            // Preserve Authorization for downstream services that validate JWTs
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .header("X-User-Id", claims.getSubject())
            .header("X-Roles", String.join(",", roles))
            .header("X-Assignments", String.join(",", assignments))
            .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
