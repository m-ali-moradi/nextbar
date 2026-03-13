package com.nextbar.dropPoint.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextbar.dropPoint.domain.EventDroppointAssociation;

/**
 * Repository for managing associations between events and drop points.
 */

@Repository
public interface EventDroppointAssociationRepository extends JpaRepository<EventDroppointAssociation, UUID> {

    boolean existsByEventIdAndDropPointId(Long eventId, Long dropPointId);

    List<EventDroppointAssociation> findByEventId(Long eventId);

    java.util.Optional<EventDroppointAssociation> findTopByDropPointIdOrderByCreatedAtDesc(Long dropPointId);

    void deleteByEventId(Long eventId);
}
