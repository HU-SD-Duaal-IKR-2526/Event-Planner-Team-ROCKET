package com.elton.eventplanner.event.infrastructure.persistence;

import com.elton.eventplanner.event.domain.model.Event;
import com.elton.eventplanner.event.domain.model.EventStatus;
import com.elton.eventplanner.event.domain.repository.EventRepository;
import com.elton.eventplanner.event.domain.valueobject.EventId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepositoryAdapter implements EventRepository {

    private final EventJpaRepository jpaRepository;
    private final EventMapper mapper;

    public EventRepositoryAdapter(EventJpaRepository jpaRepository, EventMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Event> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Event> findByStatus(EventStatus status) {
        return jpaRepository.findByStatus(status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Event> findById(EventId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Event save(Event event) {
        EventJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(event));
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(EventId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsById(EventId id) {
        return jpaRepository.existsById(id.getValue());
    }
}
