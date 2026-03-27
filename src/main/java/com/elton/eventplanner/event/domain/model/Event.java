package com.elton.eventplanner.event.domain.model;

import com.elton.eventplanner.event.domain.events.DomainEvent;
import com.elton.eventplanner.event.domain.exception.EventAlreadyCancelledException;
import com.elton.eventplanner.event.domain.events.EventCancelledDomainEvent;
import com.elton.eventplanner.event.domain.events.EventStatusChangedDomainEvent;
import com.elton.eventplanner.event.domain.valueobject.EventDate;
import com.elton.eventplanner.event.domain.valueobject.EventDescription;
import com.elton.eventplanner.event.domain.valueobject.EventId;
import com.elton.eventplanner.event.domain.valueobject.EventName;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Event {

    private EventId id;
    private EventName name;
    private EventDate date;
    private String location;
    private EventDescription description;
    private EventStatus status;
    private Long userId;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

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
            throw new EventAlreadyCancelledException(id != null ? id.getValue() : null);
        }
        this.status = EventStatus.CANCELLED;
        domainEvents.add(new EventCancelledDomainEvent(id != null ? id.getValue() : null));
    }

    public void updateStatus(LocalDate today) {
        if (this.status == EventStatus.CANCELLED) {
            return;
        }
        EventStatus previousStatus = this.status;
        if (this.date.isBefore(today)) {
            this.status = EventStatus.COMPLETED;
        } else if (this.date.isAfter(today)) {
            this.status = EventStatus.PLANNED;
        }
        if (this.status != previousStatus) {
            domainEvents.add(new EventStatusChangedDomainEvent(
                    id != null ? id.getValue() : null,
                    previousStatus.name(),
                    this.status.name()
            ));
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

    // --- Domain events ---

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = Collections.unmodifiableList(new ArrayList<>(domainEvents));
        domainEvents.clear();
        return events;
    }

    // --- Getters ---

    public EventId getId() {
        return id;
    }

    public EventName getName() {
        return name;
    }

    public EventDate getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public EventDescription getDescription() {
        return description;
    }

    public EventStatus getStatus() {
        return status;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
