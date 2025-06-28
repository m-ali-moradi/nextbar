package com.dmsa.warehouse.services;

import com.dmsa.warehouse.model.BeverageStock;
import com.dmsa.warehouse.model.EmptyBottleStock;
import com.dmsa.warehouse.repository.BeverageStockRepository;
import com.dmsa.warehouse.repository.DropPointRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarehouseService {

    @Autowired
    private BeverageStockRepository beverageRepo;

    @Autowired
    private DropPointRecordRepository emptyRepo;

    public WarehouseService(BeverageStockRepository beverageRepo) {
        this.beverageRepo = beverageRepo;
    }

    public List<BeverageStock> getAllBeverages() {
        return beverageRepo.findAll();
    }

    public int getAvailableStock(String beverageType) {
        return beverageRepo
                .findByBeverageTypeIgnoreCase(beverageType)
                .map(BeverageStock::getQuantity)
                .orElse(0);
    }

    public void replenish(String beverageType, int amount) {
        BeverageStock stock = beverageRepo
                .findByBeverageTypeIgnoreCase(beverageType)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No stock record for beverage: " + beverageType));
        int newQty = Math.max(0, stock.getQuantity() - amount);
        stock.setQuantity(newQty);
        beverageRepo.save(stock);
    }

    @Transactional
    public BeverageStock addBeverage(String beverageType, Integer quantity) {
        BeverageStock b = new BeverageStock(beverageType, quantity);
        return beverageRepo.save(b);
    }

    @Transactional
    public void deleteBeverage(Long id) {
        beverageRepo.deleteById(id);
    }

}