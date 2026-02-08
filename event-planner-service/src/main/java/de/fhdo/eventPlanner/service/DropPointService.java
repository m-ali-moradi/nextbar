package de.fhdo.eventPlanner.service;

import de.fhdo.eventPlanner.dto.request.CreateDropPointRequest;
import de.fhdo.eventPlanner.dto.request.UpdateDropPointRequest;
import de.fhdo.eventPlanner.dto.response.DropPointResponse;
import de.fhdo.eventPlanner.event.DropPointCreatedEvent;
import de.fhdo.eventPlanner.event.EventPublisher;
import de.fhdo.eventPlanner.event.StaffAssignedEvent;
import de.fhdo.eventPlanner.exception.InvalidStateTransitionException;
import de.fhdo.eventPlanner.exception.ResourceNotFoundException;
import de.fhdo.eventPlanner.model.DropPoint;
import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.EventStatus;
import de.fhdo.eventPlanner.repository.DropPointRepository;
import de.fhdo.eventPlanner.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing DropPoints.
 * Handles creation, updates, deletion, and staff assignment.
 */
@Service
public class DropPointService {

    private static final Logger log = LoggerFactory.getLogger(DropPointService.class);

    private final DropPointRepository dropPointRepository;
    private final EventRepository eventRepository;
    private final EventPublisher eventPublisher;

    public DropPointService(DropPointRepository dropPointRepository,
            EventRepository eventRepository,
            EventPublisher eventPublisher) {
        this.dropPointRepository = dropPointRepository;
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Get all drop points.
     */
    @Transactional(readOnly = true)
    public List<DropPointResponse> getAllDropPoints() {
        log.debug("Fetching all drop points");
        return dropPointRepository.findAll().stream()
                .map(DropPointResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get drop points for a specific event.
     */
    @Transactional(readOnly = true)
    public List<DropPointResponse> getDropPointsByEventId(Long eventId) {
        log.debug("Fetching drop points for event ID: {}", eventId);
        return dropPointRepository.findByEventId(eventId).stream()
                .map(DropPointResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get drop point by ID.
     */
    @Transactional(readOnly = true)
    public DropPointResponse getDropPointById(Long id) {
        log.debug("Fetching drop point with ID: {}", id);
        DropPoint dropPoint = findDropPointById(id);
        return DropPointResponse.fromEntity(dropPoint);
    }

    /**
     * Create a new drop point for an event.
     */
    @Transactional
    public DropPointResponse createDropPoint(CreateDropPointRequest request) {
        log.info("Creating new drop point: {} for event ID: {}",
                request.getName(), request.getEventId());

        // Validate event exists and is in valid state
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.getEventId()));

        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only add drop points to events in SCHEDULED status. Current status: " +
                            event.getStatus());
        }

        // Check for duplicate drop point name in event
        if (dropPointRepository.existsByNameAndEventId(request.getName(), request.getEventId())) {
            throw new IllegalArgumentException(
                    "A drop point with name '" + request.getName() + "' already exists for this event");
        }

        // Create drop point
        DropPoint dropPoint = DropPoint.builder()
                .name(request.getName())
                .location(request.getLocation())
                .assignedStaff(request.getAssignedStaff())
                .eventOccupancy(false)
                .event(event)
                .build();

        dropPoint = dropPointRepository.save(dropPoint);
        log.info("Created drop point with ID: {}", dropPoint.getId());

        // Publish DropPointCreatedEvent
        DropPointCreatedEvent createdEvent = DropPointCreatedEvent.from(
                dropPoint.getId(),
                dropPoint.getName(),
                dropPoint.getLocation(),
                event.getId(),
                event.getName());
        eventPublisher.publishDropPointCreated(createdEvent);

        // Publish StaffAssignedEvent if staff is assigned
        if (request.getAssignedStaff() != null && !request.getAssignedStaff().isBlank()) {
            StaffAssignedEvent staffEvent = StaffAssignedEvent.forDropPoint(
                    request.getAssignedStaff(),
                    dropPoint.getId(),
                    dropPoint.getName(),
                    event.getId(),
                    event.getName(),
                    request.getAssignedStaff());
            eventPublisher.publishStaffAssigned(staffEvent);
        }

        return DropPointResponse.fromEntity(dropPoint);
    }

    /**
     * Update an existing drop point.
     */
    @Transactional
    public DropPointResponse updateDropPoint(Long id, UpdateDropPointRequest request) {
        log.info("Updating drop point with ID: {}", id);

        DropPoint dropPoint = findDropPointById(id);

        // Only allow updates for drop points on scheduled events
        if (dropPoint.getEvent().getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only update drop points on events in SCHEDULED status. Current status: " +
                            dropPoint.getEvent().getStatus());
        }

        String previousStaff = dropPoint.getAssignedStaff();

        if (request.getName() != null) {
            dropPoint.setName(request.getName());
        }
        if (request.getLocation() != null) {
            dropPoint.setLocation(request.getLocation());
        }
        if (request.getAssignedStaff() != null) {
            dropPoint.setAssignedStaff(request.getAssignedStaff());
        }

        dropPoint = dropPointRepository.save(dropPoint);
        log.info("Updated drop point with ID: {}", dropPoint.getId());

        // Publish StaffAssignedEvent if staff changed
        if (request.getAssignedStaff() != null &&
                !request.getAssignedStaff().equals(previousStaff)) {
            StaffAssignedEvent staffEvent = StaffAssignedEvent.forDropPoint(
                    request.getAssignedStaff(),
                    dropPoint.getId(),
                    dropPoint.getName(),
                    dropPoint.getEvent().getId(),
                    dropPoint.getEvent().getName(),
                    request.getAssignedStaff());
            eventPublisher.publishStaffAssigned(staffEvent);
        }

        return DropPointResponse.fromEntity(dropPoint);
    }

    /**
     * Delete a drop point.
     */
    @Transactional
    public void deleteDropPoint(Long id) {
        log.info("Deleting drop point with ID: {}", id);

        DropPoint dropPoint = findDropPointById(id);

        // Only allow deletion for drop points on scheduled events
        if (dropPoint.getEvent().getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only delete drop points on events in SCHEDULED status. Current status: " +
                            dropPoint.getEvent().getStatus());
        }

        dropPointRepository.delete(dropPoint);
        log.info("Deleted drop point with ID: {}", id);
    }

    /**
     * Assign staff to a drop point.
     */
    @Transactional
    public DropPointResponse assignStaff(Long dropPointId, String staffIds) {
        log.info("Assigning staff to drop point ID: {}", dropPointId);

        DropPoint dropPoint = findDropPointById(dropPointId);
        dropPoint.setAssignedStaff(staffIds);
        dropPoint = dropPointRepository.save(dropPoint);

        // Publish staff assigned event
        StaffAssignedEvent staffEvent = StaffAssignedEvent.forDropPoint(
                staffIds,
                dropPoint.getId(),
                dropPoint.getName(),
                dropPoint.getEvent().getId(),
                dropPoint.getEvent().getName(),
                staffIds);
        eventPublisher.publishStaffAssigned(staffEvent);

        log.info("Assigned staff to drop point ID: {}", dropPointId);
        return DropPointResponse.fromEntity(dropPoint);
    }

    private DropPoint findDropPointById(Long id) {
        return dropPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DropPoint", id));
    }
}
