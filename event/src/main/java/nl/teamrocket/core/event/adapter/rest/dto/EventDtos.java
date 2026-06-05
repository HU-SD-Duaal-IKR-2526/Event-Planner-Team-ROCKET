package nl.teamrocket.core.event.adapter.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import nl.teamrocket.core.event.domain.model.Event;
import nl.teamrocket.core.event.domain.model.EventMetadata;
import nl.teamrocket.core.event.domain.model.EventStatus;
import nl.teamrocket.core.event.domain.model.Visibility;

import java.time.Instant;
import java.util.UUID;

/**
 * Verzamelklasse voor alle REST DTOs van de Event-module. Eén bestand → kleine
 * footprint, makkelijk vindbaar. Domain-objecten lekken niet door de REST-grens.
 */
public final class EventDtos {

    private EventDtos() {}

    public record CreateEventRequest(
            @NotNull(message = "organizerId is verplicht") UUID organizerId,
            UUID venueId,
            @NotBlank(message = "title is verplicht")
            @Size(max = 120) String title,
            @Size(max = 2000) String description,
            @NotNull(message = "startsAt is verplicht") Instant startsAt,
            @NotNull(message = "endsAt is verplicht") Instant endsAt,
            @Positive(message = "capacity moet > 0") Integer capacity,
            String dressCode,
            String speaker,
            String livestreamUrl,
            Visibility visibility
    ) {}

    public record UpdateEventRequest(
            @Size(max = 120) String title,
            @Size(max = 2000) String description,
            Instant startsAt,
            Instant endsAt,
            @Positive Integer capacity,
            String dressCode,
            String speaker,
            String livestreamUrl,
            Visibility visibility
    ) {}

    public record CancelEventRequest(
            @Size(max = 500) String reason
    ) {}

    public record EventResponse(
            UUID id,
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
            Visibility visibility,
            EventStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant publishedAt,
            Instant cancelledAt,
            String cancellationReason,
            Long version
    ) {
        public static EventResponse from(Event e) {
            EventMetadata m = e.getMetadata();
            return new EventResponse(
                    e.getId(),
                    e.getOrganizerId(),
                    e.getVenueId(),
                    e.getTitle(),
                    e.getDescription(),
                    e.getTimeSlot().startsAt(),
                    e.getTimeSlot().endsAt(),
                    m != null ? m.capacity() : null,
                    m != null ? m.dressCode() : null,
                    m != null ? m.speaker() : null,
                    m != null ? m.livestreamUrl() : null,
                    e.getVisibility(),
                    e.getStatus(),
                    e.getCreatedAt(),
                    e.getUpdatedAt(),
                    e.getPublishedAt(),
                    e.getCancelledAt(),
                    e.getCancellationReason(),
                    e.getVersion()
            );
        }
    }

    public record ApiError(int status, String message) {}
}
