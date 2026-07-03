package nl.teamrocket.core.event.domain.exception;

import nl.teamrocket.core.event.domain.model.EventStatus;

public class InvalidEventStatusTransitionException extends RuntimeException {
    public InvalidEventStatusTransitionException(EventStatus from, EventStatus to) {
        super("Ongeldige status-transitie: " + from + " → " + to);
    }
}
