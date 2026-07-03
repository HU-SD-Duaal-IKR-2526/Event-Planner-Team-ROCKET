package nl.teamrocket.core.event.application.command;

import nl.teamrocket.core.event.domain.model.Visibility;

import java.time.Instant;
import java.util.UUID;

public record CreateEventCommand(
        UUID organizerId,
        UUID venueId,
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
