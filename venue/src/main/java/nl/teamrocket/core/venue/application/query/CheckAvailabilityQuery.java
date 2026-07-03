package nl.teamrocket.core.venue.application.query;

import java.time.Instant;
import java.util.UUID;

/**
 * Beantwoordt of een venue in een tijdslot beschikbaar is. Communicatiedoc §3.2.2:
 * {@code GET /venues/{id}/availability?from&to}. Het Event BC roept dit
 * synchroon aan vóór het publiceren van een event.
 */
public record CheckAvailabilityQuery(UUID venueId, Instant from, Instant to) {

    public CheckAvailabilityQuery {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from en to zijn verplicht");
        }
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("to moet na from liggen");
        }
    }
}
