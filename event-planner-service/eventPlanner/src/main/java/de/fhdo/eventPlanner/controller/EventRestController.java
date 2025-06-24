
package de.fhdo.eventPlanner.controller;

import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.DefineBeverage;
import de.fhdo.eventPlanner.service.EventPlanningService;
import de.fhdo.eventPlanner.mock.WarehouseCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
        return eventService.saveEvent(event);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @GetMapping("/beverages")
    public List<DefineBeverage> beverages() {
        return warehouseCatalog.getAllBeverages();
    }
}
