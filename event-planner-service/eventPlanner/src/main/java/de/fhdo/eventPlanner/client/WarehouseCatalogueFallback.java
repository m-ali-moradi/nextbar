package de.fhdo.eventPlanner.client;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class WarehouseCatalogueFallback implements WarehouseCatalogueClient {

    @Override
    public List<RawBeverage> getRawBeverages() {
        return Arrays.asList(
                createBeverage(1L, "Cola", 100L),
                createBeverage(2L, "Water", 150L),
                createBeverage(3L, "Orange Juice", 75L),
                createBeverage(4L, "Lemonade", 60L)
        );
    }

    private RawBeverage createBeverage(Long id, String type, Long quantity) {
        RawBeverage beverage = new RawBeverage();
        beverage.setId(id);
        beverage.setBeverageType(type);
        beverage.setQuantity(quantity);
        return beverage;
    }
}
