package de.fhdo.dropPointsSys.service;

import de.fhdo.dropPointsSys.domain.DropPoint;
import de.fhdo.dropPointsSys.domain.DropPointStatus;
import de.fhdo.dropPointsSys.repositories.DropPointRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DropPointService {

    private final DropPointRepository dropPointRepository;

    public DropPointService(DropPointRepository dropPointRepository) {
        this.dropPointRepository = dropPointRepository;
    }

    public Optional<DropPoint> get_drop_point(Long id) {
        return this.dropPointRepository.findById(id);
    }

    public List<DropPoint> get_all_drop_points() {
        return this.dropPointRepository.findAll();
    }

    public DropPoint create_drop_point(DropPoint dropPoint) {
        return this.dropPointRepository.save(dropPoint);
    }

    public Optional<DropPoint> update_drop_point(Long id, DropPoint dropPoint) {
        return this.dropPointRepository.findById(id).map(existing -> {
            existing.setLocation(dropPoint.getLocation());
            existing.setCapacity(dropPoint.getCapacity());
            existing.setStatus(dropPoint.getStatus());
            existing.setCurrent_empties_stock(dropPoint.getCurrent_empties_stock());
            return this.dropPointRepository.save(existing);
        });
    }

    public boolean delete_drop_point(Long id) {
        if (this.dropPointRepository.existsById(id)) {
            this.dropPointRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<DropPoint> get_level_and_status(Long id) {
        return this.dropPointRepository.findById(id);
    }

    public Optional<DropPoint> remove_empties(Long id) {
        return this.dropPointRepository.findById(id).map(existing -> {
            existing.setCurrent_empties_stock(0);
            existing.setStatus(DropPointStatus.EMPTY);
            return this.dropPointRepository.save(existing);
        });
    }

    public Optional<DropPoint> add_empties(Long id) {
        return this.dropPointRepository.findById(id).map(existing -> {
            Integer current_empties_stock = existing.getCurrent_empties_stock();
            Integer max_capacity = existing.getCapacity();

            if (current_empties_stock < max_capacity) {
                existing.setCurrent_empties_stock(current_empties_stock + 1);
            }
            else {
                existing.setStatus(DropPointStatus.FULL);
            }
            return this.dropPointRepository.save(existing);
        });
    }

    public Optional<DropPoint> notify_warehouse(long id) {
        return this.dropPointRepository.findById(id).map(ex -> {
            ex.setStatus(DropPointStatus.FULL_AND_NOTIFIED_TO_WAREHOUSE);
            return this.dropPointRepository.save(ex);
        });

    }

}
