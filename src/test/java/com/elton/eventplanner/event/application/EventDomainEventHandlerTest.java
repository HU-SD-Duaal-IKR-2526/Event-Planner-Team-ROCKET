package com.elton.eventplanner.event.application;

import com.elton.eventplanner.event.domain.events.EventCancelledDomainEvent;
import com.elton.eventplanner.event.domain.events.EventCreatedDomainEvent;
import com.elton.eventplanner.event.domain.events.EventStatusChangedDomainEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EventDomainEventHandlerTest {

    private EventDomainEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EventDomainEventHandler();
    }

    @Test
    void on_eventCreated_doesNotThrow() {
        assertDoesNotThrow(() -> handler.on(new EventCreatedDomainEvent(1L, "Team Rocket Kickoff")));
    }

    @Test
    void on_eventCancelled_doesNotThrow() {
        assertDoesNotThrow(() -> handler.on(new EventCancelledDomainEvent(1L)));
    }

    @Test
    void on_eventStatusChanged_doesNotThrow() {
        assertDoesNotThrow(() -> handler.on(new EventStatusChangedDomainEvent(1L, "PLANNED", "COMPLETED")));
    }
}
