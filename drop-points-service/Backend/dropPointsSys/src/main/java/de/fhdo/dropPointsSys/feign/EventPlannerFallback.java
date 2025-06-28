package de.fhdo.dropPointsSys.feign;

import de.fhdo.dropPointsSys.dto.DropPointDto;
import de.fhdo.dropPointsSys.domain.DropPointStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EventPlannerFallback implements EventPlannerClient {

    @Override
    public List<DropPointDto> getDropPoints() {
        return Arrays.asList(
                new DropPointDto(2L, "North Gate", 100, 0, DropPointStatus.EMPTY),
                new DropPointDto(3L, "South Exit", 80, 0, DropPointStatus.EMPTY)
        );
    }
}
