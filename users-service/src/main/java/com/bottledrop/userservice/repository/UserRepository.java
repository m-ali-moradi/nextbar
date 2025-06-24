package com.bottledrop.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bottledrop.userservice.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
