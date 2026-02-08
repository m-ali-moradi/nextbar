package de.fhdo.eventPlanner.service;

import de.fhdo.eventPlanner.client.UserServiceClient;
import de.fhdo.eventPlanner.dto.external.UserDto;
import de.fhdo.eventPlanner.dto.request.CreateBarRequest;
import de.fhdo.eventPlanner.dto.request.UpdateBarRequest;
import de.fhdo.eventPlanner.dto.response.BarResponse;
import de.fhdo.eventPlanner.event.BarCreatedEvent;
import de.fhdo.eventPlanner.event.EventPublisher;
import de.fhdo.eventPlanner.event.StaffAssignedEvent;
import de.fhdo.eventPlanner.exception.InvalidStateTransitionException;
import de.fhdo.eventPlanner.exception.ResourceNotFoundException;
import de.fhdo.eventPlanner.model.Bar;
import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.EventStatus;
import de.fhdo.eventPlanner.repository.BarRepository;
import de.fhdo.eventPlanner.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Bars.
 * Handles creation, updates, deletion, and staff assignment.
 */
@Service
public class BarService {

    private static final Logger log = LoggerFactory.getLogger(BarService.class);

    private final BarRepository barRepository;
    private final EventRepository eventRepository;
    private final UserServiceClient userServiceClient;
    private final EventPublisher eventPublisher;

    public BarService(BarRepository barRepository,
            EventRepository eventRepository,
            UserServiceClient userServiceClient,
            EventPublisher eventPublisher) {
        this.barRepository = barRepository;
        this.eventRepository = eventRepository;
        this.userServiceClient = userServiceClient;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Get all bars.
     */
    @Transactional(readOnly = true)
    public List<BarResponse> getAllBars() {
        log.debug("Fetching all bars");
        return barRepository.findAll().stream()
                .map(BarResponse::fromEntitySummary)
                .collect(Collectors.toList());
    }

    /**
     * Get bars for a specific event.
     */
    @Transactional(readOnly = true)
    public List<BarResponse> getBarsByEventId(Long eventId) {
        log.debug("Fetching bars for event ID: {}", eventId);
        return barRepository.findByEventIdWithStocks(eventId).stream()
                .map(BarResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get bar by ID with full details.
     */
    @Transactional(readOnly = true)
    public BarResponse getBarById(Long id) {
        log.debug("Fetching bar with ID: {}", id);
        Bar bar = findBarById(id);
        return BarResponse.fromEntity(bar);
    }

    /**
     * Create a new bar for an event.
     * Staff assignment is required and triggers a RabbitMQ event.
     */
    @Transactional
    public BarResponse createBar(CreateBarRequest request) {
        log.info("Creating new bar: {} for event ID: {}", request.getName(), request.getEventId());

        // Validate event exists and is in valid state
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.getEventId()));

        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only add bars to events in SCHEDULED status. Current status: " + event.getStatus());
        }

        // Check for duplicate bar name in event
        if (barRepository.existsByNameAndEventId(request.getName(), request.getEventId())) {
            throw new IllegalArgumentException(
                    "A bar with name '" + request.getName() + "' already exists for this event");
        }

        // Create bar
        Bar bar = Bar.builder()
                .name(request.getName())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .assignedStaff(request.getAssignedStaff())
                .eventOccupancy(false)
                .event(event)
                .build();

        bar = barRepository.save(bar);
        log.info("Created bar with ID: {}", bar.getId());

        // Publish BarCreatedEvent
        BarCreatedEvent barCreatedEvent = BarCreatedEvent.from(
                bar.getId(),
                bar.getName(),
                bar.getLocation(),
                bar.getCapacity(),
                event.getId(),
                event.getName());
        eventPublisher.publishBarCreated(barCreatedEvent);

        // Publish StaffAssignedEvent if staff is assigned
        if (request.getAssignedStaff() != null && !request.getAssignedStaff().isBlank()) {
            StaffAssignedEvent staffEvent = StaffAssignedEvent.forBar(
                    request.getAssignedStaff(),
                    bar.getId(),
                    bar.getName(),
                    event.getId(),
                    event.getName(),
                    request.getAssignedStaff());
            eventPublisher.publishStaffAssigned(staffEvent);
        }

        return BarResponse.fromEntity(bar);
    }

    /**
     * Update an existing bar.
     */
    @Transactional
    public BarResponse updateBar(Long id, UpdateBarRequest request) {
        log.info("Updating bar with ID: {}", id);

        Bar bar = findBarById(id);

        // Only allow updates for bars on scheduled events
        if (bar.getEvent().getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only update bars on events in SCHEDULED status. Current status: " +
                            bar.getEvent().getStatus());
        }

        String previousStaff = bar.getAssignedStaff();

        if (request.getName() != null) {
            bar.setName(request.getName());
        }
        if (request.getLocation() != null) {
            bar.setLocation(request.getLocation());
        }
        if (request.getCapacity() != null) {
            bar.setCapacity(request.getCapacity());
        }
        if (request.getAssignedStaff() != null) {
            bar.setAssignedStaff(request.getAssignedStaff());
        }

        bar = barRepository.save(bar);
        log.info("Updated bar with ID: {}", bar.getId());

        // Publish StaffAssignedEvent if staff changed
        if (request.getAssignedStaff() != null &&
                !request.getAssignedStaff().equals(previousStaff)) {
            StaffAssignedEvent staffEvent = StaffAssignedEvent.forBar(
                    request.getAssignedStaff(),
                    bar.getId(),
                    bar.getName(),
                    bar.getEvent().getId(),
                    bar.getEvent().getName(),
                    request.getAssignedStaff());
            eventPublisher.publishStaffAssigned(staffEvent);
        }

        return BarResponse.fromEntity(bar);
    }

    /**
     * Delete a bar.
     */
    @Transactional
    public void deleteBar(Long id) {
        log.info("Deleting bar with ID: {}", id);

        Bar bar = findBarById(id);

        // Only allow deletion for bars on scheduled events
        if (bar.getEvent().getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only delete bars on events in SCHEDULED status. Current status: " +
                            bar.getEvent().getStatus());
        }

        barRepository.delete(bar);
        log.info("Deleted bar with ID: {}", id);
    }

    /**
     * Assign staff to a bar (for use when staff changes after creation).
     */
    @Transactional
    public BarResponse assignStaff(Long barId, String staffIds) {
        log.info("Assigning staff to bar ID: {}", barId);

        Bar bar = findBarById(barId);
        bar.setAssignedStaff(staffIds);
        bar = barRepository.save(bar);

        // Publish staff assigned event
        StaffAssignedEvent staffEvent = StaffAssignedEvent.forBar(
                staffIds,
                bar.getId(),
                bar.getName(),
                bar.getEvent().getId(),
                bar.getEvent().getName(),
                staffIds);
        eventPublisher.publishStaffAssigned(staffEvent);

        log.info("Assigned staff to bar ID: {}", barId);
        return BarResponse.fromEntity(bar);
    }

    /**
     * Get available staff members from UserService.
     */
    public List<UserDto> getAvailableStaff() {
        log.debug("Fetching available staff from UserService");
        return userServiceClient.getAllUsers();
    }

    private Bar findBarById(Long id) {
        return barRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bar", id));
    }
}
