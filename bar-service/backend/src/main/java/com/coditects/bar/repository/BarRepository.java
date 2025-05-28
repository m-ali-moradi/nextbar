package com.coditects.bar.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coditects.bar.model.Bar;

/**
 * Repository interface for managing Bar entities.
 * This interface extends JpaRepository to provide CRUD operations for Bar entities.
 */
@Repository
public interface BarRepository extends JpaRepository<Bar, UUID> {
    
}
