package com.elton.eventplanner.event.infrastructure.persistence;

import com.elton.eventplanner.event.domain.model.Event;
import com.elton.eventplanner.event.domain.valueobject.EventDate;
import com.elton.eventplanner.event.domain.valueobject.EventDescription;
import com.elton.eventplanner.event.domain.valueobject.EventId;
import com.elton.eventplanner.event.domain.valueobject.EventName;

import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toDomain(EventJpaEntity entity) {
        return new Event(
                new EventId(entity.getId()),
                new EventName(entity.getName()),
                new EventDate(entity.getDate()),
                entity.getLocation(),
                new EventDescription(entity.getDescription()),
                entity.getStatus(),
                entity.getUserId()
        );
    }

    public EventJpaEntity toJpaEntity(Event event) {
        Long id = event.getId() != null ? event.getId().getValue() : null;
        return new EventJpaEntity(
                id,
                event.getName().getValue(),
                event.getDate().getValue(),
                event.getLocation(),
                event.getDescription().getValue(),
                event.getStatus(),
                event.getUserId()
        );
    }
}
