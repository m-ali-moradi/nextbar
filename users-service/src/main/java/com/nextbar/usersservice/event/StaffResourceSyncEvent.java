package com.nextbar.usersservice.event;

import java.time.Instant;
import java.util.List;

public class StaffResourceSyncEvent {

    private Long eventId;
    private String eventName;
    private String action;
    private List<StaffResourceAssignment> assignments;
    private Instant timestamp;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<StaffResourceAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<StaffResourceAssignment> assignments) {
        this.assignments = assignments;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static class StaffResourceAssignment {
        private String staffId;
        private String serviceCode;
        private Long resourceId;

        public String getStaffId() {
            return staffId;
        }

        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }

        public String getServiceCode() {
            return serviceCode;
        }

        public void setServiceCode(String serviceCode) {
            this.serviceCode = serviceCode;
        }

        public Long getResourceId() {
            return resourceId;
        }

        public void setResourceId(Long resourceId) {
            this.resourceId = resourceId;
        }
    }
}
