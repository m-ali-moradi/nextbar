package com.nextbar.eventPlanner.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.eventPlanner.dto.request.CreateDropPointRequest;
import com.nextbar.eventPlanner.dto.request.UpdateDropPointRequest;
import com.nextbar.eventPlanner.dto.response.DropPointResponse;
import com.nextbar.eventPlanner.exception.InvalidStateTransitionException;
import com.nextbar.eventPlanner.exception.ResourceNotFoundException;
import com.nextbar.eventPlanner.model.DropPoint;
import com.nextbar.eventPlanner.model.Event;
import com.nextbar.eventPlanner.model.EventStatus;
import com.nextbar.eventPlanner.model.ResourceMode;
import com.nextbar.eventPlanner.repository.DropPointRepository;
import com.nextbar.eventPlanner.repository.EventRepository;

/**
 * Service for managing DropPoints.
 * Handles creation, updates, deletion, and staff assignment.
 */
@Service
public class DropPointService {

    private static final Logger log = LoggerFactory.getLogger(DropPointService.class);

    private final DropPointRepository dropPointRepository;
    private final EventRepository eventRepository;

    public DropPointService(DropPointRepository dropPointRepository,
            EventRepository eventRepository) {
        this.dropPointRepository = dropPointRepository;
        this.eventRepository = eventRepository;
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
        ResourceMode resourceMode = request.getResourceMode() != null ? request.getResourceMode() : ResourceMode.NEW;
        log.info("Creating drop point for event ID: {} (mode={})", request.getEventId(), resourceMode);

        // Validate event exists and is in valid state
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.getEventId()));

        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only add drop points to events in SCHEDULED status. Current status: " +
                            event.getStatus());
        }

        if (resourceMode == ResourceMode.EXISTING) {
            Long existingDropPointId;
            try {
                existingDropPointId = Long.valueOf(request.getExistingResourceId());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Invalid existingResourceId for drop point: " + request.getExistingResourceId());
            }

            DropPoint existingDropPoint = findDropPointById(existingDropPointId);
            Event previousEvent = existingDropPoint.getEvent();
            if (Boolean.TRUE.equals(existingDropPoint.getEventOccupancy())) {
                throw new IllegalArgumentException("Selected existing drop point is currently occupied and cannot be reused");
            }
            if (previousEvent != null
                    && previousEvent.getId() != null
                    && !previousEvent.getId().equals(event.getId())
                    && previousEvent.getStatus() != EventStatus.COMPLETED
                    && previousEvent.getStatus() != EventStatus.CANCELLED) {
                throw new IllegalArgumentException(
                        "Selected existing drop point belongs to an event that is not completed/cancelled");
            }

            existingDropPoint.setEvent(event);
            existingDropPoint.setEventOccupancy(true);
            if (request.getAssignedStaff() != null && !request.getAssignedStaff().isBlank()) {
                existingDropPoint.setAssignedStaff(request.getAssignedStaff());
            }

            DropPoint savedDropPoint = dropPointRepository.save(existingDropPoint);
            log.info("Reused existing drop point with ID: {} for event ID: {}", savedDropPoint.getId(), event.getId());

            return DropPointResponse.fromEntity(savedDropPoint);
        }

        String resolvedName = request.getName();
        Integer requestedCapacity = request.getCapacity();
        int resolvedCapacity = requestedCapacity == null ? 100 : requestedCapacity.intValue();

        // Check for duplicate drop point name in event
        if (dropPointRepository.existsByNameAndEventId(resolvedName, request.getEventId())) {
            throw new IllegalArgumentException(
                "A drop point with name '" + resolvedName + "' already exists for this event");
        }

        // Create drop point
        DropPoint dropPoint = DropPoint.builder()
                .name(resolvedName)
                .location(request.getLocation())
                .assignedStaff(request.getAssignedStaff())
                .capacity(resolvedCapacity)
                .eventOccupancy(false)
                .event(event)
                .build();

        dropPoint = dropPointRepository.save(dropPoint);
        log.info("Created drop point with ID: {}", dropPoint.getId());


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
        if (request.getCapacity() != null) {
            dropPoint.setCapacity(request.getCapacity());
        }

        dropPoint = dropPointRepository.save(dropPoint);
        log.info("Updated drop point with ID: {}", dropPoint.getId());

        // No event publishing from this service (publishing removed)

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

        log.info("Assigned staff to drop point ID: {}", dropPointId);
        return DropPointResponse.fromEntity(dropPoint);
    }

    private DropPoint findDropPointById(Long id) {
        return dropPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DropPoint", id));
    }
}
