package de.fhdo.eventPlanner.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdo.eventPlanner.client.WarehouseServiceClient;
import de.fhdo.eventPlanner.dto.external.WarehouseStockDto;
import de.fhdo.eventPlanner.dto.request.CreateEventRequest;
import de.fhdo.eventPlanner.dto.request.UpdateEventRequest;
import de.fhdo.eventPlanner.dto.response.EventDetailResponse;
import de.fhdo.eventPlanner.dto.response.EventResponse;
import de.fhdo.eventPlanner.event.BarBootstrapEvent;
import de.fhdo.eventPlanner.event.DropPointBootstrapEvent;
import de.fhdo.eventPlanner.event.EventCompletedEvent;
import de.fhdo.eventPlanner.event.EventPublisher;
import de.fhdo.eventPlanner.event.EventStartedEvent;
import de.fhdo.eventPlanner.event.StaffResourceSyncEvent;
import de.fhdo.eventPlanner.event.StockReservedConsumedEvent;
import de.fhdo.eventPlanner.exception.InvalidStateTransitionException;
import de.fhdo.eventPlanner.exception.ResourceNotFoundException;
import de.fhdo.eventPlanner.model.Bar;
import de.fhdo.eventPlanner.model.BarStock;
import de.fhdo.eventPlanner.model.DropPoint;
import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.EventStatus;
import de.fhdo.eventPlanner.repository.AssignedStaffRepository;
import de.fhdo.eventPlanner.repository.BarRepository;
import de.fhdo.eventPlanner.repository.BarStockRepository;
import de.fhdo.eventPlanner.repository.DropPointRepository;
import de.fhdo.eventPlanner.repository.EventRepository;

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
    private final AssignedStaffRepository assignedStaffRepository;
    private final BarStockRepository barStockRepository;
    private final WarehouseServiceClient warehouseServiceClient;
    private final EventPublisher eventPublisher;

    @Autowired
    public EventService(EventRepository eventRepository,
            BarRepository barRepository,
            DropPointRepository dropPointRepository,
            AssignedStaffRepository assignedStaffRepository,
            BarStockRepository barStockRepository,
            WarehouseServiceClient warehouseServiceClient,
            EventPublisher eventPublisher) {
        this.eventRepository = eventRepository;
        this.barRepository = barRepository;
        this.dropPointRepository = dropPointRepository;
        this.assignedStaffRepository = assignedStaffRepository;
        this.barStockRepository = barStockRepository;
        this.warehouseServiceClient = warehouseServiceClient;
        this.eventPublisher = eventPublisher;
    }

    public EventService(EventRepository eventRepository,
            BarRepository barRepository,
            DropPointRepository dropPointRepository,
            BarStockRepository barStockRepository,
            WarehouseServiceClient warehouseServiceClient,
            EventPublisher eventPublisher) {
        this(eventRepository, barRepository, dropPointRepository, null, barStockRepository,
                warehouseServiceClient, eventPublisher);
    }

    public EventService(EventRepository eventRepository,
            BarRepository barRepository,
            DropPointRepository dropPointRepository,
            EventPublisher eventPublisher) {
        this(eventRepository, barRepository, dropPointRepository, null, null, null, eventPublisher);
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
        EventDetailResponse response = EventDetailResponse.fromEntity(event);
        enrichAssignedStaffDisplayNames(response);
        return response;
    }

    /**
     * Create a new event.
     */
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating new event: {}", request.getName());

        Integer attendeesCount = request.getAttendeesCount();
        Boolean isPublic = request.getIsPublic();

        Event event = Event.builder()
                .name(request.getName())
                .date(request.getDate())
                .location(request.getLocation())
                .description(request.getDescription())
                .organizerName(request.getOrganizerName())
                .organizerEmail(request.getOrganizerEmail())
                .organizerPhone(request.getOrganizerPhone())
                .attendeesCount(attendeesCount != null ? attendeesCount.intValue() : 0)
                .maxAttendees(request.getMaxAttendees())
                .isPublic(isPublic == null || isPublic.booleanValue())
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
     * - Publishes BarBootstrapEvent with bar details and stocks to bar service
     * - Publishes DropPointBootstrapEvent with drop point details to drop point
     * service
     * - publish bar and drop point id for selected staff to user-service. so
     * user-service can put the ID to selected user resourceid in service table.
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

        // Validate bars exist
        List<Bar> bars = barRepository.findByEventId(id);
        if (bars.isEmpty()) {
            throw new IllegalArgumentException("Cannot start event: at least one bar is required.");
        }

        // Validate drop points exist
        List<DropPoint> dropPoints = dropPointRepository.findByEventId(id);
        if (dropPoints.isEmpty()) {
            throw new IllegalArgumentException("Cannot start event: at least one drop point is required.");
        }

        // Consume reserved stock for all bars
        consumeReservedStockForEvent(id);

        // Update event status
        event.setStatus(EventStatus.RUNNING);

        // Activate all bars
        List<Long> activatedBarIds = bars.stream()
                .peek(bar -> bar.setEventOccupancy(true))
                .map(Bar::getId)
                .collect(Collectors.toList());
        barRepository.saveAll(bars);

        // Activate all drop points
        List<Long> activatedDropPointIds = dropPoints.stream()
                .peek(dp -> dp.setEventOccupancy(true))
                .map(DropPoint::getId)
                .collect(Collectors.toList());
        dropPointRepository.saveAll(dropPoints);

        // Save event updates
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

        // Publish bar bootstrap events with stock details
        // only publish information that bar service needs to know.
        // barid, name, location, capacity. for stocks only item name and quantity. for
        // event, only event id, name and status.
        List<BarBootstrapEvent.BarBootstrapItem> bootstrapBars = bars.stream()
                .map(bar -> {
                    List<BarStock> stocks = getBarStocksForBar(bar.getId());
                    List<BarBootstrapEvent.BarStockItem> stockItems = stocks.stream()
                            .map(stock -> BarBootstrapEvent.BarStockItem.builder()
                                    .itemName(stock.getItemName())
                                    .quantity(stock.getQuantity())
                                    .build())
                            .collect(Collectors.toList());

                    return BarBootstrapEvent.BarBootstrapItem.builder()
                            .barId(bar.getId())
                            .name(bar.getName())
                            .location(bar.getLocation())
                            .capacity(bar.getCapacity())
                            .stocks(stockItems)
                            .build();
                })
                .collect(Collectors.toList());

        BarBootstrapEvent barBootstrapEvent = BarBootstrapEvent.from(
                event.getId(),
                event.getName(),
                bootstrapBars);
        eventPublisher.publishBarBootstrap(barBootstrapEvent);

        // Publish drop point bootstrap events with details
        // only publish information that dropoint service needs to know.
        // droppointid, name, location, capacity. for event, only event id, name and
        // status.
        List<DropPointBootstrapEvent.DropPointBootstrapItem> bootstrapDropPoints = dropPoints.stream()
                .map(dp -> DropPointBootstrapEvent.DropPointBootstrapItem.builder()
                        .dropPointId(dp.getId())
                        .location(dp.getLocation())
                        .capacity(dp.getCapacity())
                        .build())
                .collect(Collectors.toList());

        DropPointBootstrapEvent bootstrapEvent = DropPointBootstrapEvent.from(
                event.getId(),
                event.getName(),
                bootstrapDropPoints);
        eventPublisher.publishDropPointBootstrap(bootstrapEvent);

        List<StaffResourceSyncEvent.StaffResourceAssignment> startAssignments = buildStaffAssignmentsForStart(bars,
                dropPoints);
        if (!startAssignments.isEmpty()) {
            eventPublisher.publishStaffResourceSync(
                    StaffResourceSyncEvent.assign(event.getId(), event.getName(), startAssignments));
        }

        EventDetailResponse response = EventDetailResponse.fromEntity(event);
        enrichAssignedStaffDisplayNames(response);
        return response;
    }

    // Helper method to enrich assigned staff display names in the event detail
    // response
    private void enrichAssignedStaffDisplayNames(EventDetailResponse response) {
        if (response == null || response.getBars() == null || response.getBars().isEmpty()) {
            return;
        }

        response.getBars().forEach(barResponse -> {
            String assignedStaffUserId = barResponse.getAssignedStaff();
            if (assignedStaffUserId == null || assignedStaffUserId.isBlank()) {
                return;
            }

            String resolvedName = resolveAssignedStaffDisplayName(assignedStaffUserId);
            barResponse.setAssignedStaff(resolvedName);
        });
    }

    // Helper method to resolve assigned staff display name from user ID
    private String resolveAssignedStaffDisplayName(String assignedStaffUserId) {
        if (assignedStaffRepository == null || assignedStaffUserId == null || assignedStaffUserId.isBlank()) {
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

    /**
     * Complete an event.
     * - Updates status to COMPLETED
     * - Sets eventOccupancy = false for all bars and drop points
     * - Publishes EventCompletedEvent
     * - publish event completed with event id to bar and drop point services. so
     * they update status of bars and drop points associated with the event to
     * COMPLETED.
     * - publish assigned staff ids to user-service. so user-service can remove the
     * resourceid of selected user in service table.
     */
    @Transactional
    public EventResponse completeEvent(Long id) {
        log.info("Completing event with ID: {}", id);

        Event event = findEventById(id);

        // Validate current state, must be RUNNING to complete
        if (event.getStatus() != EventStatus.RUNNING) {
            throw new InvalidStateTransitionException(
                    "Can only complete events in RUNNING status. Current status: " + event.getStatus());
        }

        // Update event status
        event.setStatus(EventStatus.COMPLETED);

        // Deactivate all related bars
        List<Bar> bars = barRepository.findByEventId(id);
        bars.forEach(bar -> bar.setEventOccupancy(false));
        barRepository.saveAll(bars);

        // Deactivate all related drop points
        List<DropPoint> dropPoints = dropPointRepository.findByEventId(id);
        dropPoints.forEach(dp -> dp.setEventOccupancy(false));
        dropPointRepository.saveAll(dropPoints);

        // Save event updates
        event = eventRepository.save(event);
        log.info("Event completed: {}", event.getName());

        // Publish event completed notification
        EventCompletedEvent completedEvent = EventCompletedEvent.from(
                event.getId(),
                event.getName(),
                dropPoints.stream().map(DropPoint::getId).collect(Collectors.toList()));

        eventPublisher.publishEventCompleted(completedEvent);

        eventPublisher.publishEventCompletedForResources(completedEvent);

        List<StaffResourceSyncEvent.StaffResourceAssignment> clearAssignments = buildStaffAssignmentsForStart(bars,
                dropPoints);
        if (!clearAssignments.isEmpty()) {
            eventPublisher.publishStaffResourceSync(
                    StaffResourceSyncEvent.clear(event.getId(), event.getName(), clearAssignments));
        }

        return EventResponse.fromEntity(event);
    }

    /**
     * Cancel an event.
     */
    @Transactional
    public EventResponse cancelEvent(Long id) {
        log.info("Cancelling event with ID: {}", id);

        Event event = findEventById(id);
        EventStatus previousStatus = event.getStatus();

        if (previousStatus == EventStatus.COMPLETED) {
            throw new InvalidStateTransitionException("Cannot cancel a completed event.");
        }

        if (previousStatus == EventStatus.SCHEDULED) {
            releaseReservedStockForEvent(id);
        }

        event.setStatus(EventStatus.CANCELLED);

        // Deactivate all bars and drop points if running
        if (previousStatus == EventStatus.RUNNING) {
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

    private void consumeReservedStockForEvent(Long eventId) {
        List<BarStock> barStocks = getBarStocksForEvent(eventId);
        if (barStocks.isEmpty()) {
            return;
        }

        ensureWarehouseClientAvailable();
        for (BarStock barStock : barStocks) {
            Integer quantity = barStock.getQuantity();
            if (quantity != null && quantity > 0) {
                ensureReservedStockForItem(barStock.getItemName(), quantity);
                WarehouseStockDto response;
                try {
                    response = warehouseServiceClient.consumeReservedStock(barStock.getItemName(), quantity);
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Failed to consume reserved stock for item: " + barStock.getItemName() + ". "
                                    + ex.getMessage(),
                            ex);
                }
                if (response == null) {
                    throw new IllegalStateException(
                            "Warehouse service unavailable while consuming stock for item: " + barStock.getItemName());
                }
                eventPublisher.publishStockReservedConsumed(
                        StockReservedConsumedEvent.from(eventId, barStock.getItemName(), quantity));
            }
        }
    }

    private void ensureReservedStockForItem(String itemName, int requiredQuantity) {
        WarehouseStockDto stock;
        boolean stockLookupSupported = true;
        try {
            stock = warehouseServiceClient.getStockByType(itemName);
        } catch (UnsupportedOperationException ex) {
            stockLookupSupported = false;
            stock = null;
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "Failed to check warehouse stock for item: " + itemName + ". " + ex.getMessage(),
                    ex);
        }

        if (!stockLookupSupported) {
            return;
        }

        if (stock == null) {
            throw new IllegalStateException("Warehouse service unavailable while checking stock for item: " + itemName);
        }

        Integer reservedQuantity = stock.getReservedQuantity();
        int reserved = reservedQuantity == null ? 0 : reservedQuantity;
        if (reserved >= requiredQuantity) {
            return;
        }

        int missingReservation = requiredQuantity - reserved;
        WarehouseStockDto reserveResponse;
        try {
            reserveResponse = warehouseServiceClient.reserveStock(itemName, missingReservation);
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "Failed to reserve stock for item: " + itemName + ". " + ex.getMessage(),
                    ex);
        }

        if (reserveResponse == null) {
            throw new IllegalStateException(
                    "Warehouse service unavailable while reserving stock for item: " + itemName);
        }
    }

    private void releaseReservedStockForEvent(Long eventId) {
        List<BarStock> barStocks = getBarStocksForEvent(eventId);
        if (barStocks.isEmpty()) {
            return;
        }

        ensureWarehouseClientAvailable();
        for (BarStock barStock : barStocks) {
            Integer quantity = barStock.getQuantity();
            if (quantity != null && quantity > 0) {
                WarehouseStockDto response = warehouseServiceClient.releaseReservedStock(barStock.getItemName(),
                        quantity);
                if (response == null) {
                    throw new IllegalStateException(
                            "Warehouse service unavailable while releasing stock for item: " + barStock.getItemName());
                }
            }
        }
    }

    private List<BarStock> getBarStocksForEvent(Long eventId) {
        if (barStockRepository == null) {
            return java.util.Collections.emptyList();
        }
        return barStockRepository.findByBar_Event_Id(eventId);
    }

    private List<BarStock> getBarStocksForBar(Long barId) {
        if (barStockRepository == null) {
            return java.util.Collections.emptyList();
        }
        return barStockRepository.findByBarId(barId);
    }

    private List<StaffResourceSyncEvent.StaffResourceAssignment> buildStaffAssignmentsForStart(
            List<Bar> bars,
            List<DropPoint> dropPoints) {
        List<StaffResourceSyncEvent.StaffResourceAssignment> assignments = bars.stream()
                .filter(bar -> bar.getAssignedStaff() != null && !bar.getAssignedStaff().isBlank())
                .map(bar -> StaffResourceSyncEvent.StaffResourceAssignment.builder()
                        .staffId(bar.getAssignedStaff().trim())
                        .serviceCode("BAR_SERVICE")
                        .resourceId(bar.getId())
                        .build())
                .collect(Collectors.toList());

        assignments.addAll(dropPoints.stream()
                .filter(dropPoint -> dropPoint.getAssignedStaff() != null && !dropPoint.getAssignedStaff().isBlank())
                .map(dropPoint -> StaffResourceSyncEvent.StaffResourceAssignment.builder()
                        .staffId(dropPoint.getAssignedStaff().trim())
                        .serviceCode("DROP_POINT_SERVICE")
                        .resourceId(dropPoint.getId())
                        .build())
                .collect(Collectors.toList()));

        return assignments;
    }

    private void ensureWarehouseClientAvailable() {
        if (warehouseServiceClient == null) {
            throw new IllegalStateException("Warehouse client not configured for stock transition operations");
        }
    }
}
