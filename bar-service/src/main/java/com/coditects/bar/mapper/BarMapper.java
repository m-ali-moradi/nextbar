package com.coditects.bar.mapper;

import com.coditects.bar.model.Bar;
import com.coditects.bar.model.dto.BarDto;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Bar entity and BarDto.
 */
@Component
public class BarMapper {

    /**
     * Converts a Bar entity to a BarDto.
     *
     * @param bar the Bar entity
     * @return the BarDto
     */
    public BarDto toDto(Bar bar) {
        if (bar == null) {
            return null;
        }
        return new BarDto(
                bar.getId(),
                bar.getName(),
                bar.getLocation(),
                bar.getMaxCapacity()
        );
    }

    /**
     * Converts a BarDto to a Bar entity.
     * Note: Stock items are not included in this mapping.
     *
     * @param dto the BarDto
     * @return the Bar entity
     */
    public Bar toEntity(BarDto dto) {
        if (dto == null) {
            return null;
        }
        Bar bar = new Bar();
        bar.setId(dto.id());
        bar.setName(dto.name());
        bar.setLocation(dto.location());
        bar.setMaxCapacity(dto.maxCapacity());
        return bar;
    }
}
