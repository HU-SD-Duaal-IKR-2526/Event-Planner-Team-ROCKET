package nl.hu.ikr.notification.infrastructure.messaging;

import java.util.UUID;

/**
 * Payload van event.cancelled.v1 (Event BC, exchange event.events).
 * Bevat geen titel: het domain event van de Event BC stuurt alleen
 * eventId en reason mee. userId is optioneel (zie EventUpdatedEvent).
 */
public record EventCancelledEvent(
        UUID eventId,
        UUID userId,
        String reason,
        String messageId
) {
}
