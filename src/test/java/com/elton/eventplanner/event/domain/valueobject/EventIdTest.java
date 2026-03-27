package com.elton.eventplanner.event.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EventIdTest {

    @Test
    void validId_createsSuccessfully() {
        EventId id = new EventId(1L);
        assertEquals(1L, id.getValue());
    }

    @Test
    void nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EventId(null));
    }

    @Test
    void equalIds_areEqual() {
        EventId a = new EventId(42L);
        EventId b = new EventId(42L);
        assertEquals(a, b);
    }

    @Test
    void differentIds_areNotEqual() {
        EventId a = new EventId(1L);
        EventId b = new EventId(2L);
        assertNotEquals(a, b);
    }

    @Test
    void toString_returnsStringValue() {
        EventId id = new EventId(99L);
        assertEquals("99", id.toString());
    }
}
