package com.nextbar.usersservice.repository;

import com.nextbar.usersservice.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Permission entities.
 * Provides standard CRUD operations and custom query methods for Permission.
 */
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByCode(String code);
}
