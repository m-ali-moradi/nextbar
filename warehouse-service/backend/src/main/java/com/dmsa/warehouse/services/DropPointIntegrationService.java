package com.dmsa.warehouse.services;

import com.dmsa.warehouse.dto.DropPointDto;
import com.dmsa.warehouse.feign.DropPointClient;
import com.dmsa.warehouse.model.DropPointRecord;
import com.dmsa.warehouse.repository.DropPointRecordRepository;
import com.dmsa.warehouse.model.DropPointRecord.Status;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DropPointIntegrationService {

    private final DropPointClient dropPointClient;
    private final DropPointRecordRepository dropPointRecordRepository;

    public DropPointIntegrationService(
            DropPointClient dropPointClient,
            DropPointRecordRepository dropPointRecordRepository) {
        this.dropPointClient = dropPointClient;
        this.dropPointRecordRepository = dropPointRecordRepository;
    }

     @Transactional
  public List<DropPointRecord> fetchNotified() {
    List<DropPointDto> all = dropPointClient.getAllDropPoints();
    for (DropPointDto d : all) {
      System.out.println("DropPoint: " + d.getId() + " " + d.getLocation() +" " + d.getCurrentEmpties() + " " + d.getStatus());
    }
    return all.stream()
      .filter(d -> "FULL_AND_NOTIFIED_TO_WAREHOUSE".equals(d.getStatus()))
      .map(d -> {
        DropPointRecord rec = dropPointRecordRepository.findById(d.getId())
          .orElseGet(() -> new DropPointRecord(d.getId()));
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
      .orElseThrow(() -> new IllegalArgumentException("Unknown DropPoint "+id));
    if (rec.getStatus() != Status.NOTIFIED) {
      throw new IllegalStateException("Can only accept once");
    }
    rec.setStatus(Status.ACCEPTED);
    rec.setCurrentEmpties(0); // Reset empties count on acceptance
    return dropPointRecordRepository.save(rec);
  }

  @Transactional(readOnly=true)
  public DropPointRecord getStatus(Long id) {
    return dropPointRecordRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Unknown DropPoint "+id));
  }

  @Transactional(readOnly=true)
  public List<DropPointRecord> listNotified() {
    // return dropPointRecordRepository.findByStatus(Status.NOTIFIED);
    return dropPointRecordRepository.findAll();
  }

}
