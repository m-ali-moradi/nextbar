package de.fhdo.dropPointsSys.converters;

import de.fhdo.dropPointsSys.domain.DropPoint;
import de.fhdo.dropPointsSys.dto.DropPointDto;

public class DropPointConverter {

    public static DropPointDto toDto(DropPoint dropPointEntity) {
        return new DropPointDto(
                dropPointEntity.getId(),
                dropPointEntity.getLocation(),
                dropPointEntity.getCapacity(),
                dropPointEntity.getCurrent_empties_stock(),
                dropPointEntity.getStatus()
        );
    }

    public static DropPoint toEntity(DropPointDto dto) {

        DropPoint dropPoint = new DropPoint();
        dropPoint.setId(dto.getId());
        dropPoint.setLocation(dto.getLocation());
        dropPoint.setCapacity(dto.getCapacity());
        dropPoint.setStatus(dto.getStatus());
        return dropPoint;

    }


}
