package com.nextbar.usersservice.event;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nextbar.usersservice.service.UserService;

@Configuration
public class StaffResourceSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(StaffResourceSyncConsumer.class);

    private final UserService userService;

    public StaffResourceSyncConsumer(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public Consumer<StaffResourceSyncEvent> consumeStaffResourceSync() {
        return event -> {
            if (event == null || event.getAssignments() == null || event.getAssignments().isEmpty()) {
                log.debug("Received empty StaffResourceSyncEvent payload");
                return;
            }

            String action = event.getAction() == null ? "" : event.getAction().trim().toUpperCase(Locale.ROOT);
            List<StaffResourceSyncEvent.StaffResourceAssignment> assignments = event.getAssignments();

            for (StaffResourceSyncEvent.StaffResourceAssignment assignment : assignments) {
                if (assignment == null) {
                    continue;
                }
                if ("ASSIGN".equals(action)) {
                    userService.syncStaffResourceAssignment(
                            assignment.getStaffId(),
                            assignment.getServiceCode(),
                            assignment.getResourceId());
                } else if ("CLEAR".equals(action)) {
                    userService.clearStaffResourceAssignment(
                            assignment.getStaffId(),
                            assignment.getServiceCode(),
                            assignment.getResourceId());
                }
            }

            log.info("Processed StaffResourceSyncEvent eventId={} action={} assignments={}",
                    event.getEventId(), action, assignments.size());
        };
    }
}
