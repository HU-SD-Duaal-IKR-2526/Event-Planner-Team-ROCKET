package nl.teamrocket.core.venue.domain.exception;

public class InvalidCapacityException extends RuntimeException {
    public InvalidCapacityException(int capacity) {
        super("Venue capacity moet > 0, was " + capacity);
    }
}
