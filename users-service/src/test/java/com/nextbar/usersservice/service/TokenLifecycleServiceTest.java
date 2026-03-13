package com.nextbar.usersservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nextbar.usersservice.model.RefreshToken;
import com.nextbar.usersservice.model.TokenBlacklistEntry;
import com.nextbar.usersservice.model.User;
import com.nextbar.usersservice.repository.RefreshTokenRepository;
import com.nextbar.usersservice.repository.TokenBlacklistRepository;
import com.nextbar.usersservice.util.JwtUtil;

/**
 * Unit tests for {@link TokenLifecycleService}.
 *
 * <p>
 * These tests focus on the most security-critical behaviors: storing refresh
 * tokens as hashes (not plain text), revoking access tokens safely, and
 * validating refresh-token ownership.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class TokenLifecycleServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenLifecycleService tokenLifecycleService;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    @Captor
    private ArgumentCaptor<TokenBlacklistEntry> blacklistCaptor;

    @Test
    @DisplayName("createRefreshToken stores hashed token and returns original token")
    void createRefreshToken_storesHashAndReturnsRawToken() {
        User user = new User();
        user.setUsername("alice");

        String rawRefreshToken = "raw-refresh-token";
        Date expiration = Date.from(Instant.now().plusSeconds(3600));

        when(jwtUtil.generateRefreshToken("alice")).thenReturn(rawRefreshToken);
        when(jwtUtil.extractExpiration(rawRefreshToken)).thenReturn(expiration);

        String result = tokenLifecycleService.createRefreshToken(user);

        assertEquals(rawRefreshToken, result);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken stored = refreshTokenCaptor.getValue();
        assertEquals(user, stored.getUser());
        assertEquals(expiration.toInstant(), stored.getExpiresAt());
        assertNotEquals(rawRefreshToken, stored.getTokenHash());
        assertTrue(stored.getTokenHash().matches("[0-9a-f]{64}"));
    }

    @Test
    @DisplayName("revokeAccessToken stores blacklist entry for valid non-revoked token")
    void revokeAccessToken_savesBlacklistEntry_whenValidAndNotAlreadyRevoked() {
        String accessToken = "access-token";
        Date expiration = Date.from(Instant.now().plusSeconds(600));

        when(jwtUtil.validateToken(accessToken)).thenReturn(true);
        when(jwtUtil.isAccessToken(accessToken)).thenReturn(true);
        when(jwtUtil.extractJti(accessToken)).thenReturn("jti-123");
        when(jwtUtil.extractExpiration(accessToken)).thenReturn(expiration);
        when(tokenBlacklistRepository.existsByJti("jti-123")).thenReturn(false);

        tokenLifecycleService.revokeAccessToken(accessToken);

        verify(tokenBlacklistRepository).save(blacklistCaptor.capture());
        TokenBlacklistEntry entry = blacklistCaptor.getValue();
        assertEquals("jti-123", entry.getJti());
        assertEquals(expiration.toInstant(), entry.getExpiresAt());
    }

    @Test
    @DisplayName("revokeAccessToken does nothing for invalid token")
    void revokeAccessToken_doesNothing_whenTokenInvalid() {
        when(jwtUtil.validateToken("bad-token")).thenReturn(false);

        tokenLifecycleService.revokeAccessToken("bad-token");

        verify(tokenBlacklistRepository, never()).save(any(TokenBlacklistEntry.class));
    }

    @Test
    @DisplayName("validateRefreshTokenAndResolveUser throws on subject mismatch")
    void validateRefreshTokenAndResolveUser_throws_whenSubjectDoesNotMatchUser() {
        String refreshToken = "refresh-token";

        User storedUser = new User();
        storedUser.setUsername("alice");
        storedUser.setEnabled(true);
        storedUser.setLocked(false);

        RefreshToken storedEntity = new RefreshToken();
        storedEntity.setUser(storedUser);
        storedEntity.setExpiresAt(Instant.now().plusSeconds(1200));
        storedEntity.setRevoked(false);

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(anyString()))
                .thenReturn(Optional.of(storedEntity));
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("bob");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tokenLifecycleService.validateRefreshTokenAndResolveUser(refreshToken));

        assertEquals("Refresh token subject mismatch", exception.getMessage());
    }
}
