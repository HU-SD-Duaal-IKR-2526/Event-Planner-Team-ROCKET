package nl.teamrocket.core.event.adapter.publisher;

import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.event.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.core.event.domain.event.EventDomainEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Log-only adapter voor {@link DomainEventPublisher} — actief in dev/test.
 *
 * In het prod-profiel wordt deze vervangen door
 * {@link RabbitMqDomainEventPublisher}, die de events daadwerkelijk naar de
 * "event.events" exchange publiceert (communicatie-ontwerp §6.4). De
 * transactional outbox uit het ontwerp is nog niet gebouwd; zie README
 * §"Afwijkingen van het ontwerp".
 */
@Slf4j
@Component
@Profile("!prod")
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(EventDomainEvent event) {
        log.info("[domain-event] routingKey={} eventId={} occurredAt={} payload={}",
                event.routingKey(), event.eventId(), event.occurredAt(), event);
    }
}
