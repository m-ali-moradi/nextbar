package com.nextbar.usersservice.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextbar.usersservice.model.RefreshToken;

/**
 * Repository for managing RefreshToken entities.
 * Provides standard CRUD operations and custom query methods for RefreshToken.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    void deleteByExpiresAtBefore(Instant threshold);
}