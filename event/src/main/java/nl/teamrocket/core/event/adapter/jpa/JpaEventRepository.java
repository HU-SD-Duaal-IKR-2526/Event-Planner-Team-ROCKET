package nl.teamrocket.core.event.adapter.jpa;

import lombok.RequiredArgsConstructor;
import nl.teamrocket.core.event.adapter.jpa.entity.EventJpaEntity;
import nl.teamrocket.core.event.adapter.jpa.mapper.EventJpaMapper;
import nl.teamrocket.core.event.adapter.jpa.repository.SpringDataEventRepository;
import nl.teamrocket.core.event.application.port.outbound.EventRepository;
import nl.teamrocket.core.event.domain.model.Event;
import nl.teamrocket.core.event.domain.model.EventStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary adapter: implementeert de outbound port {@link EventRepository} bovenop
 * Spring Data JPA. Doet de mapping naar/van het domain aggregate.
 *
 * Architectuurdoc §4.2: deze laag mag JPA kennen; de application laag enkel
 * de port.
 */
@Component
@RequiredArgsConstructor
public class JpaEventRepository implements EventRepository {

    private final SpringDataEventRepository delegate;

    @Override
    public Event save(Event event) {
        EventJpaEntity entity = delegate.findById(event.getId())
                .map(existing -> {
                    EventJpaMapper.updateEntity(existing, event);
                    return existing;
                })
                .orElseGet(() -> EventJpaMapper.toEntity(event));

        EventJpaEntity saved = delegate.save(entity);
        event.setVersion(saved.getVersion());
        return event;
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return delegate.findById(id).map(EventJpaMapper::toDomain);
    }

    @Override
    public List<Event> findAll() {
        return delegate.findAll().stream().map(EventJpaMapper::toDomain).toList();
    }

    @Override
    public List<Event> findByFilter(EventStatus status, UUID organizerId) {
        return delegate.findByFilter(status, organizerId).stream()
                .map(EventJpaMapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        delegate.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return delegate.existsById(id);
    }
}
