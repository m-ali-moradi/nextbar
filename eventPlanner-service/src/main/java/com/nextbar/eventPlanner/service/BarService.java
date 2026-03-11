package com.nextbar.eventPlanner.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.eventPlanner.client.UserServiceClient;
import com.nextbar.eventPlanner.dto.external.AssignRoleRequest;
import com.nextbar.eventPlanner.dto.external.UserDto;
import com.nextbar.eventPlanner.dto.request.CreateBarRequest;
import com.nextbar.eventPlanner.dto.request.UpdateBarRequest;
import com.nextbar.eventPlanner.dto.response.BarResponse;
import com.nextbar.eventPlanner.exception.InvalidStateTransitionException;
import com.nextbar.eventPlanner.exception.ResourceNotFoundException;
import com.nextbar.eventPlanner.model.AssignedStaff;
import com.nextbar.eventPlanner.model.Bar;
import com.nextbar.eventPlanner.model.Event;
import com.nextbar.eventPlanner.model.EventStatus;
import com.nextbar.eventPlanner.model.ResourceMode;
import com.nextbar.eventPlanner.repository.AssignedStaffRepository;
import com.nextbar.eventPlanner.repository.BarRepository;
import com.nextbar.eventPlanner.repository.EventRepository;

/**
 * Service for managing Bars.
 * Handles creation, updates, deletion, and staff assignment.
 */
@Service
public class BarService {

    private static final Logger log = LoggerFactory.getLogger(BarService.class);

    private static final String BAR_SERVICE_CODE = "BAR_SERVICE";
    private static final String BAR_OPERATOR_ROLE = "BARTENDER";

    private final BarRepository barRepository;
    private final EventRepository eventRepository;
    private final AssignedStaffRepository assignedStaffRepository;
    private final UserServiceClient userServiceClient;
    private final DropPointService dropPointService;
    public BarService(BarRepository barRepository,
            EventRepository eventRepository,
            AssignedStaffRepository assignedStaffRepository,
            UserServiceClient userServiceClient,
            DropPointService dropPointService) {
        this.barRepository = barRepository;
        this.eventRepository = eventRepository;
        this.assignedStaffRepository = assignedStaffRepository;
        this.userServiceClient = userServiceClient;
        this.dropPointService = dropPointService;
    }

    /**
     * Get all bars.
     * fetch assigned staff firstname and last from assigned_staff table and set it on the response as assignedStaff.
     */
    @Transactional(readOnly = true)
    public List<BarResponse> getAllBars() {
        log.debug("Fetching all bars");
        return barRepository.findAll().stream()
                .map(this::toSummaryResponseWithAssignedStaffDisplayName)
                .collect(Collectors.toList());
    }

    /**
     * Get bars for a specific event.
     * fetch assigned staff firstname and last from assigned_staff table and set it on the response as assignedStaff.
     */
    @Transactional(readOnly = true)
    public List<BarResponse> getBarsByEventId(Long eventId) {
        log.debug("Fetching bars for event ID: {}", eventId);
        return barRepository.findByEventIdWithStocks(eventId).stream()
                .map(this::toResponseWithAssignedStaffDisplayName)
                .collect(Collectors.toList());
    }

    /**
     * Get bar by ID with full details.
     * 
     */
    @Transactional(readOnly = true)
    public BarResponse getBarById(Long id) {
        log.debug("Fetching bar with ID: {}", id);
        Bar bar = findBarById(id);
        return toResponseWithAssignedStaffDisplayName(bar);
    }

