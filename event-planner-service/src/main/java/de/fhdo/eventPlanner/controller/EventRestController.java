
package de.fhdo.eventPlanner.controller;

import de.fhdo.eventPlanner.dto.BarPlanForm;
import de.fhdo.eventPlanner.dto.DropPointForm;
import de.fhdo.eventPlanner.exception.ResourceNotFoundException;
import de.fhdo.eventPlanner.mapper.BarPlanMapper;
import de.fhdo.eventPlanner.mapper.DropPointPlanMapper;
import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.DefineBeverage;
import de.fhdo.eventPlanner.model.BarPlan;
import de.fhdo.eventPlanner.service.EventPlanningService;
import de.fhdo.eventPlanner.client.WarehouseCatalogueClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * REST controller for managing events, bars, and drop points.
 * Provides endpoints for CRUD operations and data retrieval.
 */
@RestController
@RequestMapping("/api")
public class EventRestController {

    private final EventPlanningService eventService;
    private final WarehouseCatalogueClient warehouseClient;
    private final BarPlanMapper barPlanMapper;
    private final DropPointPlanMapper dropPointPlanMapper;

    public EventRestController(EventPlanningService eventService,
                              WarehouseCatalogueClient warehouseClient,
                              BarPlanMapper barPlanMapper,
                              DropPointPlanMapper dropPointPlanMapper) {
        this.eventService = eventService;
        this.warehouseClient = warehouseClient;
        this.barPlanMapper = barPlanMapper;
        this.dropPointPlanMapper = dropPointPlanMapper;
    }
    /**
     * Get all events.
     *
     * @return list of all events
     */
    @GetMapping("/events")
    public ResponseEntity<List<Event>> list() {
        return ResponseEntity.ok(eventService.findAllEvents());
    }

    /**
     * Get a specific event by ID.
     *
     * @param id the event ID
     * @return the event with beverages names formatted in bar stock
     */
    @GetMapping("/events/{id}")
    public ResponseEntity<Event> get(@PathVariable Long id) {
        // Load the event (with its beverages and bars)
        Event event = eventService.findEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        // Build a lookup of beverageId -> beverageName
        Map<Long, String> nameById = event.getBeverages().stream()
                .collect(Collectors.toMap(
                        DefineBeverage::getId,
                        DefineBeverage::getName
                ));

        // For each bar, rebuild its stock map so keys become "Name (ID)"
        for (BarPlan bar : event.getBars()) {
            Map<String, Integer> raw = bar.getBeverageStock();
            Map<String, Integer> renamed = new LinkedHashMap<>();
            raw.forEach((idStr, qty) -> {
                Long bevId = Long.valueOf(idStr);
                String bevName = nameById.getOrDefault(bevId, "<?>");
                String label = String.format("%s (%d)", bevName, bevId);
                renamed.put(label, qty);
            });
            bar.setBeverageStock(renamed);
        }
        
        return ResponseEntity.ok(event);
    }

    /**
     * Create a new event.
     *
     * @param event the event to create
     * @return the created event
     */
    @PostMapping("/events")
    public ResponseEntity<Event> create(@Valid @RequestBody Event event) {
        Event createdEvent = eventService.saveEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Update an existing event.
     *
     * @param id the event ID
     * @param event the updated event data
     * @return the updated event
     */
    @PutMapping("/events/{id}")
    public ResponseEntity<Event> update(@PathVariable Long id, @Valid @RequestBody Event event) {
        if (eventService.findEventById(id).isEmpty()) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        event.setEventId(id);
        Event saved = eventService.saveEvent(event);
        // Rename beverageStock for each bar, just like in GET
        Map<Long, String> nameById = saved.getBeverages().stream()
                .collect(Collectors.toMap(
                        DefineBeverage::getId,
                        DefineBeverage::getName
                ));
        for (BarPlan bar : saved.getBars()) {
            Map<String, Integer> raw = bar.getBeverageStock();
            Map<String, Integer> renamed = new LinkedHashMap<>();
            raw.forEach((idStr, qty) -> {
                Long bevId = Long.valueOf(idStr);
                String bevName = nameById.getOrDefault(bevId, "<?>" );
                String label = String.format("%s (%d)", bevName, bevId);
                renamed.put(label, qty);
            });
            bar.setBeverageStock(renamed);
        }
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete an event.
     *
     * @param id the event ID
     * @return no content response
     */
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!eventService.findEventById(id).isPresent()) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all beverages from warehouse.
     *
     * @return list of all beverages
     */
    @GetMapping("/events/beverages")
    public ResponseEntity<List<DefineBeverage>> beverages() {
        return ResponseEntity.ok(warehouseClient.getAllBeverages());
    }

    /**
     * Fetch all drop-point plans from all events.
     *
     * @return list of drop point plans
     */
    @GetMapping("/events/drop-point-plan")
    public ResponseEntity<List<DropPointForm>> fetchDropPoints() {
        List<DropPointForm> dropPoints = eventService.findAllEvents().stream()
                .flatMap(event -> event.getDropPoints().stream())
                .map(dropPointPlanMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dropPoints);
    }

    @GetMapping("/events/bar-plan")
    public List<BarPlanForm> fetchBarPlans() {
        // 1) Build a lookup from beverage ID → beverage name
        //Map<Long, String> nameById = warehouseCatalog.getAllBeverages().stream()
                //.collect(Collectors.toMap(DefineBeverage::getId, DefineBeverage::getName));
        Map<Long, String> nameById = warehouseClient.getAllBeverages().stream()
                .collect(Collectors.toMap(DefineBeverage::getId, DefineBeverage::getName));

        // 2) Stream through every event’s bars, map to BarPlanForm
        return eventService.findAllEvents().stream()
                .flatMap(event -> event.getBars().stream())
                .map(bar -> {
                    BarPlanForm form = new BarPlanForm();
                    form.setBarId(bar.getBarId());
                    form.setBarName(bar.getBarName());
                    form.setLocation(bar.getLocation());
                    form.setTotalCapacity(bar.getTotalCapacity());
                    form.setTotalAssignedDrinkQuantity(bar.getTotalAssignedDrinkQuantity());
                    form.setEventId(bar.getEvent().getEventId());

                    // 3) Turn raw ID→qty map into labelled stock map
                    Map<String,Integer> labeled = new LinkedHashMap<>();
                    bar.getBeverageStock().forEach((idStr, qty) -> {
                        Long bevId = Long.valueOf(idStr);
                        String bevName = nameById.getOrDefault(bevId, "<?>");
                        labeled.put(String.format("%s (%d)", bevName, bevId), qty);
                    });
                    form.setBeverageStock(labeled);

                    return form;
                })
                .collect(Collectors.toList());
    }



}
