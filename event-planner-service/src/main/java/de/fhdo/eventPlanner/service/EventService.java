package de.fhdo.eventPlanner.service;

import de.fhdo.eventPlanner.dto.request.CreateEventRequest;
import de.fhdo.eventPlanner.dto.request.UpdateEventRequest;
import de.fhdo.eventPlanner.dto.response.EventDetailResponse;
import de.fhdo.eventPlanner.dto.response.EventResponse;
import de.fhdo.eventPlanner.event.EventPublisher;
import de.fhdo.eventPlanner.event.EventStartedEvent;
import de.fhdo.eventPlanner.exception.InvalidStateTransitionException;
import de.fhdo.eventPlanner.exception.ResourceNotFoundException;
import de.fhdo.eventPlanner.model.Bar;
import de.fhdo.eventPlanner.model.DropPoint;
import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.EventStatus;
import de.fhdo.eventPlanner.repository.BarRepository;
import de.fhdo.eventPlanner.repository.DropPointRepository;
import de.fhdo.eventPlanner.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Events.
 * Handles creation, updates, deletion, and lifecycle operations.
 */
@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final BarRepository barRepository;
    private final DropPointRepository dropPointRepository;
    private final EventPublisher eventPublisher;

    public EventService(EventRepository eventRepository,
            BarRepository barRepository,
            DropPointRepository dropPointRepository,
            EventPublisher eventPublisher) {
        this.eventRepository = eventRepository;
        this.barRepository = barRepository;
        this.dropPointRepository = dropPointRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Get all events.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        log.debug("Fetching all events");
        return eventRepository.findAll().stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get events by status.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByStatus(EventStatus status) {
        log.debug("Fetching events with status: {}", status);
        return eventRepository.findByStatus(status).stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get event by ID (summary).
     */
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        log.debug("Fetching event with ID: {}", id);
        Event event = findEventById(id);
        return EventResponse.fromEntity(event);
    }

    /**
     * Get event by ID with full details (including bars and drop points).
     */
    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetailById(Long id) {
        log.debug("Fetching event details for ID: {}", id);
        Event event = findEventById(id);
        return EventDetailResponse.fromEntity(event);
    }

    /**
     * Create a new event.
     */
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating new event: {}", request.getName());

        Event event = Event.builder()
                .name(request.getName())
                .date(request.getDate())
                .location(request.getLocation())
                .description(request.getDescription())
                .organizerName(request.getOrganizerName())
                .organizerEmail(request.getOrganizerEmail())
                .organizerPhone(request.getOrganizerPhone())
                .attendeesCount(request.getAttendeesCount() != null ? request.getAttendeesCount() : 0)
                .maxAttendees(request.getMaxAttendees())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .status(EventStatus.SCHEDULED)
                .build();

        event = eventRepository.save(event);
        log.info("Created event with ID: {}", event.getId());

        return EventResponse.fromEntity(event);
    }

    /**
     * Update an existing event.
     */
    @Transactional
    public EventResponse updateEvent(Long id, UpdateEventRequest request) {
        log.info("Updating event with ID: {}", id);

        Event event = findEventById(id);

        // Only allow updates for scheduled events
        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Cannot update event that is not in SCHEDULED status. Current status: " + event.getStatus());
        }

        if (request.getName() != null) {
            event.setName(request.getName());
        }
        if (request.getDate() != null) {
            event.setDate(request.getDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getOrganizerName() != null) {
            event.setOrganizerName(request.getOrganizerName());
        }
        if (request.getOrganizerEmail() != null) {
            event.setOrganizerEmail(request.getOrganizerEmail());
        }
        if (request.getOrganizerPhone() != null) {
            event.setOrganizerPhone(request.getOrganizerPhone());
        }
        if (request.getAttendeesCount() != null) {
            event.setAttendeesCount(request.getAttendeesCount());
        }
        if (request.getMaxAttendees() != null) {
            event.setMaxAttendees(request.getMaxAttendees());
        }
        if (request.getIsPublic() != null) {
            event.setIsPublic(request.getIsPublic());
        }

        event = eventRepository.save(event);
        log.info("Updated event with ID: {}", event.getId());

        return EventResponse.fromEntity(event);
    }

    /**
     * Delete an event.
     */
    @Transactional
    public void deleteEvent(Long id) {
        log.info("Deleting event with ID: {}", id);

        Event event = findEventById(id);

        // Only allow deletion of scheduled or cancelled events
        if (event.getStatus() == EventStatus.RUNNING) {
            throw new InvalidStateTransitionException(
                    "Cannot delete a running event. Please cancel or complete it first.");
        }

        eventRepository.delete(event);
        log.info("Deleted event with ID: {}", id);
    }

    /**
     * Start an event.
     * - Updates status to RUNNING
     * - Sets eventOccupancy = true for all bars and drop points
     * - Publishes EventStartedEvent
     */
    @Transactional
    public EventDetailResponse startEvent(Long id) {
        log.info("Starting event with ID: {}", id);

        Event event = findEventById(id);

        // Validate current state
        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only start events in SCHEDULED status. Current status: " + event.getStatus());
        }

        // Update event status
        event.setStatus(EventStatus.RUNNING);

        // Activate all bars
        List<Bar> bars = barRepository.findByEventId(id);
        List<Long> activatedBarIds = bars.stream()
                .peek(bar -> bar.setEventOccupancy(true))
                .map(Bar::getId)
                .collect(Collectors.toList());
        barRepository.saveAll(bars);

        // Activate all drop points
        List<DropPoint> dropPoints = dropPointRepository.findByEventId(id);
        List<Long> activatedDropPointIds = dropPoints.stream()
                .peek(dp -> dp.setEventOccupancy(true))
                .map(DropPoint::getId)
                .collect(Collectors.toList());
        dropPointRepository.saveAll(dropPoints);

        event = eventRepository.save(event);
        log.info("Event started: {} - Activated {} bars and {} drop points",
                event.getName(), activatedBarIds.size(), activatedDropPointIds.size());

        // Publish event started notification
        EventStartedEvent eventStarted = EventStartedEvent.from(
                event.getId(),
                event.getName(),
                event.getLocation(),
                activatedBarIds,
                activatedDropPointIds);
        eventPublisher.publishEventStarted(eventStarted);

        return EventDetailResponse.fromEntity(event);
    }

    /**
     * Complete an event.
     */
    @Transactional
    public EventResponse completeEvent(Long id) {
        log.info("Completing event with ID: {}", id);

        Event event = findEventById(id);

        if (event.getStatus() != EventStatus.RUNNING) {
            throw new InvalidStateTransitionException(
                    "Can only complete events in RUNNING status. Current status: " + event.getStatus());
        }

        event.setStatus(EventStatus.COMPLETED);

        // Deactivate all bars and drop points
        List<Bar> bars = barRepository.findByEventId(id);
        bars.forEach(bar -> bar.setEventOccupancy(false));
        barRepository.saveAll(bars);

        List<DropPoint> dropPoints = dropPointRepository.findByEventId(id);
        dropPoints.forEach(dp -> dp.setEventOccupancy(false));
        dropPointRepository.saveAll(dropPoints);

        event = eventRepository.save(event);
        log.info("Event completed: {}", event.getName());

        return EventResponse.fromEntity(event);
    }

    /**
     * Cancel an event.
     */
    @Transactional
    public EventResponse cancelEvent(Long id) {
        log.info("Cancelling event with ID: {}", id);

        Event event = findEventById(id);

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new InvalidStateTransitionException("Cannot cancel a completed event.");
        }

        event.setStatus(EventStatus.CANCELLED);

        // Deactivate all bars and drop points if running
        if (event.getStatus() == EventStatus.RUNNING) {
            List<Bar> bars = barRepository.findByEventId(id);
            bars.forEach(bar -> bar.setEventOccupancy(false));
            barRepository.saveAll(bars);

            List<DropPoint> dropPoints = dropPointRepository.findByEventId(id);
            dropPoints.forEach(dp -> dp.setEventOccupancy(false));
            dropPointRepository.saveAll(dropPoints);
        }

        event = eventRepository.save(event);
        log.info("Event cancelled: {}", event.getName());

        return EventResponse.fromEntity(event);
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }
}
