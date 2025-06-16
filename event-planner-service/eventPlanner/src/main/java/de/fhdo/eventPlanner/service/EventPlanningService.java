package de.fhdo.eventPlanner.service;

import de.fhdo.eventPlanner.model.*;
import de.fhdo.eventPlanner.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Optional;

@Service
public class EventPlanningService {

    private final EventRepository eventRepository;

    @Autowired
    public EventPlanningService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> findEventById(Long id) {
        return eventRepository.findById(id);
    }

    /**
     * Create or update an event (with bars & drop points). We validate:
     *   – For each BarPlan: sum(drink quantities) ≤ totalCapacity
     */
    @Transactional
    public Event saveEvent(Event event) {
        // Validate each BarPlan’s beverage assignment
        for (BarPlan bar : event.getBars()) {
            int sum = bar.getTotalAssignedDrinkQuantity();
            if (sum > bar.getTotalCapacity()) {
                throw new ValidationException(
                        "Total assigned drink quantity (" + sum + ") exceeds bar capacity (" + bar.getTotalCapacity() + ") " +
                                "for bar: " + bar.getBarName());
            }
        }

        // (You could add more validations: e.g. at least one beverage selected, drop point count matches capacity, etc.)
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}
