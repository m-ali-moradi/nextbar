package de.fhdo.eventPlanner.controller;

import de.fhdo.eventPlanner.dto.BarPlanForm;
import de.fhdo.eventPlanner.dto.DropPointForm;
import de.fhdo.eventPlanner.dto.EventForm;
import de.fhdo.eventPlanner.mock.WarehouseCatalog;
import de.fhdo.eventPlanner.service.EventPlanningService;
import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.DefineBeverage;
import de.fhdo.eventPlanner.model.BarPlan;
import de.fhdo.eventPlanner.model.DropPointPlan;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Controller
@SessionAttributes("eventForm")
public class EventWizardController {

    private final EventPlanningService eventService;
    private final WarehouseCatalog warehouseCatalog;

    @Autowired
    public EventWizardController(EventPlanningService eventService,
                                 WarehouseCatalog warehouseCatalog) {
        this.eventService = eventService;
        this.warehouseCatalog = warehouseCatalog;
    }

    /**
     * If no EventForm exists in session, provide a fresh one.
     */
    @ModelAttribute("eventForm")
    public EventForm eventForm() {
        EventForm form = new EventForm();
        form.setStatus(de.fhdo.eventPlanner.model.Status.PLANNED);
        return form;
    }

    // ────────────────────────────────────
    // STEP 1: EVENT DETAILS
    // ────────────────────────────────────
    @GetMapping("/events/create/step1")
    public String showStep1(@ModelAttribute("eventForm") EventForm eventForm,
                            Model model) {
        model.addAttribute("allBeverages", warehouseCatalog.getAllBeverages());
        return "events/step1";    // templates/events/step1.html
    }

