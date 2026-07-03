package nl.teamrocket.core.userprofile.application.port.outbound;
public interface DomainEventPublisher {
    void publish(String routingKey, Object event);
}
