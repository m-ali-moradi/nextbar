package com.coditects.bar.startup;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.coditects.bar.exception.ValidationException;
import com.coditects.bar.feign.EventPlannerClient;
import com.coditects.bar.model.Product;
import com.coditects.bar.model.dto.EventPlannerBarDto;
import com.coditects.bar.repository.ProductRepository;
import com.coditects.bar.service.BarService;
import com.coditects.bar.service.BarStockService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@AllArgsConstructor
@Component
public class BarDataInitializer implements ApplicationRunner {

    private final EventPlannerClient eventPlannerClient;
    private final BarService barService;
    private final ProductRepository productRepository;
    private final BarStockService barStockService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("start fetching bar plans from eventplanner service...");
        List<EventPlannerBarDto> bars = null;

        try {
            bars = eventPlannerClient.fetchBarPlans();
        } catch (Exception e) {
            log.error("fetch bar plans failed", e);
        }

        if (bars == null || bars.isEmpty()) {
            log.error("fetch bar plans failed, creating fallback data...");

            // Create fallback products
            if (productRepository.count() == 0) {
                productRepository.save(new Product(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), "Fanta"));
                productRepository.save(new Product(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"), "Beer"));
                productRepository.save(new Product(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), "Red Bull"));
                productRepository.save(new Product(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"), "Cola Zero"));
            }

            // Create fallback bar
            UUID fallbackBarId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            barService.registerBar(fallbackBarId, "CoolBar", "EF38D", 300);

            // Add fallback stock items
            barStockService.addStock(fallbackBarId, UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), 100);
            barStockService.addStock(fallbackBarId, UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"), 50);
            barStockService.addStock(fallbackBarId, UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), 30);
            barStockService.addStock(fallbackBarId, UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"), 20);
            log.info("fallback bar and stock data created successfully.");
            return;

        }


        // If data from remote serivce is fetched, process them

        for (EventPlannerBarDto remoteBar : bars) {

            // Convert remote bar ID from Int to UUID
            UUID barId = int2UUID(remoteBar.getBarId());

            // Create from remote (Enventplanner service) bar data
            barService.registerBar(
                    barId,
                    remoteBar.getBarName(),
                    remoteBar.getLocation(),
                    remoteBar.getTotalCapacity()
            );
            log.info("bar {} created successfully.", remoteBar.getBarName());

            // Extract stock items from remote and save them to local stock
            if (remoteBar.getBeverageStock() != null) {
                for (Map.Entry<String, Integer> entry : remoteBar.getBeverageStock().entrySet()) {
                    String name = entry.getKey().split("\\s*\\(")[0]; // e.g. "Coke"
                    String idStr = entry.getKey().replaceAll(".*\\((\\d+)\\)", "$1"); // e.g. "1"
                    UUID productId = int2UUID(Integer.parseInt(idStr));
                    int quantity = entry.getValue();
                    productRepository.save(new Product(productId,name));
                    try {
                        barStockService.addStock(barId, productId, quantity);
                    } catch (ValidationException e) {
                        System.err.printf("Could not add stock for bar %s and product %s: %s%n",
                                remoteBar.getBarName(), name, e.getMessage());
                    }
                }
                log.info("stock items for bar {} created successfully.", remoteBar.getBarName());
            }
        }
    }


    public static UUID int2UUID(int id) {
        return UUID.nameUUIDFromBytes(("int-" + id).getBytes(StandardCharsets.UTF_8));
    }
}
