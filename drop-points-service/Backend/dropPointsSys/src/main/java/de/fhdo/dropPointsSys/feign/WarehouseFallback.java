package de.fhdo.dropPointsSys.feign;

import de.fhdo.dropPointsSys.domain.DropPointStatus;
import de.fhdo.dropPointsSys.dto.DropPointDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WarehouseFallback implements WarehouseClient {

    private final List<DropPointDto> dummyDropPoints = Arrays.asList(
            new DropPointDto(1L, "ErdGeschoss From Allen", 100, 0, DropPointStatus.ACCEPTED),
            new DropPointDto(2L, "North Gate", 100, 0, DropPointStatus.ACCEPTED),
            new DropPointDto(3L, "South Exit", 80, 0, DropPointStatus.ACCEPTED),
            new DropPointDto(4L, "East Wing", 60, 0, DropPointStatus.ACCEPTED),
            new DropPointDto(8L, "2 Stock", 50, 0, DropPointStatus.ACCEPTED)
    );

    @Override
    public DropPointDto status(Long id) {
        return dummyDropPoints.stream()
                .filter(dp -> dp.getId().equals(id))
                .findFirst()
                .orElse(null); // or return a default DropPointDto if you prefer
    }
}
