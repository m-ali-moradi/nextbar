package com.nextbar.usersservice.repository;
}
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
public interface UserRepository extends JpaRepository<User, UUID> {

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nextbar.usersservice.model.User;


