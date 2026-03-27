package com.elton.eventplanner.event.domain.model;

import com.elton.eventplanner.event.domain.valueobject.EventDate;
import com.elton.eventplanner.event.domain.valueobject.EventDescription;
import com.elton.eventplanner.event.domain.valueobject.EventId;
import com.elton.eventplanner.event.domain.valueobject.EventName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventTest {

    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.create(
                new EventName("Team Rocket Kickoff"),
                new EventDate(LocalDate.now().plusDays(10)),
                "Amsterdam",
                new EventDescription("Een kickoff meeting voor Team Rocket."),
                1L
        );
    }

    // --- create() ---

    @Test
    void create_setsStatusToPlanned() {
        assertEquals(EventStatus.PLANNED, event.getStatus());
    }

    @Test
    void create_hasNoIdYet() {
        assertNull(event.getId());
    }

    @Test
    void create_storesAllFields() {
        assertEquals("Team Rocket Kickoff", event.getName().getValue());
        assertEquals("Amsterdam", event.getLocation());
        assertEquals(1L, event.getUserId());
    }

    // --- cancel() ---

    @Test
    void cancel_changesStatusToCancelled() {
        event.cancel();
        assertEquals(EventStatus.CANCELLED, event.getStatus());
    }

    @Test
    void cancel_alreadyCancelled_throwsIllegalStateException() {
        event.cancel();
        assertThrows(IllegalStateException.class, () -> event.cancel());
    }

    // --- updateStatus() ---

    @Test
    void updateStatus_pastDate_setsCompleted() {
        Event pastEvent = Event.create(
                new EventName("Oud event"),
                new EventDate(LocalDate.now().minusDays(1)),
                "Rotterdam",
                new EventDescription("Dit event is al voorbij."),
                1L
        );
        pastEvent.updateStatus(LocalDate.now());
        assertEquals(EventStatus.COMPLETED, pastEvent.getStatus());
    }

    @Test
    void updateStatus_futureDate_setsPlanned() {
        event.updateStatus(LocalDate.now());
        assertEquals(EventStatus.PLANNED, event.getStatus());
    }

    @Test
    void updateStatus_cancelledEvent_doesNotChangeStatus() {
        event.cancel();
        event.updateStatus(LocalDate.now().plusDays(100));
        assertEquals(EventStatus.CANCELLED, event.getStatus());
    }

    // --- update() ---

    @Test
    void update_changesAllFields() {
        EventName newName = new EventName("Nieuw event naam");
        EventDate newDate = new EventDate(LocalDate.now().plusDays(20));
        EventDescription newDesc = new EventDescription("Bijgewerkte beschrijving van dit event.");

        event.update(newName, newDate, "Utrecht", newDesc, EventStatus.PLANNED, 2L);

        assertEquals("Nieuw event naam", event.getName().getValue());
        assertEquals("Utrecht", event.getLocation());
        assertEquals(2L, event.getUserId());
    }

    // --- equals() ---

    @Test
    void equals_sameId_areEqual() {
        Event e1 = new Event(new EventId(1L), new EventName("Event A"),
                new EventDate(LocalDate.now().plusDays(1)), "Den Haag",
                new EventDescription("Beschrijving van event A."), EventStatus.PLANNED, 1L);
        Event e2 = new Event(new EventId(1L), new EventName("Event B"),
                new EventDate(LocalDate.now().plusDays(2)), "Leiden",
                new EventDescription("Beschrijving van event B."), EventStatus.CANCELLED, 2L);

        assertEquals(e1, e2);
    }

    @Test
    void equals_differentId_areNotEqual() {
        Event e1 = new Event(new EventId(1L), new EventName("Event A"),
                new EventDate(LocalDate.now().plusDays(1)), "Den Haag",
                new EventDescription("Beschrijving van event A."), EventStatus.PLANNED, 1L);
        Event e2 = new Event(new EventId(2L), new EventName("Event A"),
                new EventDate(LocalDate.now().plusDays(1)), "Den Haag",
                new EventDescription("Beschrijving van event A."), EventStatus.PLANNED, 1L);

        assertNotEquals(e1, e2);
    }
}
