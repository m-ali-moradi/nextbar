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
                new DefineBeverage(1L, "Coke", 200L),
                new DefineBeverage(2L, "Sprite", 250L),
                new DefineBeverage(3L, "Water", 100L),
                new DefineBeverage(4L, "Beer", 300L),
                new DefineBeverage(5L, "Juice", 275L)
        );
    }
}
