package com.nextbar.bar.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextbar.bar.model.EventBarAssociation;

/**
 * Repository for managing associations between events and bars.
 */
@Repository
public interface EventBarAssociationRepository extends JpaRepository<EventBarAssociation, UUID> {

    boolean existsByEventIdAndBarId(Long eventId, UUID barId);

    List<EventBarAssociation> findByEventId(Long eventId);

    Optional<EventBarAssociation> findTopByBarIdOrderByCreatedAtDesc(UUID barId);

    void deleteByEventId(Long eventId);
}
