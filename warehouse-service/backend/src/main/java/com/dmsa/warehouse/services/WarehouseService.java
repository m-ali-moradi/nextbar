package com.dmsa.warehouse.services;

import com.dmsa.warehouse.model.BeverageStock;
import com.dmsa.warehouse.model.EmptyBottleStock;
import com.dmsa.warehouse.repository.BeverageStockRepository;
import com.dmsa.warehouse.repository.EmptyBottleStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class WarehouseService {

    @Autowired
    private BeverageStockRepository beverageRepo;

    @Autowired
    private EmptyBottleStockRepository emptyRepo;

    public List<BeverageStock> getAllBeverages() {
        return beverageRepo.findAll();
    }
 //CHECK MATHS
    public void replenish(String beverageType, int quantity) {
        BeverageStock stock = beverageRepo.findByBeverageType(beverageType)
                .orElse(new BeverageStock(beverageType, 0));
        stock.setQuantity(stock.getQuantity() + quantity);
        beverageRepo.save(stock);
    }
/////rename amele
    public void acceptEmpties(int quantity) {
        EmptyBottleStock stock = emptyRepo.findAll().stream().findFirst().orElse(new EmptyBottleStock(0));
        stock.setQuantity(stock.getQuantity() + quantity);
        emptyRepo.save(stock);
    }

    public int getEmptyBottleStock() {
        return emptyRepo.findAll().stream().findFirst().map(EmptyBottleStock::getQuantity).orElse(0);
    }
}