package com.elton.eventplanner.event.domain.events;

import java.time.Instant;

public record EventCancelledDomainEvent(Long eventId, Instant occurredOn) implements DomainEvent {

    public EventCancelledDomainEvent(Long eventId) {
        this(eventId, Instant.now());
    }
}
