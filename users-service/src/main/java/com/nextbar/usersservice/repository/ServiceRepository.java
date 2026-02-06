package com.nextbar.usersservice.repository;

import com.nextbar.usersservice.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Optional<Service> findByCode(String code);
}

