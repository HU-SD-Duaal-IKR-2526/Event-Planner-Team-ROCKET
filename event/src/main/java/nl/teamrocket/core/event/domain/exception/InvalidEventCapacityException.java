package nl.teamrocket.core.event.domain.exception;

public class InvalidEventCapacityException extends RuntimeException {
    public InvalidEventCapacityException(int capacity) {
        super("Capacity moet groter zijn dan 0, was: " + capacity);
    }
}
