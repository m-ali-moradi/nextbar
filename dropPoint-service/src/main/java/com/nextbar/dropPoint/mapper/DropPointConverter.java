package com.nextbar.dropPoint.mapper;

import com.nextbar.dropPoint.domain.DropPoint;
import com.nextbar.dropPoint.domain.DropPointStatus;
import com.nextbar.dropPoint.dto.DropPointDto;

/****
 * Converter class for mapping between DropPoint entities and DTOs.
 * Handles null values and provides default fallbacks for missing fields.
 */
public class DropPointConverter {

    public static DropPointDto toDto(DropPoint dropPointEntity) {
        Integer capacityValue = dropPointEntity.getCapacity();
        Integer currentEmptiesValue = dropPointEntity.getCurrent_empties_stock();
        int capacity = capacityValue != null ? capacityValue.intValue() : 0;
        int currentEmpties = currentEmptiesValue != null ? currentEmptiesValue.intValue() : 0;
        DropPointStatus status = dropPointEntity.getStatus() != null
            ? dropPointEntity.getStatus()
            : DropPointStatus.EMPTY;

        return new DropPointDto(
                dropPointEntity.getId(),
                dropPointEntity.getLocation(),
            capacity,
            currentEmpties,
            status
        );
    }

    public static DropPoint toEntity(DropPointDto dto) {

        DropPoint dropPoint = new DropPoint();
        dropPoint.setLocation(dto.getLocation());
        dropPoint.setCapacity(dto.getCapacity());
        dropPoint.setCurrent_empties_stock(dto.getCurrent_empties());
        dropPoint.setStatus(dto.getStatus());
        return dropPoint;

    }


}
