package nl.teamrocket.identity.adapter.amqp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.identity.security.CorrelationContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Secondary adapter: publishes domain events to RabbitMQ.
 * Exchange: ep.events (topic), routing key provided by caller.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqDomainEventPublisher implements DomainEventPublisher {

    private static final String EVENTS_EXCHANGE = "ep.events";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String routingKey, Object event) {
        String messageId = UUID.randomUUID().toString();
        String correlationId = CorrelationContext.current();

        rabbitTemplate.convertAndSend(EVENTS_EXCHANGE, routingKey, event, message -> {
            message.getMessageProperties().setMessageId(messageId);
            message.getMessageProperties().setCorrelationId(correlationId);
            return message;
        });

        log.debug("Published event '{}' messageId={} correlationId={}",
                routingKey, messageId, correlationId);
    }
}
