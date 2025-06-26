package com.dmsa.warehouse.services;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dmsa.warehouse.dto.ReplenishRequest;
import com.dmsa.warehouse.dto.ReplenishResponse;
import com.dmsa.warehouse.feign.BarServiceClient;

@Service
public class SupplyUpdateService {

    private final BarServiceClient barServiceClient;
    private final WarehouseService warehouseService;

    public SupplyUpdateService(BarServiceClient barServiceClient, WarehouseService warehouseService) {
        this.barServiceClient = barServiceClient;
        this.warehouseService = warehouseService;
    }

    // public void sendSupplyUpdate(UUID barId, UUID requestId,int quantity, ReplenishRequest request) {
    //     // barServiceClient.updateSupplyStatus(barId, requestId, request.getQuantity(), "IN_PROGRESS");
    //     barServiceClient.updateSupplyStatus(barId, requestId, quantity, "IN_PROGRESS");
    // }

    public ReplenishResponse processSupplyRequest(UUID barId,
                                                  UUID requestId,
                                                  ReplenishRequest request,
                                                  String currentStatus) {
        int requested   = request.getQuantity();
        String bevKey   = request.getBeverageType();
        int available   = warehouseService.getAvailableStock(bevKey);

        // From REQUESTED → …
        if ("REQUESTED".equalsIgnoreCase(currentStatus)) {
            // no stock at all → REJECTED
            if (available == 0) {
                barServiceClient.updateSupplyStatus(barId, requestId, 0, "REJECTED");
                return new ReplenishResponse(barId, requestId, 0,
                    "Stock unavailable. Status set to REJECTED.");
            }

            // some or full stock → IN_PROGRESS
            int toSend = Math.min(requested, available);
            warehouseService.replenish(bevKey, toSend);  // subtract in DB
            barServiceClient.updateSupplyStatus(barId, requestId, toSend, "IN_PROGRESS");

            if (toSend < requested) {
                int missing = requested - toSend;
                String msg = "Only " + toSend + " out of " + requested +
                             " available. " + missing + " not in stock. " +
                             "Status set to IN_PROGRESS.";
                return new ReplenishResponse(barId, requestId, toSend, msg);
            } else {
                return new ReplenishResponse(barId, requestId, toSend,
                    "Full quantity available. Status set to IN_PROGRESS.");
            }
        }

        // From IN_PROGRESS → DELIVERED
        if ("IN_PROGRESS".equalsIgnoreCase(currentStatus)) {
            barServiceClient.updateSupplyStatus(barId, requestId, requested, "DELIVERED");
            return new ReplenishResponse(barId, requestId, requested,
                "Supply delivered. Status set to DELIVERED.");
        }

        // All other transitions are invalid
        throw new IllegalStateException("Cannot update status from " + currentStatus);
    }
}
