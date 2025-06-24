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
        dropPoint.setLocation(dto.getLocation());
        dropPoint.setCapacity(dto.getCapacity());
        dropPoint.setCurrent_empties_stock(dto.getCurrent_empties());
        dropPoint.setStatus(dto.getStatus());
        return dropPoint;

    }


}
