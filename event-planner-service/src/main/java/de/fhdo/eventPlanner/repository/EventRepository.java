package de.fhdo.eventPlanner.repository;

import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Event entity operations.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Find all events by status.
     */
    List<Event> findByStatus(EventStatus status);

    /**
     * Find all events scheduled on a specific date.
     */
    List<Event> findByDate(LocalDate date);

    /**
     * Find all events at a specific location.
     */
    List<Event> findByLocationContainingIgnoreCase(String location);

    /**
     * Find all public events.
     */
    List<Event> findByIsPublicTrue();

    /**
     * Find events by organizer email.
     */
    List<Event> findByOrganizerEmail(String organizerEmail);

    /**
     * Find events by status ordered by date.
     */
    List<Event> findByStatusOrderByDateAsc(EventStatus status);

    /**
     * Find upcoming events (scheduled and not yet started).
     */
    List<Event> findByStatusAndDateGreaterThanEqual(EventStatus status, LocalDate date);
}
