package de.fhdo.dropPointsSys.startup;

import de.fhdo.dropPointsSys.domain.DropPoint;
import de.fhdo.dropPointsSys.dto.DropPointDto;
import de.fhdo.dropPointsSys.feign.EventPlannerClient;
import de.fhdo.dropPointsSys.service.DropPointService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DropPointDataInitializer implements ApplicationRunner {

    private final EventPlannerClient eventPlannerClient;
    private final DropPointService dropPointService;

    public DropPointDataInitializer(@Qualifier("eventPlannerFallback") EventPlannerClient eventPlannerClient, DropPointService dropPointService) {
        this.eventPlannerClient = eventPlannerClient;
        this.dropPointService = dropPointService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            var dropPoints = eventPlannerClient.getDropPoints();
            for (DropPointDto remoteDropPoint : dropPoints) {
                // 1. Register drop point in local DB
                DropPoint newDropPoint = new DropPoint();
                newDropPoint.setId(remoteDropPoint.getId());
                newDropPoint.setLocation(remoteDropPoint.getLocation());
                newDropPoint.setCurrent_empties_stock(remoteDropPoint.getCurrent_empties());
                newDropPoint.setCapacity(remoteDropPoint.getCapacity());
                newDropPoint.setStatus(remoteDropPoint.getStatus());
                DropPoint localDropPoint = dropPointService.create_drop_point(newDropPoint);

                System.out.println("DropPoint "+localDropPoint);

            }
        } catch (Exception e) {
            System.err.println("Fallback triggered due to: " + e.getClass().getName() + " - " + e.getMessage());

        }
    }
}