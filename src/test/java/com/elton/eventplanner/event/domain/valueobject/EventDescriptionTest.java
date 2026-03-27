package com.elton.eventplanner.event.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventDescriptionTest {

    @Test
    void validDescription_createsSuccessfully() {
        EventDescription desc = new EventDescription("Een beschrijving van minimaal tien tekens.");
        assertEquals("Een beschrijving van minimaal tien tekens.", desc.getValue());
    }

    @Test
    void nullDescription_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EventDescription(null));
    }

    @Test
    void descriptionTooShort_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EventDescription("Kort"));
    }

    @Test
    void descriptionTooLong_throwsIllegalArgumentException() {
        String tooLong = "A".repeat(501);
        assertThrows(IllegalArgumentException.class, () -> new EventDescription(tooLong));
    }

    @Test
    void descriptionAtMinLength_createsSuccessfully() {
        String minLength = "A".repeat(10);
        EventDescription desc = new EventDescription(minLength);
        assertEquals(10, desc.getValue().length());
    }

    @Test
    void descriptionAtMaxLength_createsSuccessfully() {
        String maxLength = "A".repeat(500);
        EventDescription desc = new EventDescription(maxLength);
        assertEquals(500, desc.getValue().length());
    }

    @Test
    void equalDescriptions_areEqual() {
        EventDescription a = new EventDescription("Zelfde beschrijving voor dit event.");
        EventDescription b = new EventDescription("Zelfde beschrijving voor dit event.");
        assertEquals(a, b);
    }

    @Test
    void differentDescriptions_areNotEqual() {
        EventDescription a = new EventDescription("Beschrijving een voor dit event.");
        EventDescription b = new EventDescription("Beschrijving twee voor dit event.");
        assertNotEquals(a, b);
    }
}
