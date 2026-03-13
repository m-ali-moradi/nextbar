package com.nextbar.bar.mapper;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.nextbar.bar.model.BarStockItem;
import com.nextbar.bar.dto.response.BarStockItemDto;

/**
 * Mapper for converting between BarStockItem entity and BarStockItemDto.
 */
@Component
public class BarStockMapper {

    /**
     * Converts a BarStockItem entity to a BarStockItemDto.
     *
     * @param stockItem the BarStockItem entity
     * @return the BarStockItemDto
     */
    public BarStockItemDto toDto(BarStockItem stockItem) {
        if (stockItem == null) {
            return null;
        }
        return new BarStockItemDto(
                Objects.requireNonNull(stockItem.getId()),
                stockItem.getBarId(),
                stockItem.getProductName(),
                stockItem.getQuantity());
    }

    /**
     * Converts a BarStockItemDto to a BarStockItem entity.
     *
     * @param dto the BarStockItemDto
     * @return the BarStockItem entity
     */
    public BarStockItem toEntity(BarStockItemDto dto) {
        if (dto == null) {
            return null;
        }
        BarStockItem stockItem = new BarStockItem();
        stockItem.setId(dto.id());
        stockItem.setBarId(dto.barId());
        stockItem.setProductName(dto.productName());
        stockItem.setQuantity(dto.quantity());
        return stockItem;
    }
}
