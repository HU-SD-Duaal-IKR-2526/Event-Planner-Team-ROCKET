package com.elton.eventplanner.event.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.elton.eventplanner.event.domain.model.Event;
import com.elton.eventplanner.event.domain.model.EventStatus;
import com.elton.eventplanner.event.domain.valueobject.EventDate;
import com.elton.eventplanner.event.domain.valueobject.EventDescription;
import com.elton.eventplanner.event.domain.valueobject.EventId;
import com.elton.eventplanner.event.domain.valueobject.EventName;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventMapperTest {

    private EventMapper mapper;
    private final LocalDate eventDate = LocalDate.of(2026, 6, 15);

    @BeforeEach
    void setUp() {
        mapper = new EventMapper();
    }

    @Test
    void toDomain_mapsAllFields() {
        EventJpaEntity entity =
                new EventJpaEntity(
                        1L,
                        "Team Rocket Kickoff",
                        eventDate,
                        "Amsterdam",
                        "Een kickoff meeting voor Team Rocket.",
                        EventStatus.PLANNED,
                        2L);

        Event domain = mapper.toDomain(entity);

        assertEquals(1L, domain.getId().getValue());
        assertEquals("Team Rocket Kickoff", domain.getName().getValue());
        assertEquals(eventDate, domain.getDate().getValue());
        assertEquals("Amsterdam", domain.getLocation());
        assertEquals("Een kickoff meeting voor Team Rocket.", domain.getDescription().getValue());
        assertEquals(EventStatus.PLANNED, domain.getStatus());
        assertEquals(2L, domain.getUserId());
    }

    @Test
    void toJpaEntity_withId_mapsAllFields() {
        Event event =
                new Event(
                        new EventId(1L),
                        new EventName("Team Rocket Kickoff"),
                        new EventDate(eventDate),
                        "Amsterdam",
                        new EventDescription("Een kickoff meeting voor Team Rocket."),
                        EventStatus.PLANNED,
                        2L);

        EventJpaEntity entity = mapper.toJpaEntity(event);

        assertEquals(1L, entity.getId());
        assertEquals("Team Rocket Kickoff", entity.getName());
        assertEquals(eventDate, entity.getDate());
        assertEquals("Amsterdam", entity.getLocation());
        assertEquals("Een kickoff meeting voor Team Rocket.", entity.getDescription());
        assertEquals(EventStatus.PLANNED, entity.getStatus());
        assertEquals(2L, entity.getUserId());
    }

    @Test
    void toJpaEntity_withoutId_setsNullId() {
        Event event =
                Event.create(
                        new EventName("Nieuw Event"),
                        new EventDate(eventDate),
                        "Rotterdam",
                        new EventDescription("Een nieuw event zonder id nog."),
                        1L);

        EventJpaEntity entity = mapper.toJpaEntity(event);

        assertNull(entity.getId());
        assertEquals("Nieuw Event", entity.getName());
    }

    @Test
    void toDomain_cancelledStatus_mapsCorrectly() {
        EventJpaEntity entity =
                new EventJpaEntity(
                        3L,
                        "Afgelast Event",
                        eventDate,
                        "Den Haag",
                        "Dit event is helaas afgelast.",
                        EventStatus.CANCELLED,
                        1L);

        Event domain = mapper.toDomain(entity);

        assertEquals(EventStatus.CANCELLED, domain.getStatus());
    }

    @Test
    void toJpaEntity_completedStatus_mapsCorrectly() {
        Event event =
                new Event(
                        new EventId(4L),
                        new EventName("Afgelopen Event"),
                        new EventDate(eventDate),
                        "Utrecht",
                        new EventDescription("Dit event is al afgelopen."),
                        EventStatus.COMPLETED,
                        1L);

        EventJpaEntity entity = mapper.toJpaEntity(event);

        assertEquals(EventStatus.COMPLETED, entity.getStatus());
    }
}
