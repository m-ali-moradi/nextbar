package de.fhdo.dropPointsSys.controllers.droppoint;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.fhdo.dropPointsSys.converters.DropPointConverter;
import de.fhdo.dropPointsSys.domain.DropPointStatus;
import de.fhdo.dropPointsSys.dto.DropPointDto;
import de.fhdo.dropPointsSys.feign.WarehouseClient;
import de.fhdo.dropPointsSys.service.DropPointService;

@CrossOrigin
@RestController
@RequestMapping("/droppoints")
public class DropPointRestController {

    private static final Logger logger = LoggerFactory.getLogger(DropPointRestController.class);
    private final DropPointService dropPointService;
    private final WarehouseClient warehouseClient;


    public DropPointRestController(DropPointService dropPointService, WarehouseClient warehouseClient) {
        this.dropPointService = dropPointService;
        this.warehouseClient = warehouseClient;
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

    @PostMapping("/create")
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

    // Get verification of transfered empties
    @GetMapping("/verify_transferred_empties/{id}")
    public ResponseEntity<?> verify_transfer(@PathVariable Long id) {
        try {
            var verifiedDropPoint = warehouseClient.status(id);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            logger.debug("Verified drop points (JSON): {}", objectMapper.writeValueAsString(verifiedDropPoint));
            if (verifiedDropPoint == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Transfer of Empties in DropPoint " + id + " was rejected.");
            }

            if (verifiedDropPoint.status == DropPointStatus.ACCEPTED) {
                return dropPointService.remove_empties(id)
                        .map(value -> ResponseEntity.ok(DropPointConverter.toDto(value)))
                        .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build());
            } else {
                String message = "Transfer not verified. Current DropPoint status: " + verifiedDropPoint.status;
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(message);
            }

        } catch (Exception e) {
            logger.error("Error emptying drop point: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fallback triggered due to: " +e.getMessage());


        }
    }

}
