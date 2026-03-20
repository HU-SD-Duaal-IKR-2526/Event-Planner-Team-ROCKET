package com.elton.eventplanner.event.domain.model;

import com.elton.eventplanner.event.domain.valueobject.EventDate;
import com.elton.eventplanner.event.domain.valueobject.EventDescription;
import com.elton.eventplanner.event.domain.valueobject.EventId;
import com.elton.eventplanner.event.domain.valueobject.EventName;

import java.time.LocalDate;
import java.util.Objects;

public class Event {

    private EventId id;
    private EventName name;
    private EventDate date;
    private String location;
    private EventDescription description;
    private EventStatus status;
    private Long userId;

    // Herstel vanuit persistence
    public Event(EventId id, EventName name, EventDate date, String location,
                 EventDescription description, EventStatus status, Long userId) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.location = location;
        this.description = description;
        this.status = status;
        this.userId = userId;
    }

    // Fabrieksmethode voor nieuw event — altijd PLANNED
    public static Event create(EventName name, EventDate date, String location,
                               EventDescription description, Long userId) {
        return new Event(null, name, date, location, description, EventStatus.PLANNED, userId);
    }

    // --- Gedrag (business logic) ---

    public void cancel() {
        if (this.status == EventStatus.CANCELLED) {
            throw new IllegalStateException("Event is already cancelled");
        }
        this.status = EventStatus.CANCELLED;
    }

    public void updateStatus(LocalDate today) {
        if (this.status == EventStatus.CANCELLED) return;
        if (this.date.isBefore(today)) {
            this.status = EventStatus.COMPLETED;
        } else if (this.date.isAfter(today)) {
            this.status = EventStatus.PLANNED;
        }
    }

    public void update(EventName name, EventDate date, String location,
                       EventDescription description, EventStatus status, Long userId) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.description = description;
        this.status = status;
        this.userId = userId;
    }

    // --- Getters ---

    public EventId getId() { return id; }
    public EventName getName() { return name; }
    public EventDate getDate() { return date; }
    public String getLocation() { return location; }
    public EventDescription getDescription() { return description; }
    public EventStatus getStatus() { return status; }
    public Long getUserId() { return userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
