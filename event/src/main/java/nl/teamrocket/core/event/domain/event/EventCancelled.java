package nl.teamrocket.core.event.domain.event;

import java.time.Instant;
import java.util.UUID;

public record EventCancelled(
        UUID eventId,
        String reason,
        Instant cancelledAt,
        Instant occurredAt
) implements EventDomainEvent {

    @Override public String routingKey() { return "event.cancelled.v1"; }
}
