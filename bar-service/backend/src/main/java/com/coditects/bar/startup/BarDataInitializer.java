package com.coditects.bar.startup;

import com.coditects.bar.feign.EventPlannerClient;
import com.coditects.bar.model.Bar;
import com.coditects.bar.model.BarStockItem;
import com.coditects.bar.model.Product;
import com.coditects.bar.model.dto.BarDto;
import com.coditects.bar.model.dto.BarStockItemDto;
import com.coditects.bar.repository.ProductRepository;
import com.coditects.bar.service.BarService;
import com.coditects.bar.service.BarStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BarDataInitializer implements ApplicationRunner {

    private final EventPlannerClient eventPlannerClient;
    private final BarService barService;
    private final BarStockService barStockService;
    private final ProductRepository productRepository;

    public BarDataInitializer(EventPlannerClient eventPlannerClient, BarService barService, BarStockService barStockService, ProductRepository productRepository) {
        this.eventPlannerClient = eventPlannerClient;
        this.barService = barService;
        this.barStockService = barStockService;
        this.productRepository = productRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("Loading Products...");
        if (productRepository.count() == 0) {
            productRepository.save(new Product(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), "Fanta"));
            productRepository.save(new Product(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"), "Beer"));
            productRepository.save(new Product(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), "Red Bull"));
            productRepository.save(new Product(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"), "Cola Zero"));
        }
        System.out.println("Initializing bar data from eventplanner...");
        try {
            List<BarDto> bars = eventPlannerClient.getAllBars();
            for (BarDto remoteBar : bars) {
                System.out.println("bars "+bars);
                // 1. Register bar in local DB
                BarDto localBar = barService.registerBar(remoteBar.id(), remoteBar.name(), remoteBar.location(), remoteBar.maxCapacity());

                 // 2. Fetch stock for that bar from eventplanner
                List<BarStockItemDto> stockItems = eventPlannerClient.getStockForBar(remoteBar.id().toString());
                System.out.println("stockItems "+stockItems);
                 // 3. Add each stock item
                for (BarStockItemDto item : stockItems) {
                    barStockService.addStock(item.barId(), item.productId(), item.quantity());
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing bar data from eventplanner: " + e.getMessage());
        }
    }
}
