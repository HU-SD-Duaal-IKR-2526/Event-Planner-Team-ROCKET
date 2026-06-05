package nl.teamrocket.core.event.domain.exception;

public class InvalidEventTimeSlotException extends RuntimeException {
    public InvalidEventTimeSlotException(String message) {
        super(message);
    }
}
