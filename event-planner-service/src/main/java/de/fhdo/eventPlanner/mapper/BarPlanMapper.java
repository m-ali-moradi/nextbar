package de.fhdo.eventPlanner.mapper;

import org.springframework.stereotype.Component;

import de.fhdo.eventPlanner.dto.BarPlanForm;
import de.fhdo.eventPlanner.model.BarPlan;

/**
 * Mapper for converting between BarPlan entity and BarPlanForm DTO.
 */
@Component
public class BarPlanMapper {

    /**
     * Converts a BarPlan entity to a BarPlanForm DTO.
     *
     * @param barPlan the BarPlan entity
     * @return the BarPlanForm DTO
     */
    public BarPlanForm toDto(BarPlan barPlan) {
        if (barPlan == null) {
            return null;
        }
        BarPlanForm form = new BarPlanForm();
        form.setBarId(barPlan.getBarId());
        form.setBarName(barPlan.getBarName());
        form.setLocation(barPlan.getLocation());
        form.setTotalCapacity(barPlan.getTotalCapacity());
        form.setBeverageStock(barPlan.getBeverageStock());
        if (barPlan.getEvent() != null) {
            form.setEventId(barPlan.getEvent().getEventId());
        }
        form.setTotalAssignedDrinkQuantity(barPlan.getTotalAssignedDrinkQuantity());
        return form;
    }

    /**
     * Converts a BarPlanForm DTO to a BarPlan entity.
     *
     * @param form the BarPlanForm DTO
     * @return the BarPlan entity
     */
    public BarPlan toEntity(BarPlanForm form) {
        if (form == null) {
            return null;
        }
        BarPlan barPlan = new BarPlan();
        barPlan.setBarId(form.getBarId());
        barPlan.setBarName(form.getBarName());
        barPlan.setLocation(form.getLocation());
        barPlan.setTotalCapacity(form.getTotalCapacity());
        barPlan.setBeverageStock(form.getBeverageStock());
        // Event relationship is set separately by the service
        return barPlan;
    }

    /**
     * Updates an existing BarPlan entity from a BarPlanForm DTO.
     *
     * @param form the source DTO
     * @param barPlan the target entity to update
     */
    public void updateEntity(BarPlanForm form, BarPlan barPlan) {
        if (form == null || barPlan == null) {
            return;
        }
        barPlan.setBarName(form.getBarName());
        barPlan.setLocation(form.getLocation());
        barPlan.setTotalCapacity(form.getTotalCapacity());
        barPlan.setBeverageStock(form.getBeverageStock());
    }
}
