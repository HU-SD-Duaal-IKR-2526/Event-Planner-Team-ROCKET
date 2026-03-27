package com.elton.eventplanner.event.domain.events;

import java.time.Instant;

public record EventStatusChangedDomainEvent(
        Long eventId, String oldStatus, String newStatus, Instant occurredOn) implements DomainEvent {

    public EventStatusChangedDomainEvent(Long eventId, String oldStatus, String newStatus) {
        this(eventId, oldStatus, newStatus, Instant.now());
    }
}
