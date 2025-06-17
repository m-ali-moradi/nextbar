package de.fhdo.dropPointsSys.repositories;

import de.fhdo.dropPointsSys.domain.DropPoint;
import de.fhdo.dropPointsSys.domain.DropPointStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DropPointRepository extends JpaRepository<DropPoint, Long> {
    List<DropPoint> findAllByStatus(DropPointStatus status);
}
