package com.nextbar.usersservice.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextbar.usersservice.model.TokenBlacklistEntry;

/**
 * Repository for managing TokenBlacklistEntry entities.
 * Provides standard CRUD operations and custom query methods for
 * TokenBlacklistEntry.
 */
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklistEntry, UUID> {

    boolean existsByJti(String jti);

    void deleteByExpiresAtBefore(Instant threshold);
}