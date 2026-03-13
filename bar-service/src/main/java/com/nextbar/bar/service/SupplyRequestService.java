package com.nextbar.bar.service;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.nextbar.bar.model.SupplyStatus;
import com.nextbar.bar.dto.response.SupplyItemDto;
import com.nextbar.bar.dto.response.SupplyRequestDto;

/**
 * Service interface for managing supply requests in a bar.
 * Provides methods to create, retrieve, update, and delete supply requests.
 */
public interface SupplyRequestService {
    SupplyRequestDto createRequest(@NonNull UUID barId, List<SupplyItemDto> items);

    List<SupplyRequestDto> getRequestsByBar(@NonNull UUID barId);

    SupplyRequestDto getRequest(@NonNull UUID requestId);

    void updateRequestStatus(@NonNull UUID requestId, Integer quantity, SupplyStatus status);

    void deleteRequest(@NonNull UUID requestId);
}
