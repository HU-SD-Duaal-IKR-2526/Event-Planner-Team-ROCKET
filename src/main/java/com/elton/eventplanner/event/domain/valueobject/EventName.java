package com.elton.eventplanner.event.domain.valueobject;

import java.util.Objects;

public final class EventName {

    private final String value;

    public EventName(String value) {
        Objects.requireNonNull(value, "Event name cannot be null");
        if (value.isBlank())
            throw new IllegalArgumentException("Event name cannot be blank");
        if (value.length() < 4 || value.length() > 200)
            throw new IllegalArgumentException("Event name must be between 4 and 200 characters");
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventName other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
