package nl.hu.ikr.notification.infrastructure.messaging;

import nl.hu.ikr.notification.application.NotificationService;
import nl.hu.ikr.notification.domain.NotificationType;
import nl.hu.ikr.notification.infrastructure.redis.IdempotencyGate;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class EventUpdatedConsumer {

    private final NotificationService notificationService;
    private final IdempotencyGate gate;

    public EventUpdatedConsumer(
            NotificationService notificationService,
            IdempotencyGate gate
    ) {
        this.notificationService = notificationService;
        this.gate = gate;
    }

    @RabbitListener(queues = "q.notification.event.updated")
    public void consume(
            EventUpdatedEvent event,
            @Header(name = "amqp_messageId", required = false)
            String messageId
    ) {

        if (messageId != null && !gate.claim(messageId)) {
            return;
        }

        notificationService.createNotification(
                event.userId(),
                "Event Updated",
                "The event '" + event.eventName() + "' has been updated.",
                NotificationType.EVENT_UPDATED
        );
    }
}
