package nl.teamrocket.core.event.domain.exception;

import java.util.UUID;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(UUID eventId) {
        super("Event niet gevonden: " + eventId);
    }
}
