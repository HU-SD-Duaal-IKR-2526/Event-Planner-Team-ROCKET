package nl.teamrocket.core.event.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker-interface voor alle domain events uit het Event-aggregate.
 * De application layer pakt deze op via {@link nl.teamrocket.core.event.domain.model.Event#pullDomainEvents()}
 * en publiceert ze via de {@code DomainEventPublisher} outbound port.
 *
 * Routing keys voor de RabbitMQ topic exchange (communicatiedoc §3.2.1 / §5.2):
 *   event.created.v1, event.published.v1, event.updated.v1, event.cancelled.v1, event.completed.v1
 */
public interface EventDomainEvent {
    UUID eventId();
    Instant occurredAt();
    String routingKey();
}
