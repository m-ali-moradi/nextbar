package com.nextbar.usersservice.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nextbar.usersservice.model.User;

/**
 * Repository for managing User entities.
 * Provides standard CRUD operations and custom query methods for User.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
