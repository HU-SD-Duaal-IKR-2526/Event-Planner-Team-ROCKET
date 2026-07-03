package nl.teamrocket.core.event.application.command;

import nl.teamrocket.core.event.domain.model.Visibility;

import java.time.Instant;
import java.util.UUID;

/**
 * Update-command. Null-velden worden niet gewijzigd (partial update).
 */
public record UpdateEventCommand(
        UUID eventId,
        String title,
        String description,
        Instant startsAt,
        Instant endsAt,
        Integer capacity,
        String dressCode,
        String speaker,
        String livestreamUrl,
        Visibility visibility
) {}
