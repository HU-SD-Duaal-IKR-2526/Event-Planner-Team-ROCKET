package nl.teamrocket.core.event.adapter.publisher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.event.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.core.event.domain.event.EventDomainEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Publiceert domain events naar RabbitMQ (exchange: event.events, topic).
 *
 * Consumers: Registration (q.registration.event-sync, binding event.#),
 * Schedule (q.schedule.events, binding event.#) en Notification
 * (q.notification.event.updated / q.notification.event.cancelled).
 *
 * Payload: JSON-string met alle velden van het domain event, aangevuld met
 * "eventType" (routing key zonder versie-suffix, bv. "event.published" —
 * dit is wat de EventSyncListener van Registration en de
 * ScheduleProjectionListener van Schedule verwachten) en "messageId"
 * (voor idempotentie bij de consumers).
 *
 * NB: dit is een directe publish, geen transactional outbox zoals het
 * communicatie-ontwerp (§6.4) uiteindelijk voorschrijft. Een publish-fout
 * wordt gelogd maar breekt de business-operatie niet.
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class RabbitMqDomainEventPublisher implements DomainEventPublisher {

    public static final String EVENTS_EXCHANGE = "event.events";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(EventDomainEvent event) {
        String routingKey = event.routingKey();
        String messageId = UUID.randomUUID().toString();
        try {
            Map<String, Object> payload =
                    objectMapper.convertValue(event, new TypeReference<Map<String, Object>>() {});
            payload.put("eventType", stripVersionSuffix(routingKey));
            payload.put("messageId", messageId);

            String json = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(EVENTS_EXCHANGE, routingKey, json, message -> {
                message.getMessageProperties().setMessageId(messageId);
                return message;
            });
            log.info("[domain-event] published routingKey={} eventId={} messageId={}",
                    routingKey, event.eventId(), messageId);
        } catch (Exception e) {
            log.error("[domain-event] publish FAILED routingKey={} eventId={}: {}",
                    routingKey, event.eventId(), e.getMessage(), e);
        }
    }

    /** "event.published.v1" -> "event.published" */
    private static String stripVersionSuffix(String routingKey) {
        return routingKey.replaceAll("\\.v\\d+$", "");
    }
}
