package nl.teamrocket.core.userprofile.adapter.amqp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.userprofile.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.core.shared.CorrelationContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j @Component @RequiredArgsConstructor
public class RabbitMqDomainEventPublisher implements DomainEventPublisher {
    private static final String EVENTS_EXCHANGE = "ep.events";
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String routingKey, Object event) {
        String messageId = UUID.randomUUID().toString();
        String correlationId = CorrelationContext.current();
        rabbitTemplate.convertAndSend(EVENTS_EXCHANGE, routingKey, event, msg -> {
            msg.getMessageProperties().setMessageId(messageId);
            msg.getMessageProperties().setCorrelationId(correlationId);
            return msg;
        });
        log.debug("Published '{}' messageId={}", routingKey, messageId);
    }
}
