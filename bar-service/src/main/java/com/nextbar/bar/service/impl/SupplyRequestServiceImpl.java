package com.nextbar.bar.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.nextbar.bar.event.SupplyEventPublisher;
import com.nextbar.bar.exception.ValidationException;

import com.nextbar.bar.dto.response.SupplyItemDto;
import com.nextbar.bar.dto.response.SupplyRequestDto;
import com.nextbar.bar.model.Bar;
import com.nextbar.bar.model.SupplyItem;
import com.nextbar.bar.model.SupplyRequest;
import com.nextbar.bar.model.SupplyStatus;
import com.nextbar.bar.repository.BarRepository;
import com.nextbar.bar.repository.SupplyRequestRepository;
import com.nextbar.bar.service.SupplyRequestService;

import lombok.RequiredArgsConstructor;

/**
 * SupplyRequestServiceImpl provides the implementation for managing supply
 * requests.
 * It includes methods for creating, retrieving, updating, and deleting supply
 * requests,
 * along with validation of input parameters.
 */
@Service
@RequiredArgsConstructor
public class SupplyRequestServiceImpl implements SupplyRequestService {

    private static final Logger log = LoggerFactory.getLogger(SupplyRequestServiceImpl.class);

    // Repositories for accessing supply requests and products
    private final SupplyRequestRepository requestRepo;
    private final BarRepository barRepository;

    // Event publisher for notifying warehouse service
    private final SupplyEventPublisher eventPublisher;

    // Constants for validation of supply quantities
    private static final int MIN_SUPPLY_QUANTITY = 1;
    private static final int MAX_SUPPLY_QUANTITY = 50;

    // Creates a new supply request for a bar with specified items.
    @Override
    public SupplyRequestDto createRequest(@NonNull UUID barId, List<SupplyItemDto> items) {

        // check if bar exists
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found for ID: " + barId);
        }
        // check if quantity of requested items are within valid range
        for (SupplyItemDto item : items) {
            if (item.productName() == null || item.productName().isBlank()) {
                throw new ValidationException("Product name is required");
            }
            if (item.quantity() < MIN_SUPPLY_QUANTITY || item.quantity() > MAX_SUPPLY_QUANTITY) {
                throw new ValidationException(
                        String.format("Supply quantity must be between %d and %d, but was %d for product '%s'",
                                MIN_SUPPLY_QUANTITY, MAX_SUPPLY_QUANTITY, item.quantity(), item.productName()));
            }
        }

        // check if there is already a supply request for the bar and product in
        // REQUESTED, IN_PROGRESS, or DELIVERED status
        // for (SupplyItemDto item: items) {
        // if (requestRepo.existsByBarIdAndProductIdAndStatusIn(barId, item.productId(),
        // List.of(SupplyStatus.REQUESTED, SupplyStatus.IN_PROGRESS,
        // SupplyStatus.DELIVERED))) {
        // throw new ValidationException("A supply request already exists for the bar
        // and product in REQUESTED, IN_PROGRESS, or DELIVERED status. Product ID: " +
        // item.productId());
        // }
        // }

        // Create a new supply request
        SupplyRequest req = new SupplyRequest();
        req.setId(UUID.randomUUID());
        req.setBarId(barId);
        req.setStatus(SupplyStatus.REQUESTED);
        req.setCreatedAt(LocalDateTime.now());

        // Convert DTOs to entity items
        List<SupplyItem> supplyItems = items.stream()
                .map(dto -> new SupplyItem(dto.productName().trim(), dto.quantity()))
                .toList();
        req.setItems(supplyItems);

        // Save the request
        SupplyRequestDto savedRequest = toDto(requestRepo.save(req));

        // Publish event to notify warehouse service
        try {
            Bar bar = barRepository.findById(barId).orElse(null);
            String barName = bar != null ? bar.getName() : "Unknown Bar";
            eventPublisher.publishSupplyRequestCreated(savedRequest, barName);
        } catch (Exception e) {
            log.error("Failed to publish supply request created event: {}", e.getMessage());
            // Continue even if event publishing fails - the request is still saved
        }

