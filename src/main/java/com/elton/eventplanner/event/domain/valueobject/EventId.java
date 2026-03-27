package com.elton.eventplanner.event.domain.valueobject;

import java.util.Objects;

public final class EventId {

    private final Long value;

    public EventId(Long value) {
        Objects.requireNonNull(value, "EventId cannot be null");
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventId other)) {
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
        return value.toString();
    }
}
