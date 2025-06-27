package de.fhdo.eventPlanner.repository;

import de.fhdo.eventPlanner.model.BarPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarPlanRepository extends JpaRepository<BarPlan, Long> {
    // Additional queries if needed
}
