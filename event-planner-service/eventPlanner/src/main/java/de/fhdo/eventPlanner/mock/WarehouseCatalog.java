package de.fhdo.eventPlanner.mock;

import de.fhdo.eventPlanner.model.DefineBeverage;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * A simple “mock” of a Warehouse’s beverage catalog.
 * In reality you’d call Warehouse → /api/beverages, etc.
 * But for now, just a hard‐coded list.
 */
@Component
public class WarehouseCatalog {

    public List<DefineBeverage> getAllBeverages() {
        return Arrays.asList(
                new DefineBeverage(1L, "Coke", 2.50),
                new DefineBeverage(2L, "Sprite", 2.50),
                new DefineBeverage(3L, "Water", 1.00),
                new DefineBeverage(4L, "Beer", 3.00),
                new DefineBeverage(5L, "Juice", 2.75)
        );
    }
}
