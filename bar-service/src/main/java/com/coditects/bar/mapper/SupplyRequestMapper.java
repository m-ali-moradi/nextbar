package com.coditects.bar.mapper;

import com.coditects.bar.model.SupplyItem;
import com.coditects.bar.model.SupplyRequest;
import com.coditects.bar.model.dto.SupplyItemDto;
import com.coditects.bar.model.dto.SupplyRequestDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between SupplyRequest entity and SupplyRequestDto.
 */
@Component
public class SupplyRequestMapper {

    /**
     * Converts a SupplyRequest entity to a SupplyRequestDto.
     *
     * @param request the SupplyRequest entity
     * @return the SupplyRequestDto
     */
    public SupplyRequestDto toDto(SupplyRequest request) {
        if (request == null) {
            return null;
        }
        return new SupplyRequestDto(
                request.getId(),
                request.getBarId(),
                request.getItems().stream()
                        .map(this::itemToDto)
                        .collect(Collectors.toList()),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    /**
     * Converts a SupplyItem to a SupplyItemDto.
     *
     * @param item the SupplyItem
     * @return the SupplyItemDto
     */
    private SupplyItemDto itemToDto(SupplyItem item) {
        if (item == null) {
            return null;
        }
        return new SupplyItemDto(
                item.getProductId(),
                null, // productName will be set by service if needed
                item.getQuantity()
        );
    }

    /**
     * Converts a SupplyItemDto to a SupplyItem entity.
     *
     * @param dto the SupplyItemDto
     * @return the SupplyItem entity
     */
    public SupplyItem dtoToItem(SupplyItemDto dto) {
        if (dto == null) {
            return null;
        }
        SupplyItem item = new SupplyItem();
        item.setProductId(dto.productId());
        // productName is not stored in entity
        item.setQuantity(dto.quantity());
        return item;
    }
}
