package de.fhdo.eventPlanner.controller;

import de.fhdo.eventPlanner.model.*;
import de.fhdo.eventPlanner.mock.WarehouseCatalog;
import de.fhdo.eventPlanner.service.EventPlanningService;

import de.fhdo.eventPlanner.dto.EventForm;
import de.fhdo.eventPlanner.dto.BarPlanForm;
import de.fhdo.eventPlanner.dto.DropPointForm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventPlanningService eventService;
    private final WarehouseCatalog warehouseCatalog;

    @Autowired
    public EventController(EventPlanningService eventService,
                           WarehouseCatalog warehouseCatalog) {
        this.eventService = eventService;
        this.warehouseCatalog = warehouseCatalog;
    }

    /**
     * Landing page: list all events.
     */
    @GetMapping
    public String listEvents(Model model) {
        List<Event> all = eventService.findAllEvents();
        model.addAttribute("events", all);
        return "event_list";
    }

    /**
     * “Create New Event” form: page with a blank EventForm.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        EventForm form = new EventForm();
        form.setStatus(Status.PLANNED);

        // 1) Initialize bars list (so Thymeleaf can iterate over form.getBars())
        List<BarPlanForm> bars = new ArrayList<>();
        // Create a “blank” BarPlanForm whose beverageStock is a non-null map:
        BarPlanForm blankBar = new BarPlanForm();
        blankBar.setBeverageStock(new HashMap<>());
        bars.add(blankBar);
        form.setBars(bars);

        // 2) Initialize drop points list
        List<DropPointForm> drops = new ArrayList<>();
        DropPointForm blankDrop = new DropPointForm();
        drops.add(blankDrop);
        form.setDropPoints(drops);

        // 3) Expose to Thymeleaf
        model.addAttribute("allBeverages", warehouseCatalog.getAllBeverages());
        model.addAttribute("eventForm", form);
        return "event_form";
    }
    /**
     * “Edit Existing Event” form: load event by ID, populate EventForm.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model,
                               RedirectAttributes redirectAttrs) {
        Optional<Event> opt = eventService.findEventById(id);
        if (!opt.isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Event ID " + id + " not found");
            return "redirect:/events";
        }
        Event existing = opt.get();

        // Build an EventForm from existing Event:
        EventForm form = new EventForm();
        form.setEventId(existing.getEventId());
        form.setName(existing.getName());
        form.setDate(existing.getDate());
        form.setLocation(existing.getLocation());
        form.setDuration(existing.getDuration());
        form.setStatus(existing.getStatus());
        form.setBeverages(existing.getBeverages());

        // For bars & drop points, copy them into Form objects
        for (BarPlan bar : existing.getBars()) {
            BarPlanForm bf = new BarPlanForm();
            bf.setBarId(bar.getBarId());
            bf.setBarName(bar.getBarName());
            bf.setLocation(bar.getLocation());
            bf.setTotalCapacity(bar.getTotalCapacity());
            bf.setBeverageStock(new HashMap<>(bar.getBeverageStock()));
            form.getBars().add(bf);
        }
        for (DropPointPlan dp : existing.getDropPoints()) {
            DropPointForm dpf = new DropPointForm();
            dpf.setDropPointId(dp.getDropPointId());
            dpf.setLocation(dp.getLocation());
            dpf.setCapacity(dp.getCapacity());
            form.getDropPoints().add(dpf);
        }

        model.addAttribute("allBeverages", warehouseCatalog.getAllBeverages());
        model.addAttribute("eventForm", form);
        return "event_form";
    }

    /**
     * Handle form submission (both Create & Update).
     */
    @PostMapping("/save")
    public String saveEvent(@Valid @ModelAttribute("eventForm") EventForm form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttrs) {
        // 1) Basic validation errors?
        if (bindingResult.hasErrors()) {
            model.addAttribute("allBeverages", warehouseCatalog.getAllBeverages());
            return "event_form";
        }

        // 2) Build an Event entity from the form
        Event event = new Event();
        if (form.getEventId() != null) {
            event.setEventId(form.getEventId());
        }
        event.setName(form.getName());
        event.setDate(form.getDate());
        event.setLocation(form.getLocation());
        event.setDuration(form.getDuration());
        event.setStatus(form.getStatus());

        // 3) Set the beverages selected from Warehouse catalog:
        event.setBeverages(form.getBeverages());

        // 4) For each BarPlanForm in the form, create a BarPlan entity
        for (BarPlanForm bf : form.getBars()) {
            BarPlan bar = new BarPlan();
            if (bf.getBarId() != null) {
                bar.setBarId(bf.getBarId());
            }
            bar.setBarName(bf.getBarName());
            bar.setLocation(bf.getLocation());
            bar.setTotalCapacity(bf.getTotalCapacity());
            bar.setBeverageStock(bf.getBeverageStock());
            event.addBar(bar);
        }

        // 5) For each DropPointForm, create a DropPointPlan
        for (DropPointForm dpf : form.getDropPoints()) {
            DropPointPlan dp = new DropPointPlan();
            if (dpf.getDropPointId() != null) {
                dp.setDropPointId(dpf.getDropPointId());
            }
            dp.setLocation(dpf.getLocation());
            dp.setCapacity(dpf.getCapacity());
            event.addDropPoint(dp);
        }

        // 6) Save via service (which also does the “sum(drink qty) ≤ totalCapacity” validation).
        try {
            eventService.saveEvent(event);
        } catch (Exception ex) {
            // If the validation in saveEvent() fails (e.g. capacity exceeded), show error
            bindingResult.reject("barCapacityError", ex.getMessage());
            model.addAttribute("allBeverages", warehouseCatalog.getAllBeverages());
            return "event_form";
        }

        redirectAttrs.addFlashAttribute("success", "Event saved successfully");
        return "redirect:/events";
    }

    /**
     * Delete an event
     */
    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        eventService.deleteEvent(id);
        redirectAttrs.addFlashAttribute("success", "Event deleted");
        return "redirect:/events";
    }
}
