
package de.fhdo.eventPlanner.controller;

import de.fhdo.eventPlanner.dto.BarPlanForm;
import de.fhdo.eventPlanner.dto.DropPointForm;
import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.DefineBeverage;
import de.fhdo.eventPlanner.model.BarPlan;
import de.fhdo.eventPlanner.model.DropPointPlan;
import de.fhdo.eventPlanner.service.EventPlanningService;
import de.fhdo.eventPlanner.mock.WarehouseCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class EventRestController {

    private final EventPlanningService eventService;
    private final WarehouseCatalog warehouseCatalog;

    @Autowired
    public EventRestController(EventPlanningService eventService,
                               WarehouseCatalog warehouseCatalog) {
        this.eventService = eventService;
        this.warehouseCatalog = warehouseCatalog;
    }

    @GetMapping("/events")
    public List<Event> list() {
        return eventService.findAllEvents();
    }

    @GetMapping("/events/{id}")
    public Event get(@PathVariable Long id) {

        //Change code
        // 1) Load the event (with its beverages and bars)
        Event event = eventService.findEventById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        // 2) Build a lookup of beverageId -> beverageName
        Map<Long, String> nameById = event.getBeverages().stream()
                .collect(Collectors.toMap(
                        DefineBeverage::getId,
                        DefineBeverage::getName
                ));

        // 3) For each bar, rebuild its stock map so keys become "Name (ID)"
        for (BarPlan bar : event.getBars()) {
            Map<String, Integer> raw = bar.getBeverageStock();
            Map<String, Integer> renamed = new LinkedHashMap<>();
            raw.forEach((idStr, qty) -> {
                Long bevId = Long.valueOf(idStr);
                String bevName = nameById.getOrDefault(bevId, "<?>");
                // you can choose just bevName, or include the ID too:
                String label = String.format("%s (%d)", bevName, bevId);
                renamed.put(label, qty);
            });
            bar.setBeverageStock(renamed);
        }
        //End of change code
        return eventService.findEventById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@RequestBody Event event) {
        return eventService.saveEvent(event);
    }

    @PutMapping("/events/{id}")
    public Event update(@PathVariable Long id, @RequestBody Event event) {
        if (eventService.findEventById(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found");
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
        return saved;
        //return eventService.saveEvent(event);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @GetMapping("/events/beverages")
    public List<DefineBeverage> beverages() {
        return warehouseCatalog.getAllBeverages();
    }

    // New endpoint to fetch all drop-point plans without requiring an event ID
    @GetMapping("/events/drop-point-plan")
    public List<DropPointForm> fetchDropPoints() {
        return eventService.findAllEvents().stream()
                .flatMap(event -> event.getDropPoints().stream())
                .map(dp -> {
                    DropPointForm form = new DropPointForm();
                    form.setDropPointId(dp.getDropPointId());
                    form.setLocation(dp.getLocation());
                    form.setCapacity(dp.getCapacity());
                    form.setEventId(dp.getEvent().getEventId());
                    return form;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/events/bar-plan")
    public List<BarPlanForm> fetchBarPlans() {
        // 1) Build a lookup from beverage ID → beverage name
        Map<Long, String> nameById = warehouseCatalog.getAllBeverages().stream()
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
                    form.setEventId(bar.getEvent().getEventId());                // ← need this in DTO

                    // 3) Turn raw ID→qty map into labelled stock map
                    Map<String,Integer> labeled = new LinkedHashMap<>();
                    bar.getBeverageStock().forEach((idStr, qty) -> {
                        Long bevId = Long.valueOf(idStr);
                        String bevName = nameById.getOrDefault(bevId, "<?>");
                        labeled.put(String.format("%s (%d)", bevName, bevId), qty);
                    });
                    form.setBeverageStock(labeled);                              // ← and this

                    return form;
                })
                .collect(Collectors.toList());
    }



}
