package nl.teamrocket.core.event.application.port.outbound;

import nl.teamrocket.core.event.domain.event.EventDomainEvent;

/**
 * Outbound port: publicatie van domain events.
 *
 * In de doelarchitectuur (communicatiedoc §6.1 / §6.4 + data-distributiedoc §5.3)
 * gaat dit via een transactional outbox + RabbitMQ topic exchange. In deze fase is
 * de RabbitMQ-koppeling buiten scope; de in-memory adapter logt en houdt events
 * vast voor inspectie/tests. Door dezelfde port te gebruiken is later swappen
 * (adapter vervangen i.p.v. application code wijzigen) een no-op voor het domein.
 */
public interface DomainEventPublisher {
    void publish(EventDomainEvent event);
}
