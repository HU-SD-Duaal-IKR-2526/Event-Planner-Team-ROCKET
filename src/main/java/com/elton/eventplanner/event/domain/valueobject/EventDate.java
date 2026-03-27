package com.elton.eventplanner.event.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

public final class EventDate {

    private final LocalDate value;

    public EventDate(LocalDate value) {
        Objects.requireNonNull(value, "Event date cannot be null");
        this.value = value;
    }

    public LocalDate getValue() {
        return value;
    }

    public boolean isBefore(LocalDate date) {
        return value.isBefore(date);
    }

    public boolean isAfter(LocalDate date) {
        return value.isAfter(date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventDate other)) {
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
