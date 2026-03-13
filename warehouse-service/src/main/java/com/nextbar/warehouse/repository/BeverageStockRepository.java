package com.nextbar.warehouse.repository;

import com.nextbar.warehouse.model.entity.BeverageStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Repository for beverage stock management.
 */
@Repository
public interface BeverageStockRepository extends JpaRepository<BeverageStock, Long> {

    /**
     * Find stock by beverage type (case-insensitive).
     */
    Optional<BeverageStock> findByBeverageTypeIgnoreCase(String beverageType);

    /**
     * Find stock by beverage type with pessimistic lock for updates.
     * Use this when deducting stock to prevent race conditions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BeverageStock b WHERE LOWER(b.beverageType) = LOWER(:type)")
    Optional<BeverageStock> findByBeverageTypeForUpdate(@Param("type") String beverageType);

    /**
     * Check if beverage type already exists.
     */
    boolean existsByBeverageTypeIgnoreCase(String beverageType);

    /**
     * Find all beverages with low stock.
     */
    @Query("SELECT b FROM BeverageStock b WHERE b.quantity <= b.minStockLevel")
    List<BeverageStock> findLowStockItems();

    /**
     * Find beverages with quantity below a threshold.
     */
    @Query("SELECT b FROM BeverageStock b WHERE b.quantity < :threshold ORDER BY b.quantity ASC")
    List<BeverageStock> findByQuantityLessThan(@Param("threshold") int threshold);
}
