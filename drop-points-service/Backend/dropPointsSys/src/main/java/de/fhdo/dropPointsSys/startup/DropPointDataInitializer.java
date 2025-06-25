package de.fhdo.dropPointsSys.startup;

import de.fhdo.dropPointsSys.domain.DropPoint;
import de.fhdo.dropPointsSys.dto.DropPointDto;
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

    public DropPointDataInitializer(@Qualifier("de.fhdo.dropPointsSys.feign.EventPlannerClient") EventPlannerClient eventPlannerClient, DropPointService dropPointService) {
        this.eventPlannerClient = eventPlannerClient;
        this.dropPointService = dropPointService;
    }

    @Override
    public void run(ApplicationArguments args) {

        System.out.println("Initializing drop point data from eventplanner...");
        try {
            List<DropPointDto> dropPoints = eventPlannerClient.getDropPoints();
            for (DropPointDto remoteDropPoint : dropPoints) {
                System.out.println("dropPoints "+dropPoints);
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
            System.err.println("Error initializing bar data from eventplanner: " + e.getMessage());
        }
    }
}