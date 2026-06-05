package nl.teamrocket.core.event.adapter.publisher;

import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.event.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.core.event.domain.event.EventDomainEvent;
import org.springframework.stereotype.Component;

/**
 * Tijdelijke adapter voor {@link DomainEventPublisher}.
 *
 * AFWIJKING T.O.V. ONTWERP: in het communicatie-ontwerp (§6.4) en data-distributiedoc
 * (§5.3) is hier een transactional outbox + RabbitMQ-publisher voorzien. Deze
 * implementatie logt het event uitsluitend. De RabbitMQ-koppeling valt buiten de
 * scope van deze leveringsfase ("kern: Event aggregate + hexagonal + JPA + REST",
 * zie README §"Afwijkingen van het ontwerp"). Door dezelfde port te gebruiken kan
 * deze adapter later 1:1 vervangen worden door een {@code RabbitOutboxPublisher}
 * zonder application/domain code te wijzigen.
 */
@Slf4j
@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(EventDomainEvent event) {
        log.info("[domain-event] routingKey={} eventId={} occurredAt={} payload={}",
                event.routingKey(), event.eventId(), event.occurredAt(), event);
    }
}
