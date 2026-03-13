package com.nextbar.usersservice.repository;

import com.nextbar.usersservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Role entities.
 * Provides standard CRUD operations and custom query methods for Role.
 */
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}
