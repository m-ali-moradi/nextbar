package com.nextbar.usersservice.repository;

import com.nextbar.usersservice.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Service entities.
 * Provides standard CRUD operations and custom query methods for Service.
 */
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Optional<Service> findByCode(String code);
}
