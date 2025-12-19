package com.dmsa.warehouse.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dmsa.warehouse.dto.DropPointDto;
import com.dmsa.warehouse.feign.DropPointClient;
import com.dmsa.warehouse.model.DropPointRecord;
import com.dmsa.warehouse.model.DropPointRecord.Status;
import com.dmsa.warehouse.model.EmptyBottleStock;
import com.dmsa.warehouse.repository.DropPointRecordRepository;
import com.dmsa.warehouse.repository.EmptyBottleStockRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class DropPointIntegrationService {

  private final DropPointClient dropPointClient;
  private final DropPointRecordRepository dropPointRecordRepository;
  private final EmptyBottleStockRepository emptyBottleStockRepo;

  public DropPointIntegrationService(
      DropPointClient dropPointClient,
      DropPointRecordRepository dropPointRecordRepository, EmptyBottleStockRepository emptyBottleStockRepo) {
    this.dropPointClient = dropPointClient;
    this.dropPointRecordRepository = dropPointRecordRepository;
    this.emptyBottleStockRepo = emptyBottleStockRepo;
  }

  @Transactional
  @CircuitBreaker(name = "dropPointService", fallbackMethod = "fetchNotifiedFallback")
  public List<DropPointRecord> fetchNotified() {
    List<DropPointDto> all = dropPointClient.getAllDropPoints();
    return all.stream()
        .filter(d -> "FULL_AND_NOTIFIED_TO_WAREHOUSE".equals(d.getStatus()) || "EMPTY".equals(d.getStatus()))
        .map(d -> {
          DropPointRecord rec = dropPointRecordRepository.findById(d.getId())
              .orElseGet(() -> new DropPointRecord(d.getId()));

              if (rec.getStatus() == DropPointRecord.Status.ACCEPTED && d.getStatus().equals("EMPTY")) {
            rec.setStatus(DropPointRecord.Status.STATUS_RESET);
            rec.setCurrentEmpties(0);
            dropPointRecordRepository.save(rec);
            return null;
          }else if(d.getStatus().equals("EMPTY")){
            return null;
          }
          if (rec.getStatus() == DropPointRecord.Status.ACCEPTED) {
            return rec;
          }
          rec.setLocation(d.getLocation());
          rec.setCurrentEmpties(d.getCurrentEmpties());
          rec.setStatus(Status.NOTIFIED);
          return dropPointRecordRepository.save(rec);
        })
        .collect(Collectors.toList());
  }

  @Transactional
  public DropPointRecord accept(Long id) {
    DropPointRecord rec = dropPointRecordRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Unknown DropPoint " + id));
    if (rec.getStatus() != Status.NOTIFIED) {
      throw new IllegalStateException("Can only accept once");
    }
    int acceptedCount = rec.getCurrentEmpties();

    rec.setStatus(Status.ACCEPTED);
    rec.setCurrentEmpties(0); // Reset empties count on acceptance
    EmptyBottleStock stock = emptyBottleStockRepo
        .findByDropPointId(id)
        .orElseGet(() -> {
          EmptyBottleStock e = new EmptyBottleStock();
          e.setDropPointId(id);
          e.setDropPointLocation(rec.getLocation());
          e.setQuantity(0);
          return e;
        });

    stock.setQuantity(stock.getQuantity() + acceptedCount);
    emptyBottleStockRepo.save(stock);
    return dropPointRecordRepository.save(rec);
  }

  @Transactional(readOnly = true)
  public DropPointRecord getStatus(Long id) {
    return dropPointRecordRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Unknown DropPoint " + id));
  }

  @Transactional(readOnly = true)
  public List<DropPointRecord> listNotified() {
    // return dropPointRecordRepository.findByStatus(Status.NOTIFIED);
    return dropPointRecordRepository.findAll();
  }

  @SuppressWarnings("unused")
  private List<DropPointRecord> fetchNotifiedFallback(Throwable t) {
    System.err.println("fetchNotified() failed – returning dummy record: " + t.toString());
    DropPointRecord dummy = new DropPointRecord(-1L);
    dummy.setLocation("Location1");
    dummy.setCurrentEmpties(0);
    dummy.setStatus(Status.NOTIFIED);
    return List.of(dummy);
  }

}
