package nl.teamrocket.core.event.domain.event;

import nl.teamrocket.core.event.domain.model.Visibility;

import java.time.Instant;
import java.util.UUID;

/**
 * Communicatiedoc §3.2.1: EventPublished fan-out event.
 * Consumers: Registration (opent inschrijving), Schedule (bouwt tijdlijn),
 * Notification (kondigt aan), Audit (log).
 */
public record EventPublished(
        UUID eventId,
        UUID organizerId,
        UUID venueId,
        String title,
        String description,
        Instant startsAt,
        Instant endsAt,
        Integer capacity,
        Visibility visibility,
        Instant publishedAt,
        Instant occurredAt
) implements EventDomainEvent {

    @Override public String routingKey() { return "event.published.v1"; }
}