        return savedRequest;
    }

    // Retrieves all supply requests for a specific bar, sorted by created date in
    // descending order.
    @Override
    public List<SupplyRequestDto> getRequestsByBar(@NonNull UUID barId) {

        // check if there is requests for the given bar.
        // if (requestRepo.findByBarId(barId).isEmpty()) {
        // throw new ValidationException("No supply requests found for bar ID: " +
        // barId);
        // }

        // Fetch and convert supply requests for the given bar if they exist
        return requestRepo.findByBarId(barId).stream()
                .sorted(Comparator.comparing(SupplyRequest::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toDto)
                .toList();
    }

    // Retrieves a specific supply request by its ID
    @Override
    public SupplyRequestDto getRequest(@NonNull UUID requestId) {

        // Fetch a specific supply request or throw an exception if not found
        return requestRepo.findWithItemsById(requestId)
                .map(this::toDto)
                .orElseThrow(() -> new ValidationException("Supply request not found for ID: " + requestId));
    }

    @Override
    public void updateRequestStatus(@NonNull UUID requestId, Integer quantity, SupplyStatus status) {
        /*
         * Data validation for requestId and status
         * 1. request should be available
         * 2. status should be a valid SupplyStatus
         * 3. REJECTED requests cannot be updated
         * 4. REQUESTED requests can only be updated to IN_PROGRESS
         * 5. IN_PROGRESS requests can only be updated to DELIVERED
         * 6. DELIVERED requests cannot be updated
         */

        // check if the request is available
        SupplyRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ValidationException("Supply request not found for ID: " + requestId));

        // Rejected requests cannot be updated
        if (req.getStatus() == SupplyStatus.REJECTED) {
            throw new ValidationException("Rejected requests cannot be updated");
        }
        // REQUESTED requests can only be updated to IN_PROGRESS
        if (req.getStatus() == SupplyStatus.REQUESTED && status != SupplyStatus.IN_PROGRESS) {
            throw new ValidationException("REQUESTED requests can only be updated to IN_PROGRESS");
        }
        // IN_PROGRESS requests can only be updated to DELIVERED
        if (req.getStatus() == SupplyStatus.IN_PROGRESS && status != SupplyStatus.DELIVERED) {
            throw new ValidationException("IN_PROGRESS requests can only be updated to DELIVERED");
        }
        // DELIVERED request can only be updated to COMPLETED
        if (req.getStatus() == SupplyStatus.DELIVERED && status != SupplyStatus.COMPLETED) {
            throw new ValidationException("DELIVERED requests can only be updated to COMPLETED");
        }
        // COMPLETED requests cannot be updated
        if (req.getStatus() == SupplyStatus.COMPLETED) {
            throw new ValidationException("COMPLETED requests cannot be updated");
        }
        // Update the status of the supply request
        // if the quantity is not null or zero, set SupplyItem.quantity = quantity
        if (quantity != null && quantity > 0) {
            req.getItems().forEach(item -> item.setQuantity(quantity));
        }
        req.setStatus(status);
        requestRepo.save(req);
    }

    @Override
    public void deleteRequest(@NonNull UUID requestId) {
        /*
         * Data validation for requestId
         * 1. request should be available
         * 2. only REQUESTED requests can be deleted
         * 3. IN_PROGRESS and DELIVERED requests cannot be deleted
         * 4. REJECTED requests cannot be deleted
         */
        // check if the request is available

        // check if the request exists
        SupplyRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ValidationException("Supply request not found for ID: " + requestId));

        if (req.getStatus() != SupplyStatus.REQUESTED) {
            throw new ValidationException("Only REQUESTED supply requests can be cancelled");
        }

        // Delete the supply request
        requestRepo.deleteById(requestId);
    }

    // Converts a SupplyRequest entity to a SupplyRequestDto
    private SupplyRequestDto toDto(SupplyRequest req) {
        List<SupplyItemDto> itemDtos = (req.getItems() == null ? List.<SupplyItem>of() : req.getItems()).stream()
                .map(i -> new SupplyItemDto(
                        i.getProductName() == null ? "" : i.getProductName(),
                        i.getQuantity()))
                .toList();

        return new SupplyRequestDto(
                Objects.requireNonNull(req.getId()),
                Objects.requireNonNull(req.getBarId()),
                itemDtos,
                req.getStatus(),
                req.getRejectionReason(),
                req.getCreatedAt());
    }

}