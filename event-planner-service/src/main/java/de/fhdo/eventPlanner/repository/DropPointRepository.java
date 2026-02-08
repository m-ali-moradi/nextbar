package de.fhdo.eventPlanner.repository;

import de.fhdo.eventPlanner.model.DropPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DropPoint entity operations.
 */
@Repository
public interface DropPointRepository extends JpaRepository<DropPoint, Long> {

    /**
     * Find all drop points for a specific event.
     */
    List<DropPoint> findByEventId(Long eventId);

    /**
     * Find all drop points with event occupancy flag set.
     */
    List<DropPoint> findByEventOccupancyTrue();

    /**
     * Find drop points by assigned staff.
     */
    List<DropPoint> findByAssignedStaffContainingIgnoreCase(String staffName);

    /**
     * Check if a drop point name exists for an event.
     */
    boolean existsByNameAndEventId(String name, Long eventId);
}
