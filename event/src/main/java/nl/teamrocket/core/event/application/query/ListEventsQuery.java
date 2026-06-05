package nl.teamrocket.core.event.application.query;

import nl.teamrocket.core.event.domain.model.EventStatus;

import java.util.UUID;

/**
 * Filter-query voor lijst-endpoint. Alle velden optioneel.
 */
public record ListEventsQuery(
        EventStatus status,
        UUID organizerId
) {
    public static ListEventsQuery all() {
        return new ListEventsQuery(null, null);
    }
}
