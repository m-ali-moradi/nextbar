package com.nextbar.usersservice.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.usersservice.model.RefreshToken;
import com.nextbar.usersservice.model.TokenBlacklistEntry;
import com.nextbar.usersservice.model.User;
import com.nextbar.usersservice.repository.RefreshTokenRepository;
import com.nextbar.usersservice.repository.TokenBlacklistRepository;
import com.nextbar.usersservice.util.JwtUtil;

/**
 * Service for managing the lifecycle of JWT tokens (access tokens and refresh
 * tokens).
 * Handles token creation, validation, revocation, and cleanup.
 */
@Service
public class TokenLifecycleService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtUtil jwtUtil;

    public TokenLifecycleService(RefreshTokenRepository refreshTokenRepository,
            TokenBlacklistRepository tokenBlacklistRepository,
            JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Creates a new refresh token for the given user.
     * 
     * @param user The user for whom to create the refresh token.
     * @return The newly created refresh token.
     */
    @Transactional
    public String createRefreshToken(User user) {
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hashToken(refreshToken));
        entity.setExpiresAt(jwtUtil.extractExpiration(refreshToken).toInstant());
        entity.setRevoked(false);
        entity.setCreatedAt(Instant.now());
        refreshTokenRepository.save(entity);
        return refreshToken;
    }

    /**
     * Revokes a refresh token, preventing it from being used for authentication.
     * 
     * @param refreshToken The refresh token to revoke.
     */
    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String tokenHash = hashToken(refreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(entity -> {
                    entity.setRevoked(true);
                    refreshTokenRepository.save(entity);
                });
    }

    /**
     * Revokes an access token by adding it to the blacklist.
     * 
     * @param accessToken The access token to revoke.
     */
    @Transactional
    public void revokeAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        if (!jwtUtil.validateToken(accessToken) || !jwtUtil.isAccessToken(accessToken)) {
            return;
        }

        String jti = jwtUtil.extractJti(accessToken);
        if (jti == null || jti.isBlank()) {
            return;
        }
        if (tokenBlacklistRepository.existsByJti(jti)) {
            return;
        }

        TokenBlacklistEntry entry = new TokenBlacklistEntry();
        entry.setJti(jti);
        entry.setExpiresAt(jwtUtil.extractExpiration(accessToken).toInstant());
        entry.setCreatedAt(Instant.now());
        tokenBlacklistRepository.save(entry);
    }

    /**
     * Checks if an access token has been revoked.
     * 
     * @param token The access token to check.
     * @return true if the access token is revoked, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean isAccessTokenRevoked(String token) {
        String jti = jwtUtil.extractJti(token);
        return jti != null && tokenBlacklistRepository.existsByJti(jti);
    }

    /**
     * Validates a refresh token and resolves the associated user.
     * 
     * @param refreshToken The refresh token to validate.
     * @return The user associated with the refresh token.
     * @throws IllegalArgumentException if the refresh token is invalid, revoked,
     *                                  expired, or the subject mismatch.
     */
    @Transactional(readOnly = true)
    public User validateRefreshTokenAndResolveUser(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String tokenHash = hashToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token revoked or unknown"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        String usernameInToken = jwtUtil.extractUsername(refreshToken);
        User user = stored.getUser();
        if (user == null || user.getUsername() == null || !user.getUsername().equals(usernameInToken)) {
            throw new IllegalArgumentException("Refresh token subject mismatch");
        }
        if (!user.isEnabled() || user.isLocked()) {
            throw new IllegalArgumentException("User is disabled or locked");
        }
        return user;
    }

    /**
     * Rotates a refresh token by revoking the old one and creating a new one.
     * This is used to prevent token reuse and maintain security.
     * 
     * @param user            The user for whom to rotate the refresh token.
     * @param oldRefreshToken The old refresh token to revoke.
     * @return The new refresh token.
     */
    @Transactional
    public String rotateRefreshToken(User user, String oldRefreshToken) {
        revokeRefreshToken(oldRefreshToken);
        return createRefreshToken(user);
    }

    /**
     * Cleans up expired tokens from the database.
     * This method should be called periodically (e.g., via a scheduled task)
     * to remove old and invalid tokens.
     */
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        refreshTokenRepository.deleteByExpiresAtBefore(now);
        tokenBlacklistRepository.deleteByExpiresAtBefore(now);
    }

    /**
     * Hashes a token using SHA-256 for secure storage.
     * 
     * @param token The token to hash.
     * @return The SHA-256 hash of the token.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}