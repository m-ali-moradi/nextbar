package de.fhdo.dropPointsSys.dto;

/**
 * RemoteDropPoint is a simple DTO representing a drop point with its ID, associated event ID,
 * location, and capacity. It is used for transferring drop point data between services or layers.
 */
public class RemoteDropPoint {
    public Long dropPointId;
    public Long eventId;
    public String location;
    public int capacity;
}
