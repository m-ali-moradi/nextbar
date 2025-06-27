package com.dmsa.warehouse.repository;

import com.dmsa.warehouse.model.DropPointRecord;
import com.dmsa.warehouse.model.DropPointRecord.Status;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DropPointRecordRepository
        extends JpaRepository<DropPointRecord, Long> {
    List<DropPointRecord> findByStatus(Status status);
}