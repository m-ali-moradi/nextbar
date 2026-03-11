package de.fhdo.eventPlanner.repository;

import de.fhdo.eventPlanner.model.Bar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Bar entity operations.
 */
@Repository
public interface BarRepository extends JpaRepository<Bar, Long> {

    /**
     * Find all bars for a specific event.
     */
    List<Bar> findByEventId(Long eventId);

    /**
     * Find bars by event ID and load stocks eagerly.
     */
    @Query("SELECT DISTINCT b FROM Bar b LEFT JOIN FETCH b.stocks WHERE b.event.id = :eventId")
    List<Bar> findByEventIdWithStocks(@Param("eventId") Long eventId);

    /**
     * Check if a bar name exists for an event.
     */
    boolean existsByNameAndEventId(String name, Long eventId);
}
