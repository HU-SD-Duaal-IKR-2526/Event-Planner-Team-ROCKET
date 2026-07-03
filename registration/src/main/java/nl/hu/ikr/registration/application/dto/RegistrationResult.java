package nl.hu.ikr.registration.application.dto;

import nl.hu.ikr.registration.domain.RegistrationStatus;

import java.time.Instant;
import java.util.UUID;

public record RegistrationResult(
        UUID registrationId,
        RegistrationStatus status,
        Integer position,
        Instant createdAt,
        boolean isNew
) {}

