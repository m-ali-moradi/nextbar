package de.fhdo.eventPlanner.repository;

import de.fhdo.eventPlanner.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // JpaRepository already gives you: findAll(), findById(), save(), deleteById(), etc.
}
