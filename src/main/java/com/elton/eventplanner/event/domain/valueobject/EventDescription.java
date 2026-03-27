package com.elton.eventplanner.event.domain.valueobject;

import java.util.Objects;

public final class EventDescription {

    private final String value;

    public EventDescription(String value) {
        Objects.requireNonNull(value, "Event description cannot be null");
        if (value.length() < 10 || value.length() > 500) {
            throw new IllegalArgumentException("Event description must be between 10 and 500 characters");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventDescription other)) {
            return false;
        }
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
