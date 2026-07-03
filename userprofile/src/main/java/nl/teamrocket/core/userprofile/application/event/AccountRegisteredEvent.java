package nl.teamrocket.core.userprofile.application.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Inbound event: consumed from RabbitMQ "account.registered.v1" (published by Identity BC).
 * User/Profile module reacts by creating a stub profile for the new account.
 */
public record AccountRegisteredEvent(
        UUID        accountId,
        String      email,
        Set<String> roles,
        Instant     registeredAt,
        String      correlationId
) {}
