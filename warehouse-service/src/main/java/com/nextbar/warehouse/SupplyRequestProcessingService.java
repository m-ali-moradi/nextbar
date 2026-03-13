package com.nextbar.warehouse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.warehouse.dto.external.SupplyItemDto;
import com.nextbar.warehouse.dto.external.SupplyRequestDto;
import com.nextbar.warehouse.dto.request.SupplyFulfillmentRequest;
import com.nextbar.warehouse.dto.response.SupplyFulfillmentResponse;
import com.nextbar.warehouse.event.SupplyEventPublisher;
import com.nextbar.warehouse.exception.InsufficientStockException;
import com.nextbar.warehouse.exception.InvalidStateTransitionException;
import com.nextbar.warehouse.exception.ResourceNotFoundException;
import com.nextbar.warehouse.model.entity.SupplyRequest;
import com.nextbar.warehouse.model.enums.SupplyRequestStatus;
import com.nextbar.warehouse.repository.SupplyRequestRepository;


/**
 * Service for processing supply requests from bars.
 * Handles the workflow of fulfilling supply requests based on available stock.
 * Now uses Event-Driven architecture with local data.
 */
@Service
public class SupplyRequestProcessingService {

    private static final Logger log = LoggerFactory.getLogger(SupplyRequestProcessingService.class);

    private final StockService stockService;
    private final SupplyRequestRepository supplyRequestRepository;
    private final SupplyEventPublisher eventPublisher;

