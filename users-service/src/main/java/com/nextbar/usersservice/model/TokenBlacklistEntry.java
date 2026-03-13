package com.nextbar.usersservice.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a token blacklist entry in the system.
 * This entity is used to store and manage blacklisted tokens in the database.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "token_blacklist")
public class TokenBlacklistEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "jti", nullable = false, unique = true, length = 128)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}