package com.coditects.bar.mapper;

import com.coditects.bar.model.UsageLog;
import com.coditects.bar.model.dto.UsageLogDto;
import org.springframework.stereotype.Component;

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
                log.getId(),
                log.getBarId(),
                log.getProductId(),
                null, // productName will be set by service if needed
                log.getQuantity(),
                log.getTimestamp()
        );
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
        log.setProductId(dto.productId());
        // productName is not stored in entity
        log.setQuantity(dto.quantity());
        log.setTimestamp(dto.timestamp());
        return log;
    }
}
