package nl.teamrocket.core.event.domain.event;

import java.time.Instant;
import java.util.UUID;

public record EventUpdated(
        UUID eventId,
        String title,
        String description,
        Instant startsAt,
        Instant endsAt,
        Integer capacity,
        long version,
        Instant occurredAt
) implements EventDomainEvent {

    @Override public String routingKey() { return "event.updated.v1"; }
}