    /**
     * Create a new bar for an event.
     */
    @Transactional
    public BarResponse createBar(CreateBarRequest request) {
        ResourceMode resourceMode = request.getResourceMode() != null ? request.getResourceMode() : ResourceMode.NEW;
        log.info("Creating bar for event ID: {} (mode={})", request.getEventId(), resourceMode);

        // Validate event exists and is in valid state
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.getEventId()));

        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only add bars to events in SCHEDULED status. Current status: " + event.getStatus());
        }

        if (resourceMode == ResourceMode.EXISTING) {
            List<String> assignedStaffIds = normalizeStaffIds(request.getAssignedStaff());
            Long existingBarId;
            try {
                existingBarId = Long.valueOf(request.getExistingResourceId());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Invalid existingResourceId for bar: " + request.getExistingResourceId());
            }

            Bar existingBar = findBarById(existingBarId);
            Event previousEvent = existingBar.getEvent();
            if (Boolean.TRUE.equals(existingBar.getEventOccupancy())) {
                throw new IllegalArgumentException("Selected existing bar is currently occupied and cannot be reused");
            }
            if (previousEvent != null
                    && previousEvent.getId() != null
                    && !previousEvent.getId().equals(event.getId())
                    && previousEvent.getStatus() != EventStatus.COMPLETED
                    && previousEvent.getStatus() != EventStatus.CANCELLED) {
                throw new IllegalArgumentException(
                        "Selected existing bar belongs to an event that is not completed/cancelled");
            }

            existingBar.setEvent(event);
            existingBar.setEventOccupancy(true);
            if (!assignedStaffIds.isEmpty()) {
                existingBar.setAssignedStaff(assignedStaffIds.get(0));
            }

            Bar savedBar = barRepository.save(existingBar);
            log.info("Reused existing bar with ID: {} for event ID: {}", savedBar.getId(), event.getId());

            if (!assignedStaffIds.isEmpty()) {
                upsertAssignedStaffRecords(assignedStaffIds);
                // Assign bar-operator role with resourceId to each staff member
                assignBarRolesToStaff(savedBar.getId(), assignedStaffIds);
            }

            return toResponseWithAssignedStaffDisplayName(savedBar);
        }

        String resolvedName = request.getName();

        // Check for duplicate bar name in event
        if (barRepository.existsByNameAndEventId(resolvedName, request.getEventId())) {
            throw new IllegalArgumentException(
                    "A bar with name '" + resolvedName + "' already exists for this event");
        }

        List<String> assignedStaffIds = normalizeStaffIds(request.getAssignedStaff());

        // Create bar
        Bar bar = Bar.builder()
                .name(resolvedName)
                .location(request.getLocation())
                .capacity(request.getCapacity())
            .assignedStaff(assignedStaffIds.isEmpty() ? null : assignedStaffIds.get(0))
                .eventOccupancy(false)
                .event(event)
                .build();

        bar = barRepository.save(bar);
        log.info("Created bar with ID: {}", bar.getId());

        if (!assignedStaffIds.isEmpty()) {
            upsertAssignedStaffRecords(assignedStaffIds);
            assignBarRolesToStaff(bar.getId(), assignedStaffIds);
        }

        return toResponseWithAssignedStaffDisplayName(bar);
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
        List<String> assignedStaffIds = request.getAssignedStaff() == null
                ? null
                : normalizeStaffIds(request.getAssignedStaff());

        if (assignedStaffIds != null) {
            bar.setAssignedStaff(assignedStaffIds.isEmpty() ? null : assignedStaffIds.get(0));
        }

        bar = barRepository.save(bar);
        log.info("Updated bar with ID: {}", bar.getId());

        // Assign bar-operator role with resourceId to new staff if provided
        if (assignedStaffIds != null) {
            String newPrimaryStaff = assignedStaffIds.isEmpty() ? null : assignedStaffIds.get(0);
            if (!Objects.equals(newPrimaryStaff, previousStaff)) {
                if (!assignedStaffIds.isEmpty()) {
                    upsertAssignedStaffRecords(assignedStaffIds);
                    assignBarRolesToStaff(bar.getId(), assignedStaffIds);
                }
            }
        }

        return toResponseWithAssignedStaffDisplayName(bar);
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
        List<String> normalizedStaffIds = normalizeCommaSeparatedStaffIds(staffIds);
        bar.setAssignedStaff(normalizedStaffIds.isEmpty() ? null : normalizedStaffIds.get(0));
        bar = barRepository.save(bar);

        if (!normalizedStaffIds.isEmpty()) {
            upsertAssignedStaffRecords(normalizedStaffIds);
            // Assign bar-operator role with resourceId to staff
            assignBarRolesToStaff(bar.getId(), normalizedStaffIds);
        }

        log.info("Assigned staff to bar ID: {}", barId);
        return toResponseWithAssignedStaffDisplayName(bar);
    }

    /**
     * Get available staff members from UserService.
     * filter only non-admin and users with service BAR_SERVICE.
     */
    public List<UserDto> getAvailableStaff() {
        log.debug("Fetching available staff from UserService");
        return userServiceClient.getAllUsers().stream()
            .filter(user -> {
                var assignments = user.getAssignments();
                if (assignments == null || assignments.isEmpty()) {
                return false;
                }
                boolean isAdmin = assignments.stream()
                    .anyMatch(a -> a.role() != null && a.role().equalsIgnoreCase("ADMIN"));
                boolean hasBarService = assignments.stream()
                    .anyMatch(a -> a.service() != null && a.service().equals(BAR_SERVICE_CODE));
                return !isAdmin && hasBarService;
            })
            .collect(Collectors.toList());
    }

    private Bar findBarById(Long id) {
        return barRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bar", id));
    }

    /**
     * Generate a stable UUID from a long source ID.
     * Uses the same algorithm as the bar-service's EventPlannerBarEventListener
     * to ensure consistent UUID mapping between services.
     */
    private UUID toStableUuid(Long sourceId) {
        return UUID.nameUUIDFromBytes(("int-" + sourceId).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Assign bar-operator role to each staff member for the given bar.
     * The bar's Long ID is converted to a stable UUID (matching the bar-service's
     * internal ID)
     * and set as the resourceId on the user's role assignment.
     *
     * @param barId    The event-planner bar ID (Long)
     * @param staffIds List of staff UUIDs
     */
    private void assignBarRolesToStaff(Long barId, List<String> staffIds) {
        if (staffIds == null || staffIds.isEmpty()) {
            return;
        }

        UUID barUuid = toStableUuid(barId);
        for (String trimmedId : staffIds) {

            try {
                UUID userId = UUID.fromString(trimmedId);
                AssignRoleRequest request = AssignRoleRequest.builder()
                        .userId(userId)
                        .roleName(BAR_OPERATOR_ROLE)
                        .serviceCode(BAR_SERVICE_CODE)
                        .resourceId(barUuid)
                        .build();

                userServiceClient.assignRoleToUser(request);
                log.info("Assigned bar-operator role to user {} for bar {} (resourceId={})",
                        userId, barId, barUuid);
            } catch (IllegalArgumentException ex) {
                log.warn("Skipping invalid staff ID '{}': {}", trimmedId, ex.getMessage());
            } catch (Exception ex) {
                log.error("Failed to assign bar-operator role to user '{}' for bar {}: {}",
                        trimmedId, barId, ex.getMessage());
            }
        }
    }

    private List<String> normalizeStaffIds(List<String> staffIds) {
        if (staffIds == null) {
            return List.of();
        }

        return staffIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .distinct()
                .toList();
    }

    private List<String> normalizeCommaSeparatedStaffIds(String staffIds) {
        if (staffIds == null || staffIds.isBlank()) {
            return List.of();
        }

        List<String> parsed = new ArrayList<>();
        for (String rawId : staffIds.split(",")) {
            if (rawId == null) {
                continue;
            }
            String trimmed = rawId.trim();
            if (!trimmed.isEmpty()) {
                parsed.add(trimmed);
            }
        }
        return normalizeStaffIds(parsed);
    }

    private void upsertAssignedStaffRecords(List<String> staffIds) {
        for (String staffId : staffIds) {
            try {
                UUID userId = UUID.fromString(staffId);
                UserDto user = userServiceClient.getUserById(userId);
                if (user == null) {
                    log.warn("Cannot upsert assigned staff for user {} because users-service returned null", staffId);
                    continue;
                }

                AssignedStaff assignedStaff = assignedStaffRepository.findByUserId(staffId)
                        .orElseGet(AssignedStaff::new);

                assignedStaff.setUserId(staffId);
                assignedStaff.setUsername(defaultIfBlank(user.getUsername(), staffId));
                assignedStaff.setFirstName(defaultIfBlank(user.getFirstName(), "Unknown"));
                assignedStaff.setLastName(defaultIfBlank(user.getLastName(), "Unknown"));
                assignedStaff.setEmail(user.getEmail());

                assignedStaffRepository.save(assignedStaff);
            } catch (IllegalArgumentException ex) {
                log.warn("Skipping invalid assigned staff id '{}' while upserting assigned_staff table", staffId);
            } catch (Exception ex) {
                log.error("Failed to upsert assigned_staff row for user {}: {}", staffId, ex.getMessage());
            }
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private BarResponse toResponseWithAssignedStaffDisplayName(Bar bar) {
        BarResponse response = BarResponse.fromEntity(bar);
        response.setAssignedStaff(resolveAssignedStaffDisplayName(bar.getAssignedStaff()));
        return response;
    }

    private BarResponse toSummaryResponseWithAssignedStaffDisplayName(Bar bar) {
        BarResponse response = BarResponse.fromEntitySummary(bar);
        response.setAssignedStaff(resolveAssignedStaffDisplayName(bar.getAssignedStaff()));
        return response;
    }

    private String resolveAssignedStaffDisplayName(String assignedStaffUserId) {
        if (assignedStaffUserId == null || assignedStaffUserId.isBlank()) {
            return assignedStaffUserId;
        }

        return assignedStaffRepository.findByUserId(assignedStaffUserId)
                .map(assigned -> {
                    String firstName = assigned.getFirstName() == null ? "" : assigned.getFirstName().trim();
                    String lastName = assigned.getLastName() == null ? "" : assigned.getLastName().trim();
                    String fullName = (firstName + " " + lastName).trim();
                    if (!fullName.isEmpty()) {
                        return fullName;
                    }
                    if (assigned.getUsername() != null && !assigned.getUsername().isBlank()) {
                        return assigned.getUsername();
                    }
                    return assignedStaffUserId;
                })
                .orElse(assignedStaffUserId);
    }
}
