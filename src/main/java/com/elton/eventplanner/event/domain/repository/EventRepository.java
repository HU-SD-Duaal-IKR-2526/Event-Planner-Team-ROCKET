package com.elton.eventplanner.event.domain.repository;

import com.elton.eventplanner.event.domain.model.Event;
import com.elton.eventplanner.event.domain.valueobject.EventId;

import java.util.List;
import java.util.Optional;

public interface EventRepository {

    List<Event> findAll();

    Optional<Event> findById(EventId id);

    Event save(Event event);

    void deleteById(EventId id);

    boolean existsById(EventId id);
}
