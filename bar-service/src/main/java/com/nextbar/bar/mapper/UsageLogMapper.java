package com.nextbar.bar.mapper;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.nextbar.bar.model.UsageLog;
import com.nextbar.bar.dto.response.UsageLogDto;

/**
 * Mapper for converting between UsageLog entity and UsageLogDto.
 */
@Component
public class UsageLogMapper {

    /**
     * Converts a UsageLog entity to a UsageLogDto.
     *
     * @param log the UsageLog entity
     * @return the UsageLogDto
     */
    public UsageLogDto toDto(UsageLog log) {
        if (log == null) {
            return null;
        }
        return new UsageLogDto(
                Objects.requireNonNull(log.getId()),
                Objects.requireNonNull(log.getBarId()),
                log.getProductName(),
                log.getQuantity(),
                log.getTimestamp());
    }

    /**
     * Converts a UsageLogDto to a UsageLog entity.
     *
     * @param dto the UsageLogDto
     * @return the UsageLog entity
     */
    public UsageLog toEntity(UsageLogDto dto) {
        if (dto == null) {
            return null;
        }
        UsageLog log = new UsageLog();
        log.setId(dto.id());
        log.setBarId(dto.barId());
        log.setProductName(dto.productName());
        log.setQuantity(dto.quantity());
        log.setTimestamp(dto.timestamp());
        return log;
    }
}
