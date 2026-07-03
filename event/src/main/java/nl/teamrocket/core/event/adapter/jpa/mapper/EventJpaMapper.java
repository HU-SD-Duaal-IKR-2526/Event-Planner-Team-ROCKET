package nl.teamrocket.core.event.adapter.jpa.mapper;

import nl.teamrocket.core.event.adapter.jpa.entity.EventJpaEntity;
import nl.teamrocket.core.event.domain.model.Event;
import nl.teamrocket.core.event.domain.model.EventMetadata;
import nl.teamrocket.core.event.domain.model.EventTimeSlot;

/**
 * Mapping tussen domain {@link Event} en JPA {@link EventJpaEntity}.
 * Domeinklasse heeft geen JPA-afhankelijkheden — daarom hier expliciet.
 */
public final class EventJpaMapper {

    private EventJpaMapper() {}

    public static EventJpaEntity toEntity(Event event) {
        EventMetadata md = event.getMetadata();
        return new EventJpaEntity(
                event.getId(),
                event.getOrganizerId(),
                event.getVenueId(),
                event.getTitle(),
                event.getDescription(),
                event.getTimeSlot().startsAt(),
                event.getTimeSlot().endsAt(),
                md != null ? md.dressCode() : null,
                md != null ? md.speaker() : null,
                md != null ? md.capacity() : null,
                md != null ? md.livestreamUrl() : null,
                event.getVisibility(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getPublishedAt(),
                event.getCancelledAt(),
                event.getCancellationReason(),
                event.getVersion()
        );
    }

    /** Update bestaande managed entity in plaats van nieuwe te creëren — behoudt JPA identity. */
    public static void updateEntity(EventJpaEntity entity, Event event) {
        EventMetadata md = event.getMetadata();
        entity.setVenueId(event.getVenueId());
        entity.setTitle(event.getTitle());
        entity.setDescription(event.getDescription());
        entity.setStartsAt(event.getTimeSlot().startsAt());
        entity.setEndsAt(event.getTimeSlot().endsAt());
        entity.setDressCode(md != null ? md.dressCode() : null);
        entity.setSpeaker(md != null ? md.speaker() : null);
        entity.setCapacity(md != null ? md.capacity() : null);
        entity.setLivestreamUrl(md != null ? md.livestreamUrl() : null);
        entity.setVisibility(event.getVisibility());
        entity.setStatus(event.getStatus());
        entity.setUpdatedAt(event.getUpdatedAt());
        entity.setPublishedAt(event.getPublishedAt());
        entity.setCancelledAt(event.getCancelledAt());
        entity.setCancellationReason(event.getCancellationReason());
    }

    public static Event toDomain(EventJpaEntity entity) {
        EventTimeSlot slot = new EventTimeSlot(entity.getStartsAt(), entity.getEndsAt());
        EventMetadata metadata = new EventMetadata(
                entity.getDressCode(), entity.getSpeaker(),
                entity.getCapacity(), entity.getLivestreamUrl());
        Event event = Event.reconstitute(
                entity.getId(),
                entity.getOrganizerId(),
                entity.getVenueId(),
                entity.getTitle(),
                entity.getDescription(),
                slot,
                metadata,
                entity.getVisibility(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPublishedAt(),
                entity.getCancelledAt(),
                entity.getCancellationReason(),
                entity.getVersion());
        return event;
    }
}
