package com.elton.eventplanner.event.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EventDateTest {

    @Test
    void validDate_createsSuccessfully() {
        LocalDate date = LocalDate.of(2026, 6, 15);
        EventDate eventDate = new EventDate(date);
        assertEquals(date, eventDate.getValue());
    }

    @Test
    void nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EventDate(null));
    }

    @Test
    void isBefore_returnsTrue_whenDateIsBeforeGiven() {
        EventDate eventDate = new EventDate(LocalDate.of(2026, 1, 1));
        assertTrue(eventDate.isBefore(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void isBefore_returnsFalse_whenDateIsAfterGiven() {
        EventDate eventDate = new EventDate(LocalDate.of(2026, 12, 1));
        assertFalse(eventDate.isBefore(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void isAfter_returnsTrue_whenDateIsAfterGiven() {
        EventDate eventDate = new EventDate(LocalDate.of(2026, 12, 1));
        assertTrue(eventDate.isAfter(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void isAfter_returnsFalse_whenDateIsBeforeGiven() {
        EventDate eventDate = new EventDate(LocalDate.of(2026, 1, 1));
        assertFalse(eventDate.isAfter(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void equalDates_areEqual() {
        EventDate a = new EventDate(LocalDate.of(2026, 6, 15));
        EventDate b = new EventDate(LocalDate.of(2026, 6, 15));
        assertEquals(a, b);
    }

    @Test
    void differentDates_areNotEqual() {
        EventDate a = new EventDate(LocalDate.of(2026, 6, 15));
        EventDate b = new EventDate(LocalDate.of(2026, 6, 16));
        assertNotEquals(a, b);
    }
}
