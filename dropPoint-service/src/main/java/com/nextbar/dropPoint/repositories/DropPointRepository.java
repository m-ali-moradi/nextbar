package com.nextbar.dropPoint.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextbar.dropPoint.domain.DropPoint;
import com.nextbar.dropPoint.domain.DropPointStatus;

/**
 * Repository interface for managing DropPoint entities.
 * Provides methods to perform CRUD operations and custom queries based on DropPoint status.
 */
public interface DropPointRepository extends JpaRepository<DropPoint, Long> {
    List<DropPoint> findAllByStatus(DropPointStatus status);
}
