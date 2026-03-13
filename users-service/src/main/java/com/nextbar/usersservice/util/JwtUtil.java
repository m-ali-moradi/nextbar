package com.nextbar.usersservice.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for generating and validating JWT tokens.
 * This class handles token creation, parsing, refresh, and validation.
 */
@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private Long expiration;

    @Value("${security.jwt.refresh-expiration:1209600000}")
    private Long refreshExpiration;

    @Value("${security.jwt.ws-ticket-expiration:60000}")
    private Long wsTicketExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
                .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, expiration, "access");
    }

    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return createToken(claims, username, expiration, "access");
    }

    public String generateToken(String username, List<String> roles, List<String> assignments) {
        Map<String, Object> claims = new HashMap<>();
        if (roles != null && !roles.isEmpty()) {
            claims.put("roles", roles);
        }
        if (assignments != null && !assignments.isEmpty()) {
            claims.put("assignments", assignments);
        }
        return createToken(claims, username, expiration, "access");
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, refreshExpiration, "refresh");
    }

    public String generateWebSocketTicket(String username, List<String> roles, List<String> assignments) {
        Map<String, Object> claims = new HashMap<>();
        if (roles != null && !roles.isEmpty()) {
            claims.put("roles", roles);
        }
        if (assignments != null && !assignments.isEmpty()) {
            claims.put("assignments", assignments);
        }
        return createToken(claims, username, wsTicketExpiration, "ws_ticket");
    }

    /**
     * Creates a JWT token with the given claims, subject, expiration time, and
     * token type.
     * 
     * @param claims    The claims to include in the token.
     * @param subject   The subject of the token.
     * @param expiry    The expiration time of the token.
     * @param tokenType The type of the token (access or refresh).
     * @return The generated JWT token.
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiry, String tokenType) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiry))
                .id(UUID.randomUUID().toString())
                .claim("tokenType", tokenType)
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expirationDate = claims.getExpiration();
            if (expirationDate == null || expirationDate.before(new Date())) {
                return false;
            }

            String tokenType = claims.get("tokenType", String.class);
            return tokenType == null || "access".equalsIgnoreCase(tokenType);
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims extractAllClaimsPublic(String token) {
        return extractAllClaims(token);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public boolean isRefreshToken(String token) {
        try {
            String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
            return "refresh".equalsIgnoreCase(tokenType);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
            return tokenType == null || "access".equalsIgnoreCase(tokenType);
        } catch (Exception ex) {
            return false;
        }
    }
}
