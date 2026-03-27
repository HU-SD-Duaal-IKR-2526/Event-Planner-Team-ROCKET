package com.elton.eventplanner.event.domain.events;

import java.time.Instant;

public record EventCreatedDomainEvent(Long eventId, String eventName, Instant occurredOn)
        implements DomainEvent {

    public EventCreatedDomainEvent(Long eventId, String eventName) {
        this(eventId, eventName, Instant.now());
    }
}
