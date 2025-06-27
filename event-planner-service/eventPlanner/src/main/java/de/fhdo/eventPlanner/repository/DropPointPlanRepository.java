package de.fhdo.eventPlanner.repository;

import de.fhdo.eventPlanner.model.DropPointPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DropPointPlanRepository extends JpaRepository<DropPointPlan, Long> {
    // Additional queries if needed
}
