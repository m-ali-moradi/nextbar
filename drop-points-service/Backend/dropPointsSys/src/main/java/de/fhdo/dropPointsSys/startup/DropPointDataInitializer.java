package de.fhdo.dropPointsSys.startup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fhdo.dropPointsSys.domain.DropPoint;
import de.fhdo.dropPointsSys.domain.DropPointStatus;
import de.fhdo.dropPointsSys.dto.DropPointDto;
import de.fhdo.dropPointsSys.dto.RemoteDropPoint;
import de.fhdo.dropPointsSys.feign.EventPlannerClient;
import de.fhdo.dropPointsSys.service.DropPointService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DropPointDataInitializer implements ApplicationRunner {

    private final EventPlannerClient eventPlannerClient;
    private final DropPointService dropPointService;

    public DropPointDataInitializer(EventPlannerClient eventPlannerClient, DropPointService dropPointService) {
        this.eventPlannerClient = eventPlannerClient;
        this.dropPointService = dropPointService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            var rawDropPoints = eventPlannerClient.fetchDropPoints();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            System.out.println("DP (JSON)::\n" + objectMapper.writeValueAsString(rawDropPoints));

            for (Object item : rawDropPoints) {
                DropPointDto dto;

                if (item instanceof DropPointDto) {
                    dto = (DropPointDto) item;
                } else {
                    // Convert RemoteDropPoint to DropPointDto
                    RemoteDropPoint remote = objectMapper.convertValue(item, RemoteDropPoint.class);
                    dto = new DropPointDto(
                            remote.dropPointId,
                            remote.location,
                            remote.capacity,
                            0,
                            DropPointStatus.EMPTY // Default fallback status
                    );
                }

                DropPoint newDropPoint = new DropPoint();
                newDropPoint.setId(dto.id);
                newDropPoint.setLocation(dto.location);
                newDropPoint.setCurrent_empties_stock(dto.current_empties);
                newDropPoint.setCapacity(dto.capacity);
                newDropPoint.setStatus(dto.status);

                DropPoint saved = dropPointService.create_drop_point(newDropPoint);
                System.out.println("Saved DropPoint: " + objectMapper.writeValueAsString(saved));
            }

        } catch (Exception e) {
            System.err.println("Fallback triggered due to: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}