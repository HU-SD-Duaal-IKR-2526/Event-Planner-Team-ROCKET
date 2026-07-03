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
 * Consumeert registration.confirmed.v1 van de Registration BC.
 * De Registration service publiceert JSON-strings via haar outbox
 * (zie OutboxPublisher in de registration module), dus hier wordt de
 * payload als String ontvangen en zelf gedeserialiseerd.
 */
@Component
public class RegistrationConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RegistrationConfirmedConsumer.class);

    private final NotificationService notificationService;
    private final IdempotencyGate gate;
    private final ObjectMapper objectMapper;

    public RegistrationConfirmedConsumer(
            NotificationService notificationService,
            IdempotencyGate gate,
            ObjectMapper objectMapper
    ) {
        this.notificationService = notificationService;
        this.gate = gate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitConfiguration.REGISTRATION_QUEUE)
    public void consume(
            String message,
            @Header(name = "amqp_messageId", required = false)
            String messageId
    ) {
        try {
            RegistrationConfirmedEvent event =
                    objectMapper.readValue(message, RegistrationConfirmedEvent.class);

            String idempotencyKey = messageId != null
                    ? messageId
                    : "registration.confirmed:" + event.registrationId();
            if (!gate.claim(idempotencyKey)) {
                return;
            }

            if (event.userId() == null) {
                log.warn("registration.confirmed zonder userId, genegeerd: {}", message);
                return;
            }

            notificationService.createNotification(
                    event.userId(),
                    "Registration Confirmed",
                    "Your registration has been confirmed.",
                    NotificationType.REGISTRATION_CONFIRMED
            );
        } catch (Exception e) {
            // Niet opnieuw gooien — voorkomt een oneindige requeue-loop
            log.error("Fout bij verwerken registration.confirmed bericht: {}", e.getMessage(), e);
        }
    }
}
