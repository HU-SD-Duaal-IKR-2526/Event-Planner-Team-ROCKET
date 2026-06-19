package nl.hu.ikr.notification.infrastructure.messaging;

import java.util.UUID;

public record EventCancelledEvent(
        UUID eventId,
        UUID userId,
        String eventName
) {
}