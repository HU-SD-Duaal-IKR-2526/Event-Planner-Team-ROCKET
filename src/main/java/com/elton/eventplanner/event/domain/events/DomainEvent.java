package com.elton.eventplanner.event.domain.events;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
