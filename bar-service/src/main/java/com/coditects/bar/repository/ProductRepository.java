package com.coditects.bar.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coditects.bar.model.Product;

/**
 * Repository interface for managing Product entities.
 * This interface extends JpaRepository to provide CRUD operations for Product entities.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
}
