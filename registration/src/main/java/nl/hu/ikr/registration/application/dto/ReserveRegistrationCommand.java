package nl.hu.ikr.registration.application.dto;

import java.util.UUID;

public record ReserveRegistrationCommand(
        UUID eventId,
        UUID userId,
        String idempotencyKey,
        int plusOnes,
        String notes,
        String channel
) {}

