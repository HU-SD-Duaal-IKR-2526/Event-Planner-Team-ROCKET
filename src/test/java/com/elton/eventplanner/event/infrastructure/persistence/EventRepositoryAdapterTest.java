package com.elton.eventplanner.event.infrastructure.persistence;

import com.elton.eventplanner.event.domain.model.Event;
import com.elton.eventplanner.event.domain.model.EventStatus;
import com.elton.eventplanner.event.domain.valueobject.EventDate;
import com.elton.eventplanner.event.domain.valueobject.EventDescription;
import com.elton.eventplanner.event.domain.valueobject.EventId;
import com.elton.eventplanner.event.domain.valueobject.EventName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventRepositoryAdapterTest {

    @Mock
    private EventJpaRepository jpaRepository;
    @Mock
    private EventMapper mapper;
    @InjectMocks
    private EventRepositoryAdapter adapter;

    private EventJpaEntity sampleEntity;
    private Event sampleDomain;

    @BeforeEach
    void setUp() {
        sampleEntity = new EventJpaEntity(
                1L, "Team Rocket Kickoff", LocalDate.of(2026, 6, 15),
                "Amsterdam", "Een kickoff meeting voor Team Rocket.",
                EventStatus.PLANNED, 1L
        );
        sampleDomain = new Event(
                new EventId(1L),
                new EventName("Team Rocket Kickoff"),
                new EventDate(LocalDate.of(2026, 6, 15)),
                "Amsterdam",
                new EventDescription("Een kickoff meeting voor Team Rocket."),
                EventStatus.PLANNED,
                1L
        );
    }

    @Test
    void findAll_returnsMappedEvents() {
        when(jpaRepository.findAll()).thenReturn(List.of(sampleEntity));
        when(mapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        List<Event> result = adapter.findAll();

        assertEquals(1, result.size());
        assertEquals(sampleDomain, result.get(0));
    }

    @Test
    void findByStatus_returnsMappedEvents() {
        when(jpaRepository.findByStatus(EventStatus.PLANNED)).thenReturn(List.of(sampleEntity));
        when(mapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        List<Event> result = adapter.findByStatus(EventStatus.PLANNED);

        assertEquals(1, result.size());
        assertEquals(EventStatus.PLANNED, result.get(0).getStatus());
    }

    @Test
    void findById_found_returnsOptionalWithEvent() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(mapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        Optional<Event> result = adapter.findById(new EventId(1L));

        assertTrue(result.isPresent());
        assertEquals(sampleDomain, result.get());
    }

    @Test
    void findById_notFound_returnsEmptyOptional() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Event> result = adapter.findById(new EventId(99L));

        assertFalse(result.isPresent());
    }

    @Test
    void save_savesAndReturnsMappedEvent() {
        when(mapper.toJpaEntity(sampleDomain)).thenReturn(sampleEntity);
        when(jpaRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(mapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        Event result = adapter.save(sampleDomain);

        assertEquals(sampleDomain, result);
        verify(jpaRepository).save(sampleEntity);
    }

    @Test
    void deleteById_callsJpaDelete() {
        adapter.deleteById(new EventId(1L));

        verify(jpaRepository).deleteById(1L);
    }

    @Test
    void existsById_returnsTrue() {
        when(jpaRepository.existsById(1L)).thenReturn(true);

        assertTrue(adapter.existsById(new EventId(1L)));
    }

    @Test
    void existsById_returnsFalse() {
        when(jpaRepository.existsById(99L)).thenReturn(false);

        assertFalse(adapter.existsById(new EventId(99L)));
    }

    @Test
    void findByStatus_emptyResult_returnsEmptyList() {
        when(jpaRepository.findByStatus(EventStatus.CANCELLED)).thenReturn(List.of());

        List<Event> result = adapter.findByStatus(EventStatus.CANCELLED);

        assertEquals(0, result.size());
    }
}
