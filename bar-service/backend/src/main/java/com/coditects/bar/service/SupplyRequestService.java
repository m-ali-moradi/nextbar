package com.coditects.bar.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.coditects.bar.model.SupplyStatus;
import com.coditects.bar.model.dto.SupplyItemDto;
import com.coditects.bar.model.dto.SupplyRequestDto;

/**
 * Service interface for managing supply requests in a bar.
 * Provides methods to create, retrieve, update, and delete supply requests.
 */
public interface SupplyRequestService {
    SupplyRequestDto createRequest(UUID barId, List<SupplyItemDto> items);

    List<SupplyRequestDto> getRequestsByBar(UUID barId);

    SupplyRequestDto getRequest(UUID requestId);

    void updateRequestStatus(UUID requestId, Integer quantity, SupplyStatus status);

    void deleteRequest(UUID requestId);
}
