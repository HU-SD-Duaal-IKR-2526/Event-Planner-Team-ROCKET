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
 * Consumeert event.updated.v1 van de Event BC (JSON-string payload).
 *
 * BEKENDE BEPERKING: de Event BC stuurt geen userId mee (een event-update
 * raakt álle deelnemers). Zolang de fan-out via de Registration BC niet is
 * gebouwd, wordt een bericht zonder userId alleen gelogd.
 */
@Component
public class EventUpdatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventUpdatedConsumer.class);

    private final NotificationService notificationService;
    private final IdempotencyGate gate;
    private final ObjectMapper objectMapper;

    public EventUpdatedConsumer(
            NotificationService notificationService,
            IdempotencyGate gate,
            ObjectMapper objectMapper
    ) {
        this.notificationService = notificationService;
        this.gate = gate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitConfiguration.EVENT_UPDATED_QUEUE)
    public void consume(
            String message,
            @Header(name = "amqp_messageId", required = false)
            String messageId
    ) {
        try {
            EventUpdatedEvent event = objectMapper.readValue(message, EventUpdatedEvent.class);

            String idempotencyKey = messageId != null ? messageId
                    : event.messageId() != null ? event.messageId()
                    : "event.updated:" + event.eventId();
            if (!gate.claim(idempotencyKey)) {
                return;
            }

            if (event.userId() == null) {
                log.info("event.updated ontvangen voor event '{}' — geen userId in payload, "
                        + "notificatie vereist deelnemer fan-out (nog niet geimplementeerd)",
                        event.displayName());
                return;
            }

            notificationService.createNotification(
                    event.userId(),
                    "Event Updated",
                    "The event '" + event.displayName() + "' has been updated.",
                    NotificationType.EVENT_UPDATED
            );
        } catch (Exception e) {
            log.error("Fout bij verwerken event.updated bericht: {}", e.getMessage(), e);
        }
    }
}
