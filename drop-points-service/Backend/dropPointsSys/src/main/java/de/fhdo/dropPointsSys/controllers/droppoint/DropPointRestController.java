package de.fhdo.dropPointsSys.controllers.droppoint;

import de.fhdo.dropPointsSys.converters.DropPointConverter;
import de.fhdo.dropPointsSys.domain.DropPoint;
import de.fhdo.dropPointsSys.dto.DropPointDto;
import de.fhdo.dropPointsSys.service.DropPointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/droppoints")
public class DropPointRestController {

    private final DropPointService dropPointService;


    public DropPointRestController(DropPointService dropPointService) {
        this.dropPointService = dropPointService;
    }

    // Get all DropPoint
    @GetMapping
    public ResponseEntity<List<DropPointDto>> getAllDropPoints() {
        List<DropPointDto> userDtos = dropPointService.get_all_drop_points()
                .stream().map(DropPointConverter::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }

    // Get DropPoint by ID
    @GetMapping("/{id}")
    public ResponseEntity<DropPointDto> getIDropPointById(@PathVariable Long id) {

        return dropPointService.get_drop_point(id)
                .map(DropPointConverter::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    @PostMapping
    public ResponseEntity<DropPointDto> createDropPoint(@RequestBody DropPointDto dropPointDto) {
        var dropPoint = DropPointConverter.toEntity(dropPointDto);
        var savedDropPoint = dropPointService.create_drop_point(dropPoint);
        return new ResponseEntity<>(DropPointConverter.toDto(savedDropPoint), HttpStatus.CREATED);
    }

    // Update DropPoint
    @PutMapping("/{id}")
    public ResponseEntity<DropPointDto> updateDropPoint(@PathVariable Long id, @RequestBody DropPointDto dropPointDto) {
        var dropPoint = DropPointConverter.toEntity(dropPointDto);
        var updatedDropPoint = dropPointService.update_drop_point(id, dropPoint);
        return updatedDropPoint.map(value -> ResponseEntity.ok(DropPointConverter.toDto(value))).orElse(ResponseEntity.notFound().build());
    }

    // Delete DropPoint
    @DeleteMapping("/{id}")
    public ResponseEntity<DropPointDto> deleteDropPoint(@PathVariable Long id) {
        if(dropPointService.delete_drop_point(id)){
            return ResponseEntity.noContent().build();
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get Levels and Status
    @GetMapping("/level_and_status/{id}")
    public ResponseEntity<DropPointDto> get_level_and_status(@PathVariable Long id) {
        return dropPointService.get_level_and_status(id)
                .map(DropPointConverter::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    // remove empties from drop point
    @PutMapping("/remove_empties/{id}")
    public ResponseEntity<DropPointDto> remove_empties_from_droppoint(@PathVariable Long id) {
        var dropPoint = dropPointService.remove_empties(id);
        return  dropPoint.map(value -> ResponseEntity.ok(DropPointConverter.toDto(value))).orElse(ResponseEntity.notFound().build());
    }

    // add empties to a drop point
    @PutMapping("/add_empties/{id}")
    public ResponseEntity<DropPointDto> add_empties_to_droppoint(@PathVariable Long id) {
        var added_empty = dropPointService.add_empties(id);

        return added_empty.map(value -> ResponseEntity.ok(DropPointConverter.toDto(value))).orElse(ResponseEntity.notFound().build());
    }

    // notify warehouse
    @PutMapping("/notify_warehouse/{id}")
    public ResponseEntity<DropPointDto> send_notification(@PathVariable Long id) {
        var dropPoint = dropPointService.notify_warehouse(id);
        return  dropPoint.map(value -> ResponseEntity.ok(DropPointConverter.toDto(value))).orElse(ResponseEntity.notFound().build());

    }

}
