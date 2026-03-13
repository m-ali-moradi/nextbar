package com.nextbar.bar.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextbar.bar.model.UsageLog;

/**
 * Repository interface for managing UsageLog entities.
 * This interface extends JpaRepository to provide CRUD operations for UsageLog entities.
 */
@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {
    /**
     * Finds all UsageLog entities associated with a specific bar ID.
     *
     * @param barId the ID of the bar
     * @return a list of UsageLog entities for the specified bar
     */
    List<UsageLog> findByBarId(UUID barId);

    List<UsageLog> findByBarIdAndProductNameIgnoreCase(UUID barId, String productName);

    /**
     * Finds all UsageLog entities for a bar in an inclusive datetime range.
     *
     * @param barId the ID of the bar
     * @param start start datetime (inclusive)
     * @param end   end datetime (inclusive)
     * @return usage logs in range
     */
    List<UsageLog> findByBarIdAndTimestampBetween(UUID barId, LocalDateTime start, LocalDateTime end);

}
