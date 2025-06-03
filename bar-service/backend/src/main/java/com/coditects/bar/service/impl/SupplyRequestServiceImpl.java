package com.coditects.bar.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.coditects.bar.exception.ValidationException;
import com.coditects.bar.model.Product;
import com.coditects.bar.model.SupplyItem;
import com.coditects.bar.model.SupplyRequest;
import com.coditects.bar.model.SupplyStatus;
import com.coditects.bar.model.dto.SupplyItemDto;
import com.coditects.bar.model.dto.SupplyRequestDto;
import com.coditects.bar.repository.BarRepository;
import com.coditects.bar.repository.ProductRepository;
import com.coditects.bar.repository.SupplyRequestRepository;
import com.coditects.bar.service.SupplyRequestService;

import lombok.RequiredArgsConstructor;

/**
 * SupplyRequestServiceImpl provides the implementation for managing supply requests.
 * It includes methods for creating, retrieving, updating, and deleting supply requests,
 * along with validation of input parameters.
 */
@Service
@RequiredArgsConstructor
public class SupplyRequestServiceImpl implements SupplyRequestService {

    // Repositories for accessing supply requests and products
    private final SupplyRequestRepository requestRepo;
    private final ProductRepository productRepository;
    private final BarRepository barRepository;

    // Constants for validation of supply quantities
    private static final int MIN_SUPPLY_QUANTITY = 1;
    private static final int MAX_SUPPLY_QUANTITY = 50;

    // Creates a new supply request for a bar with specified items.
    @Override
    public SupplyRequestDto createRequest(UUID barId, List<SupplyItemDto> items) {

        // check if bar exists
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found for ID: " + barId);
        }
        // check if quantity of requested items are within valid range
        for (SupplyItemDto item : items) {
            if (item.quantity() < MIN_SUPPLY_QUANTITY || item.quantity() > MAX_SUPPLY_QUANTITY) {
                throw new ValidationException(
                        String.format("Supply quantity must be between %d and %d, but was %d for product ID %s",
                                MIN_SUPPLY_QUANTITY, MAX_SUPPLY_QUANTITY, item.quantity(), item.productId()));
            }
        }

        // check if there is an existing supply request that are in REQUESTED, IN_PROGRESS, or DELIVERED status
        if (requestRepo.existsByBarIdAndStatusIn(barId, List.of(SupplyStatus.REQUESTED, SupplyStatus.IN_PROGRESS, SupplyStatus.DELIVERED))) {
            throw new ValidationException("A supply request already exists for the bar in REQUESTED, IN_PROGRESS, or DELIVERED status.");
        }

        // Create a new supply request
        SupplyRequest req = new SupplyRequest();
        req.setId(UUID.randomUUID());
        req.setBarId(barId);
        req.setStatus(SupplyStatus.REQUESTED);
        req.setCreatedAt(LocalDateTime.now());

        // Convert DTOs to entity items
        List<SupplyItem> supplyItems = items.stream()
                .map(dto -> new SupplyItem(dto.productId(), dto.quantity()))
                .toList();
        req.setItems(supplyItems);

        // Save and return the DTO
        return toDto(requestRepo.save(req));
    }

    // Retrieves all supply requests for a specific bar, sorted by created date in descending order.
    @Override
    public List<SupplyRequestDto> getRequestsByBar(UUID barId) {

        // check if there is requests for the given bar.
//        if (requestRepo.findByBarId(barId).isEmpty()) {
//            throw new ValidationException("No supply requests found for bar ID: " + barId);
//        }

        // Fetch and convert supply requests for the given bar if they exist
        return requestRepo.findByBarId(barId).stream()
                .map(this::toDto)
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .toList();
    }

    // Retrieves a specific supply request by its ID
    @Override
    public SupplyRequestDto getRequest(UUID requestId) {

        // Fetch a specific supply request or throw an exception if not found
        return requestRepo.findById(requestId)
                .map(this::toDto)
                .orElseThrow(() -> new ValidationException("Supply request not found for ID: " + requestId));
    }

    @Override
    public void updateRequestStatus(UUID requestId, SupplyStatus status) {
        /*
         * Data validation for requestId and status
         * 1. request should be available
         * 2. status should be a valid SupplyStatus
         * 3. REJECTED requests cannot be updated
         * 4. REQUESTED requests can only be updated to IN_PROGRESS
         * 5. IN_PROGRESS requests can only be updated to DELIVERED
         * 6. DELIVERED requests cannot be updated
         */

        // check if request is available
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
        req.setStatus(status);
        requestRepo.save(req);
    }

    @Override
    public void deleteRequest(UUID requestId) {
        /*
         * Data validation for requestId
         * 1. request should be available
         * 2. only REQUESTED requests can be deleted
         * 3. IN_PROGRESS and DELIVERED requests cannot be deleted
         * 4. REJECTED requests cannot be deleted
         */
        // check if request is available
    
        // check if the request exists
        SupplyRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ValidationException("Supply request not found for ID: " + requestId));

        // IN_PROGRESS requests cannot be deleted
        if (req.getStatus() == SupplyStatus.IN_PROGRESS) {
            throw new ValidationException("IN_PROGRESS supply requests cannot be deleted");
        }
        // DELIVERED requests cannot be deleted
        if (req.getStatus() == SupplyStatus.DELIVERED) {
            throw new ValidationException("DELIVERED supply requests cannot be deleted");
        }
        // REJECTED requests cannot be deleted
        if (req.getStatus() == SupplyStatus.REJECTED) {
            throw new ValidationException("REJECTED supply requests cannot be deleted");
        }

        // Delete the supply request
        requestRepo.deleteById(requestId);
    }

    // Converts a SupplyRequest entity to a SupplyRequestDto
    private SupplyRequestDto toDto(SupplyRequest req) {
        List<SupplyItemDto> itemDtos = req.getItems().stream()
                .map(i -> {
                    // Fetch product details for each supply item
                    // If product is not found, throw an exception
                    Product product = productRepository.findById(i.getProductId())
                            .orElseThrow(() -> new ValidationException("Product not found: " + i.getProductId()));
                    return new SupplyItemDto(i.getProductId(), product.getName(), i.getQuantity());
                })
                .toList();

        return new SupplyRequestDto(req.getId(), req.getBarId(), itemDtos, req.getStatus(), req.getCreatedAt());
    }
}