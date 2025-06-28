package de.fhdo.eventPlanner.client;

import de.fhdo.eventPlanner.model.DefineBeverage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.stream.Collectors;

//Feign client for the external Warehouse Catalogue microservice.
@FeignClient(name = "warehouse-service", url = "http://localhost:8085")
public interface WarehouseCatalogueClient {


    //Fetch the full list of beverages (id, beverageType, quantity).
    @GetMapping("/warehouse/stock")
    List<RawBeverage> getRawBeverages();

    /**
     * Convert raw stock into DefineBeverage objects (id, name, price).
     */
    default List<DefineBeverage> getAllBeverages() {
        return getRawBeverages().stream()
                .map(rb -> {
                    DefineBeverage d = new DefineBeverage();
                    d.setId(rb.getId());
                    d.setName(rb.getBeverageType());  // map beverageType → name
                    d.setPrice(rb.getQuantity());     // map quantity → price
                    return d;
                })
                .collect(Collectors.toList());
    }

    // Raw DTO matching the API's JSON structure.
    class RawBeverage {
        private Long id;
        private String beverageType;
        private Long quantity;

        public RawBeverage() { }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getBeverageType() { return beverageType; }
        public void setBeverageType(String beverageType) { this.beverageType = beverageType; }

        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }
    }

}
