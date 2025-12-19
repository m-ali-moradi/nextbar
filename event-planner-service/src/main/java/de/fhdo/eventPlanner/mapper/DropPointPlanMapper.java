package de.fhdo.eventPlanner.mapper;

import org.springframework.stereotype.Component;

import de.fhdo.eventPlanner.dto.DropPointForm;
import de.fhdo.eventPlanner.model.DropPointPlan;

/**
 * Mapper for converting between DropPointPlan entity and DropPointForm DTO.
 */
@Component
public class DropPointPlanMapper {

    /**
     * Converts a DropPointPlan entity to a DropPointForm DTO.
     *
     * @param dropPoint the DropPointPlan entity
     * @return the DropPointForm DTO
     */
    public DropPointForm toDto(DropPointPlan dropPoint) {
        if (dropPoint == null) {
            return null;
        }
        DropPointForm form = new DropPointForm();
        form.setDropPointId(dropPoint.getDropPointId());
        form.setLocation(dropPoint.getLocation());
        form.setCapacity(dropPoint.getCapacity());
        if (dropPoint.getEvent() != null) {
            form.setEventId(dropPoint.getEvent().getEventId());
        }
        return form;
    }

    /**
     * Converts a DropPointForm DTO to a DropPointPlan entity.
     *
     * @param form the DropPointForm DTO
     * @return the DropPointPlan entity
     */
    public DropPointPlan toEntity(DropPointForm form) {
        if (form == null) {
            return null;
        }
        DropPointPlan dropPoint = new DropPointPlan();
        dropPoint.setDropPointId(form.getDropPointId());
        dropPoint.setLocation(form.getLocation());
        dropPoint.setCapacity(form.getCapacity());
        // Event relationship is set separately by the service
        return dropPoint;
    }

    /**
     * Updates an existing DropPointPlan entity from a DropPointForm DTO.
     *
     * @param form the source DTO
     * @param dropPoint the target entity to update
     */
    public void updateEntity(DropPointForm form, DropPointPlan dropPoint) {
        if (form == null || dropPoint == null) {
            return;
        }
        dropPoint.setLocation(form.getLocation());
        dropPoint.setCapacity(form.getCapacity());
    }
}
