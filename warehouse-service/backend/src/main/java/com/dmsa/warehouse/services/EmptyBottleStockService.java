package com.dmsa.warehouse.services;

import com.dmsa.warehouse.model.EmptyBottleStock;
import com.dmsa.warehouse.repository.EmptyBottleStockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmptyBottleStockService {

  private final EmptyBottleStockRepository repo;

  public EmptyBottleStockService(EmptyBottleStockRepository repo) {
    this.repo = repo;
  }

  public List<EmptyBottleStock> listAll() {
    return repo.findAll();
  }
}