    @PostMapping("/events/create/step1")
    public String processStep1(
            @Valid @ModelAttribute("eventForm") EventForm eventForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasFieldErrors("name") ||
                bindingResult.hasFieldErrors("date") ||
                bindingResult.hasFieldErrors("location") ||
                bindingResult.hasFieldErrors("duration") ||
                bindingResult.hasFieldErrors("status") ||
                bindingResult.hasFieldErrors("beverages")) {

            model.addAttribute("allBeverages", warehouseCatalog.getAllBeverages());
            return "events/step1";
        }
        return "redirect:/events/create/step2";
    }

    // ────────────────────────────────────
    // STEP 2: BARS CONFIGURATION
    // ────────────────────────────────────
    @GetMapping("/events/create/step2")
    public String showStep2(@ModelAttribute("eventForm") EventForm eventForm,
                            Model model) {
        // 1) If no bars exist yet, start with a single blank BarPlanForm
        if (eventForm.getBars() == null || eventForm.getBars().isEmpty()) {
            List<BarPlanForm> bars = new ArrayList<>();
            BarPlanForm blankBar = new BarPlanForm();
            blankBar.setBeverageStock(new HashMap<>());
            bars.add(blankBar);
            eventForm.setBars(bars);
        }

        // 2) Compute only those beverages that were selected in Step 1
        List<DefineBeverage> fullCatalog = warehouseCatalog.getAllBeverages();
        List<DefineBeverage> selectedBeverages = fullCatalog.stream()
                .filter(b -> eventForm.getBeverageIds().contains(b.getId()))
                .toList();

        // 3) Expose the filtered list to Thymeleaf as "selectedBeverages"
        model.addAttribute("selectedBeverages", selectedBeverages);

        return "events/step2";    // templates/events/step2.html
    }

    @PostMapping("/events/create/step2")
    public String processStep2(
            @Valid @ModelAttribute("eventForm") EventForm eventForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasFieldErrors("bars")) {
            model.addAttribute("allBeverages", warehouseCatalog.getAllBeverages());
            return "events/step2";
        }
        return "redirect:/events/create/step3";
    }

    // ────────────────────────────────────
    // STEP 3: DROP POINTS & FINAL SAVE
    // ────────────────────────────────────
    @GetMapping("/events/create/step3")
    public String showStep3(@ModelAttribute("eventForm") EventForm eventForm,
                            Model model) {
        if (eventForm.getDropPoints() == null || eventForm.getDropPoints().isEmpty()) {
            List<DropPointForm> drops = new ArrayList<>();
            drops.add(new DropPointForm());
            eventForm.setDropPoints(drops);
        }
        return "events/step3";   // templates/events/step3.html
    }

    @PostMapping("/events/create/step3")
    public String processStep3(
            @Valid @ModelAttribute("eventForm") EventForm eventForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs,
            SessionStatus sessionStatus
    ) {
        // If dropPoints validation fails, re‐show step3:
        if (bindingResult.hasFieldErrors("dropPoints")) {
            return "events/step3";
        }

        // Build Event from EventForm
        Event event = new Event();
        if (eventForm.getEventId() != null) {
            event.setEventId(eventForm.getEventId());
        }
        event.setName(eventForm.getName());
        event.setDate(eventForm.getDate());
        event.setLocation(eventForm.getLocation());
        event.setDuration(eventForm.getDuration());
        event.setStatus(eventForm.getStatus());

        // ─── Convert beverageIds → List<DefineBeverage> and set on event
        List<DefineBeverage> selectedBeverages = warehouseCatalog.getAllBeverages().stream()
                .filter(b -> eventForm.getBeverageIds().contains(b.getId()))
                .toList();
        event.setBeverages(selectedBeverages);

        // Build BarPlan entities
        for (BarPlanForm bf : eventForm.getBars()) {
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

        // Build DropPointPlan entities
        for (DropPointForm dpf : eventForm.getDropPoints()) {
            DropPointPlan dp = new DropPointPlan();
            if (dpf.getDropPointId() != null) {
                dp.setDropPointId(dpf.getDropPointId());
            }
            dp.setLocation(dpf.getLocation());
            dp.setCapacity(dpf.getCapacity());
            event.addDropPoint(dp);
        }

        // Attempt to save; if service throws an exception (e.g. capacity overflow),
        // reject and re‐show step3:
        try {
            eventService.saveEvent(event);
        } catch (Exception ex) {
            bindingResult.reject("barCapacityError", ex.getMessage());
            return "events/step3";
        }

        sessionStatus.setComplete();
        redirectAttrs.addFlashAttribute("success", "Event saved successfully");
        return "redirect:/events";
    }

    // ────────────────────────────────────
    // EDIT EXISTING EVENT
    // ────────────────────────────────────
    @GetMapping("/events/create/edit/{id}")
    public String beginEdit(@PathVariable("id") Long id,
                            @ModelAttribute("eventForm") EventForm eventForm,
                            Model model,
                            RedirectAttributes redirectAttrs) {
        Optional<Event> opt = eventService.findEventById(id);
        if (!opt.isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Event ID " + id + " not found");
            return "redirect:/events";
        }
        Event existing = opt.get();

        eventForm.setEventId(existing.getEventId());
        eventForm.setName(existing.getName());
        eventForm.setDate(existing.getDate());
        eventForm.setLocation(existing.getLocation());
        eventForm.setDuration(existing.getDuration());
        eventForm.setStatus(existing.getStatus());
        // ─── Convert existing.getBeverages() → List<Long> beverageIds ───
        List<Long> bevIds = existing.getBeverages().stream()
                .map(DefineBeverage::getId)
                .toList();
        eventForm.setBeverageIds(bevIds);

        eventForm.getBars().clear();
        for (var bar : existing.getBars()) {
            BarPlanForm bf = new BarPlanForm();
            bf.setBarId(bar.getBarId());
            bf.setBarName(bar.getBarName());
            bf.setLocation(bar.getLocation());
            bf.setTotalCapacity(bar.getTotalCapacity());
            bf.setBeverageStock(new HashMap<>(bar.getBeverageStock()));
            eventForm.getBars().add(bf);
        }

        eventForm.getDropPoints().clear();
        for (var dp : existing.getDropPoints()) {
            DropPointForm dpf = new DropPointForm();
            dpf.setDropPointId(dp.getDropPointId());
            dpf.setLocation(dp.getLocation());
            dpf.setCapacity(dp.getCapacity());
            eventForm.getDropPoints().add(dpf);
        }

        model.addAttribute("allBeverages", warehouseCatalog.getAllBeverages());
        return "redirect:/events/create/step1";
    }

    // ────────────────────────────────────
    // LIST ALL EVENTS  GET /events
    // ────────────────────────────────────
    @GetMapping("/events")
    public String listEvents(Model model) {
        List<Event> all = eventService.findAllEvents();
        model.addAttribute("events", all);
        return "event_list";
    }

    // ────────────────────────────────────
    // SHOW EVENT DETAILS  GET /events/details/{id}
    // ────────────────────────────────────
    @GetMapping("/events/details/{id}")
    public String showDetails(@PathVariable("id") Long id, Model model) {
        Event event = eventService.findEventById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        model.addAttribute("event", event);
        return "event_details";
    }

    // ────────────────────────────────────
    // DELETE AN EVENT  GET /events/delete/{id}
    // ────────────────────────────────────
    @GetMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable("id") Long id,
                              RedirectAttributes redirectAttrs) {
        eventService.deleteEvent(id);
        redirectAttrs.addFlashAttribute("success", "Event deleted");
        return "redirect:/events";
    }
}