    public SupplyRequestProcessingService(StockService stockService,
            SupplyRequestRepository supplyRequestRepository,
            SupplyEventPublisher eventPublisher) {
        this.stockService = stockService;
        this.supplyRequestRepository = supplyRequestRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Get supply requests for a specific bar.
     */
    @Transactional(readOnly = true)
    public List<SupplyRequestDto> getSupplyRequests(UUID barId) {
        log.debug("Fetching local supply requests for bar: {}", barId);
        List<SupplyRequest> requests = supplyRequestRepository.findByBarId(barId);
        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all supply requests ordered by creation date.
     */
    @Transactional(readOnly = true)
    public List<SupplyRequestDto> getAllSupplyRequests() {
        log.debug("Fetching all local supply requests");
        List<SupplyRequest> requests = supplyRequestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific supply request for a specific bar.
     */
    @Transactional(readOnly = true)
    public SupplyRequestDto getSupplyRequest(UUID barId, UUID requestId) {
        return toDto(findOwnedRequestOrThrow(barId, requestId));
    }

    /**
     * Reject a supply request with a reason.
     */
    @Transactional
    public SupplyRequestDto rejectSupplyRequest(UUID barId, UUID requestId, String reason) {
        log.info("Rejecting supply request: requestId={}, reason={}", requestId, reason);

        SupplyRequest request = findOwnedRequestOrThrow(barId, requestId);

        if (!request.getStatus().canTransitionTo(SupplyRequestStatus.REJECTED)) {
            throw new InvalidStateTransitionException(request.getStatus().name(), SupplyRequestStatus.REJECTED.name());
        }

        request.setFulfilledQuantity(0);
        updateRequestStatus(request, SupplyRequestStatus.REJECTED, reason, request.getFulfilledQuantity());
        return toDto(request);
    }

    /**
     * Process a supply request fulfillment.
     */
    @Transactional
    public SupplyFulfillmentResponse processSupplyRequest(UUID barId, UUID requestId,
            SupplyFulfillmentRequest requestDto) {
        log.info("Processing supply request: requestId={}, beverage={}, quantity={}, currentStatus={}",
                requestId, requestDto.getBeverageType(), requestDto.getQuantity(), requestDto.getCurrentStatus());

        SupplyRequest request = findOwnedRequestOrThrow(barId, requestId);

        SupplyRequestStatus currentStatus = SupplyRequestStatus.fromString(requestDto.getCurrentStatus());

        if (request.getStatus() != currentStatus) {
            throw new InvalidStateTransitionException(
                    String.format("Status mismatch for request %s: DB=%s, DTO=%s",
                            requestId, request.getStatus(), currentStatus));
        }

        String beverageType = requestDto.getBeverageType();
        int requestedQuantity = requestDto.getQuantity();

        return switch (request.getStatus()) {
            case REQUESTED -> processFromRequested(request, beverageType, requestedQuantity);
            case IN_PROGRESS -> processFromInProgress(request);
            default -> throw new InvalidStateTransitionException(
                    "Cannot process supply request from status: " + request.getStatus());
        };
    }

    private SupplyFulfillmentResponse processFromRequested(SupplyRequest request,
            String beverageType, int requestedQuantity) {
        if (!request.getStatus().canTransitionTo(SupplyRequestStatus.IN_PROGRESS)) {
            throw new InvalidStateTransitionException(request.getStatus().name(), SupplyRequestStatus.IN_PROGRESS.name());
        }

        request.setRequestedQuantity(requestedQuantity);
        int available = stockService.getAvailableQuantity(beverageType);

        if (available == 0) {
            log.info("No stock available for {}, rejecting request", beverageType);
            request.setFulfilledQuantity(0);
            updateRequestStatus(request, SupplyRequestStatus.REJECTED, "No stock available", request.getFulfilledQuantity());

            return buildResponse(request, 0, requestedQuantity, SupplyRequestStatus.REJECTED,
                    "Stock unavailable. Request rejected.");
        }

        int fulfilled;
        try {
            fulfilled = stockService.deductStock(beverageType, requestedQuantity);
        } catch (InsufficientStockException e) {
            log.warn("Stock check passed but deduction failed for {}", beverageType);
            request.setFulfilledQuantity(0);
            updateRequestStatus(request, SupplyRequestStatus.REJECTED, "Stock became unavailable",
                    request.getFulfilledQuantity());
            return buildResponse(request, 0, requestedQuantity, SupplyRequestStatus.REJECTED,
                    "Stock became unavailable. Request rejected.");
        }

        request.setFulfilledQuantity(fulfilled);
        updateRequestStatus(request, SupplyRequestStatus.IN_PROGRESS, null, request.getFulfilledQuantity());

        String message;
        if (fulfilled < requestedQuantity) {
            int missing = requestedQuantity - fulfilled;
            message = String.format("Partial fulfillment: %d of %d units. %d not available. Status: IN_PROGRESS",
                    fulfilled, requestedQuantity, missing);
        } else {
            message = "Full quantity available. Status: IN_PROGRESS";
        }

        return buildResponse(request, fulfilled, requestedQuantity, SupplyRequestStatus.IN_PROGRESS, message);
    }

    private SupplyFulfillmentResponse processFromInProgress(SupplyRequest request) {
        if (!request.getStatus().canTransitionTo(SupplyRequestStatus.DELIVERED)) {
            throw new InvalidStateTransitionException(request.getStatus().name(), SupplyRequestStatus.DELIVERED.name());
        }

        log.info("Marking request {} as delivered", request.getId());
        updateRequestStatus(request, SupplyRequestStatus.DELIVERED, null, request.getFulfilledQuantity());

        return buildResponse(request, request.getFulfilledQuantity(), request.getRequestedQuantity(),
                SupplyRequestStatus.DELIVERED,
                "Supply delivered successfully.");
    }

    private void updateRequestStatus(SupplyRequest request, SupplyRequestStatus newStatus, String rejectionReason,
            int fulfilledQuantity) {
        log.debug("Updating request {} status to {}", request.getId(), newStatus);
        request.setStatus(newStatus);
        if (newStatus == SupplyRequestStatus.REJECTED) {
            request.setRejectionReason(rejectionReason);
        } else {
            request.setRejectionReason(null);
        }
        supplyRequestRepository.save(request);

        eventPublisher.publishSupplyRequestUpdated(request, fulfilledQuantity);
    }

    // Helper method to build response
    private SupplyFulfillmentResponse buildResponse(SupplyRequest request, int fulfilled, int requested,
            SupplyRequestStatus status, String message) {
        return SupplyFulfillmentResponse.builder()
                .barId(request.getBarId())
                .requestId(request.getId())
                .quantityFulfilled(fulfilled)
                .quantityRequested(requested)
                .newStatus(status)
                .message(message)
                .build();
    }

    // Helper method to convert entity to DTO
    private SupplyRequestDto toDto(SupplyRequest entity) {
        SupplyRequestDto dto = new SupplyRequestDto();
        dto.setId(entity.getId());
        dto.setBarId(entity.getBarId());
        dto.setBarName(entity.getBarName());
        dto.setStatus(entity.getStatus().name());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setCreatedAt(entity.getCreatedAt().toString());

        List<SupplyItemDto> items = entity.getItems().stream()
                .map(item -> new SupplyItemDto(item.getProductId(), item.getProductName(), item.getQuantity()))
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }

    private SupplyRequest findOwnedRequestOrThrow(UUID barId, UUID requestId) {
        SupplyRequest request = supplyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("SupplyRequest", requestId));

        if (!request.getBarId().equals(barId)) {
            throw new ResourceNotFoundException("SupplyRequest", requestId);
        }

        return request;
    }
}
