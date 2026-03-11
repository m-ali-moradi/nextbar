package de.fhdo.dropPointsSys.domain;

/**
 * Enum representing the status of a DropPoint.
 * - EMPTY: The drop point is empty and ready to receive empties.
 * - FULL: The drop point is full and cannot receive more empties until it is emptied.
 * - NOTIFIED: The drop point has been notified to the warehouse and is awaiting processing.
 * - ACCEPTED: The warehouse has accepted the drop point for processing.
 */
public enum DropPointStatus {
    EMPTY,
    FULL,
    NOTIFIED,
    ACCEPTED
}
