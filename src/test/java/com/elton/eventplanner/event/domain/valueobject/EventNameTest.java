package com.elton.eventplanner.event.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EventNameTest {

    @Test
    void validName_createsSuccessfully() {
        EventName name = new EventName("Team Rocket Meeting");
        assertEquals("Team Rocket Meeting", name.getValue());
    }

    @Test
    void blankName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EventName("   "));
    }

    @Test
    void nullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EventName(null));
    }

    @Test
    void nameTooShort_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EventName("AB"));
    }

    @Test
    void nameTooLong_throwsIllegalArgumentException() {
        String tooLong = "A".repeat(201);
        assertThrows(IllegalArgumentException.class, () -> new EventName(tooLong));
    }

    @Test
    void nameAtMinLength_createsSuccessfully() {
        EventName name = new EventName("Abcd");
        assertEquals("Abcd", name.getValue());
    }

    @Test
    void nameAtMaxLength_createsSuccessfully() {
        String maxLength = "A".repeat(200);
        EventName name = new EventName(maxLength);
        assertEquals(200, name.getValue().length());
    }

    @Test
    void equalNames_areEqual() {
        EventName a = new EventName("Rocket Launch");
        EventName b = new EventName("Rocket Launch");
        assertEquals(a, b);
    }

    @Test
    void differentNames_areNotEqual() {
        EventName a = new EventName("Rocket Launch");
        EventName b = new EventName("Rocket Landing");
        assertNotEquals(a, b);
    }
}
