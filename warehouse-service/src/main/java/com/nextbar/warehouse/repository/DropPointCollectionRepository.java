package com.dmsa.warehouse.repository;

import com.dmsa.warehouse.model.entity.DropPointCollection;
import com.dmsa.warehouse.model.enums.CollectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for drop point collection records.
 */
@Repository
public interface DropPointCollectionRepository extends JpaRepository<DropPointCollection, Long> {

    /**
     * Find all collections by status.
     */
    List<DropPointCollection> findByStatus(CollectionStatus status);

    /**
     * Find all collections by drop point ID.
     */
    List<DropPointCollection> findByDropPointIdOrderByNotifiedAtDesc(Long dropPointId);

    /**
     * Find pending collection for a drop point.
     */
    @Query("SELECT c FROM DropPointCollection c WHERE c.dropPointId = :dropPointId AND c.status IN :statuses")
    Optional<DropPointCollection> findActiveByDropPointId(
            @Param("dropPointId") Long dropPointId,
            @Param("statuses") List<CollectionStatus> statuses);

    /**
     * Find all pending collections (awaiting acceptance).
     */
    default List<DropPointCollection> findPendingCollections() {
        return findByStatus(CollectionStatus.PENDING);
    }

    /**
     * Find all accepted collections (in progress).
     */
    default List<DropPointCollection> findAcceptedCollections() {
        return findByStatus(CollectionStatus.ACCEPTED);
    }

    /**
     * Check if there's an active collection for a drop point.
     */
    @Query("SELECT COUNT(c) > 0 FROM DropPointCollection c WHERE c.dropPointId = :dropPointId AND c.status IN ('PENDING', 'ACCEPTED')")
    boolean hasActiveCollection(@Param("dropPointId") Long dropPointId);
}
