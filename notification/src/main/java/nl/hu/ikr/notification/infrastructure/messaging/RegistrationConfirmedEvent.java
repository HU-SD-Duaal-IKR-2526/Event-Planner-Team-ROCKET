package nl.hu.ikr.notification.infrastructure.messaging;

import java.util.UUID;

public record RegistrationConfirmedEvent(
        UUID userId,
        UUID registrationId,
        UUID eventId
) {
}
