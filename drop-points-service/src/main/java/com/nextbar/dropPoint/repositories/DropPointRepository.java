package de.fhdo.dropPointsSys.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fhdo.dropPointsSys.domain.DropPoint;
import de.fhdo.dropPointsSys.domain.DropPointStatus;

/**
 * Repository interface for managing DropPoint entities.
 * Provides methods to perform CRUD operations and custom queries based on DropPoint status.
 */
public interface DropPointRepository extends JpaRepository<DropPoint, Long> {
    List<DropPoint> findAllByStatus(DropPointStatus status);
}
