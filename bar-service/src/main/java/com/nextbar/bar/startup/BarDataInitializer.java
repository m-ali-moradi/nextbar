package com.nextbar.bar.startup;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.nextbar.bar.model.Product;
import com.nextbar.bar.model.dto.EventPlannerBarDto;
import com.nextbar.bar.repository.ProductRepository;
import com.nextbar.bar.service.BarService;
import com.nextbar.bar.service.BarStockService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@AllArgsConstructor
@Component
public class BarDataInitializer implements ApplicationRunner {

    private final BarService barService;
    private final ProductRepository productRepository;
    private final BarStockService barStockService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("start fetching bar plans from eventplanner service...");
        List<EventPlannerBarDto> bars = null;

        try {
            bars = null;
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
            UUID fallbackBarId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
            barService.registerBar(fallbackBarId2, "NiceBar", "EF42", 250);

            // Add fallback stock items
            barStockService.addStock(fallbackBarId, UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), 100);
            barStockService.addStock(fallbackBarId, UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"), 50);
            barStockService.addStock(fallbackBarId, UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), 30);
            barStockService.addStock(fallbackBarId, UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"), 20);

            barStockService.addStock(fallbackBarId2, UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), 80);
            barStockService.addStock(fallbackBarId2, UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"), 60);
            barStockService.addStock(fallbackBarId2, UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), 40);  
            barStockService.addStock(fallbackBarId2, UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"), 25);  

            log.info("stock data created successfully.");

        }

    }


    public static UUID int2UUID(int id) {
        return UUID.nameUUIDFromBytes(("int-" + id).getBytes(StandardCharsets.UTF_8));
    }
}
