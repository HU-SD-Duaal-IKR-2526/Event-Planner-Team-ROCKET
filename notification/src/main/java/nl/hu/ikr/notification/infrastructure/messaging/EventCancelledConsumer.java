package nl.hu.ikr.notification.infrastructure.messaging;

import nl.hu.ikr.notification.application.NotificationService;
import nl.hu.ikr.notification.domain.NotificationType;
import nl.hu.ikr.notification.infrastructure.redis.IdempotencyGate;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class EventCancelledConsumer {

    private final NotificationService notificationService;
    private final IdempotencyGate gate;

    public EventCancelledConsumer(
            NotificationService notificationService,
            IdempotencyGate gate
    ) {
        this.notificationService = notificationService;
        this.gate = gate;
    }

    @RabbitListener(queues = "q.notification.event.cancelled")
    public void consume(
            EventCancelledEvent event,
            @Header(name = "amqp_messageId", required = false)
            String messageId
    ) {

        if (messageId != null && !gate.claim(messageId)) {
            return;
        }

        notificationService.createNotification(
                event.userId(),
                "Event Cancelled",
                "The event '" + event.eventName() + "' has been cancelled.",
                NotificationType.EVENT_CANCELLED
        );
    }
}