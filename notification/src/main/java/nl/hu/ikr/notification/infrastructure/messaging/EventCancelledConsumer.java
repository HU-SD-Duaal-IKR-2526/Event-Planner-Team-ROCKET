package nl.hu.ikr.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hu.ikr.notification.application.NotificationService;
import nl.hu.ikr.notification.domain.NotificationType;
import nl.hu.ikr.notification.infrastructure.redis.IdempotencyGate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumeert event.cancelled.v1 van de Event BC (JSON-string payload).
 *
 * BEKENDE BEPERKING: de Event BC stuurt geen userId mee (een annulering
 * raakt álle deelnemers). Zolang de fan-out via de Registration BC niet is
 * gebouwd, wordt een bericht zonder userId alleen gelogd.
 */
@Component
public class EventCancelledConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventCancelledConsumer.class);

    private final NotificationService notificationService;
    private final IdempotencyGate gate;
    private final ObjectMapper objectMapper;

    public EventCancelledConsumer(
            NotificationService notificationService,
            IdempotencyGate gate,
            ObjectMapper objectMapper
    ) {
        this.notificationService = notificationService;
        this.gate = gate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitConfiguration.EVENT_CANCELLED_QUEUE)
    public void consume(
            String message,
            @Header(name = "amqp_messageId", required = false)
            String messageId
    ) {
        try {
            EventCancelledEvent event = objectMapper.readValue(message, EventCancelledEvent.class);

            String idempotencyKey = messageId != null ? messageId
                    : event.messageId() != null ? event.messageId()
                    : "event.cancelled:" + event.eventId();
            if (!gate.claim(idempotencyKey)) {
                return;
            }

            if (event.userId() == null) {
                log.info("event.cancelled ontvangen voor event {} — geen userId in payload, "
                        + "notificatie vereist deelnemer fan-out (nog niet geimplementeerd)",
                        event.eventId());
                return;
            }

            notificationService.createNotification(
                    event.userId(),
                    "Event Cancelled",
                    "The event '" + event.eventId() + "' has been cancelled.",
                    NotificationType.EVENT_CANCELLED
            );
        } catch (Exception e) {
            log.error("Fout bij verwerken event.cancelled bericht: {}", e.getMessage(), e);
        }
    }
}
