package nl.hu.ikr.notification.infrastructure.messaging;

import java.util.UUID;

/**
 * Payload van event.updated.v1 (Event BC, exchange event.events).
 * userId is optioneel: de Event BC kent geen individuele ontvangers;
 * fan-out naar deelnemers vereist registratiedata (nog niet geimplementeerd).
 */
public record EventUpdatedEvent(
        UUID eventId,
        UUID userId,
        String title,
        String messageId
) {
    /** Weergavenaam van het event, of het id als er geen titel in de payload zit. */
    public String displayName() {
        return title != null ? title : String.valueOf(eventId);
    }
}
