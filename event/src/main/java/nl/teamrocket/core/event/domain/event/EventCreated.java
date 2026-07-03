package nl.teamrocket.core.event.domain.event;

import java.time.Instant;
import java.util.UUID;

public record EventCreated(
        UUID eventId,
        UUID organizerId,
        UUID venueId,
        String title,
        Instant startsAt,
        Instant endsAt,
        Instant occurredAt
) implements EventDomainEvent {

    @Override public String routingKey() { return "event.created.v1"; }
}
