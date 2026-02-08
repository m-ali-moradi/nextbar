package com.dmsa.warehouse.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dmsa.warehouse.dto.external.SupplyItemDto;
import com.dmsa.warehouse.dto.external.SupplyRequestDto;
import com.dmsa.warehouse.dto.request.SupplyFulfillmentRequest;
import com.dmsa.warehouse.dto.response.SupplyFulfillmentResponse;
import com.dmsa.warehouse.event.SupplyEventPublisher;
import com.dmsa.warehouse.exception.InsufficientStockException;
import com.dmsa.warehouse.exception.InvalidStateTransitionException;
import com.dmsa.warehouse.exception.ResourceNotFoundException;
import com.dmsa.warehouse.model.entity.SupplyRequest;
import com.dmsa.warehouse.model.enums.SupplyRequestStatus;
import com.dmsa.warehouse.repository.SupplyRequestRepository;


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
     * Get supply requests for a specific bar (from local DB).
     */
    public List<SupplyRequestDto> getSupplyRequests(UUID barId) {
        log.debug("Fetching local supply requests for bar: {}", barId);
        List<SupplyRequest> requests = supplyRequestRepository.findByBarId(barId);
        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all supply requests ordered by creation date (from local DB).
     */
    public List<SupplyRequestDto> getAllSupplyRequests() {
        log.debug("Fetching all local supply requests");
        List<SupplyRequest> requests = supplyRequestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific supply request (from local DB).
     */
    public SupplyRequestDto getSupplyRequest(UUID barId, UUID requestId) {
        log.debug("Fetching local supply request: barId={}, requestId={}", barId, requestId);
        SupplyRequest request = supplyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("SupplyRequest", requestId));

        if (!request.getBarId().equals(barId)) {
            // Technically could be a mismatch, but ID is unique anyway
            log.warn("Bar ID mismatch for request {}: expected {}, got {}",
                    requestId, barId, request.getBarId());
        }

        return toDto(request);
    }

    /**
     * Reject a supply request with a reason.
     */
    @Transactional
    public SupplyRequestDto rejectSupplyRequest(UUID barId, UUID requestId, String reason) {
        log.info("Rejecting supply request: requestId={}, reason={}", requestId, reason);

        SupplyRequest request = supplyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("SupplyRequest", requestId));

        if (!request.getBarId().equals(barId)) {
            log.warn("Bar ID mismatch for request {}: expected {}, got {}",
                    requestId, barId, request.getBarId());
        }

        if (request.getStatus() != SupplyRequestStatus.REQUESTED) {
            throw new InvalidStateTransitionException(
                    "Cannot reject supply request from status: " + request.getStatus());
        }

        updateRequestStatus(request, SupplyRequestStatus.REJECTED, reason);
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

        SupplyRequest request = supplyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("SupplyRequest", requestId));

        SupplyRequestStatus currentStatus = SupplyRequestStatus.fromString(requestDto.getCurrentStatus());

        // Validate status matches DB
        if (request.getStatus() != currentStatus) {
            log.warn("Status mismatch for request {}: DB={}, DTO={}",
                    requestId, request.getStatus(), currentStatus);
            // We proceed with DB status or throw? Let's use DB status logic but throw if
            // transitions are invalid
        }

        String beverageType = requestDto.getBeverageType();
        int requestedQuantity = requestDto.getQuantity();

        return switch (request.getStatus()) {
            case REQUESTED -> processFromRequested(request, beverageType, requestedQuantity);
            case IN_PROGRESS -> processFromInProgress(request, requestedQuantity);
            default -> throw new InvalidStateTransitionException(
                    "Cannot process supply request from status: " + request.getStatus());
        };
    }

    private SupplyFulfillmentResponse processFromRequested(SupplyRequest request,
            String beverageType, int requestedQuantity) {
        int available = stockService.getAvailableQuantity(beverageType);

        if (available == 0) {
            log.info("No stock available for {}, rejecting request", beverageType);
            updateRequestStatus(request, SupplyRequestStatus.REJECTED, "No stock available");

            return buildResponse(request, 0, requestedQuantity, SupplyRequestStatus.REJECTED,
                    "Stock unavailable. Request rejected.");
        }

        int fulfilled;
        try {
            fulfilled = stockService.deductStock(beverageType, requestedQuantity);
        } catch (InsufficientStockException e) {
            log.warn("Stock check passed but deduction failed for {}", beverageType);
            updateRequestStatus(request, SupplyRequestStatus.REJECTED, "Stock became unavailable");
            return buildResponse(request, 0, requestedQuantity, SupplyRequestStatus.REJECTED,
                    "Stock became unavailable. Request rejected.");
        }

        updateRequestStatus(request, SupplyRequestStatus.IN_PROGRESS, null);

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

    private SupplyFulfillmentResponse processFromInProgress(SupplyRequest request, int quantity) {
        log.info("Marking request {} as delivered", request.getId());
        updateRequestStatus(request, SupplyRequestStatus.DELIVERED, null);

        return buildResponse(request, quantity, quantity, SupplyRequestStatus.DELIVERED,
                "Supply delivered successfully.");
    }

    private void updateRequestStatus(SupplyRequest request, SupplyRequestStatus newStatus, String rejectionReason) {
        log.debug("Updating request {} status to {}", request.getId(), newStatus);
        request.setStatus(newStatus);
        if (rejectionReason != null) {
            request.setRejectionReason(rejectionReason);
        }
        supplyRequestRepository.save(request);

        // Publish event
        eventPublisher.publishSupplyRequestUpdated(request);
    }

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
}
