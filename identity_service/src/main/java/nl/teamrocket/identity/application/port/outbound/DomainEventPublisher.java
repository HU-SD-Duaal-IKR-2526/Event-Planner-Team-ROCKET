package nl.teamrocket.identity.application.port.outbound;

/**
 * Outbound port: publish domain events to the message broker.
 * The application layer uses this; the RabbitMQ adapter implements it.
 */
public interface DomainEventPublisher {

    /**
     * Publish a domain event with the given routing key.
     * @param routingKey e.g. "account.registered.v1"
     * @param event      the event payload (will be serialised to JSON)
     */
    void publish(String routingKey, Object event);
}
