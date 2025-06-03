package de.fhdo.dropPointsSys.controllers.droppoint;

import de.fhdo.dropPointsSys.converters.DropPointConverter;
import de.fhdo.dropPointsSys.dto.DropPointDto;
import de.fhdo.dropPointsSys.service.DropPointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/droppoints")
public class DropPointRestController {

    private final DropPointService dropPointService;


    public DropPointRestController(DropPointService dropPointService) {
        this.dropPointService = dropPointService;
    }

    // Get all droppoint
    @GetMapping
    public ResponseEntity<List<DropPointDto>> getAllDropPoints() {
        List<DropPointDto> userDtos = dropPointService.get_all_drop_points()
                .stream().map(DropPointConverter::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }

    // Get droppoint by ID
    @GetMapping("/{id}")
    public ResponseEntity<DropPointDto> getIDropPointById(@PathVariable Long id) {

        return dropPointService.get_drop_point(id)
                .map(DropPointConverter::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }


}
