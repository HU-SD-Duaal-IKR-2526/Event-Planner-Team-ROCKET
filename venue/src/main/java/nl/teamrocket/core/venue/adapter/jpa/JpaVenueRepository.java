package nl.teamrocket.core.venue.adapter.jpa;

import lombok.RequiredArgsConstructor;
import nl.teamrocket.core.venue.adapter.jpa.entity.VenueJpaEntity;
import nl.teamrocket.core.venue.adapter.jpa.mapper.VenueJpaMapper;
import nl.teamrocket.core.venue.adapter.jpa.repository.SpringDataVenueRepository;
import nl.teamrocket.core.venue.application.port.outbound.VenueRepository;
import nl.teamrocket.core.venue.domain.model.Venue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary adapter: implementeert {@link VenueRepository} bovenop Spring Data JPA.
 * Mapt naar/van het domain aggregate; domein blijft schoon.
 */
@Component
@RequiredArgsConstructor
public class JpaVenueRepository implements VenueRepository {

    private final SpringDataVenueRepository delegate;

    @Override
    public Venue save(Venue venue) {
        VenueJpaEntity entity = delegate.findById(venue.getId())
                .map(existing -> {
                    VenueJpaMapper.updateEntity(existing, venue);
                    return existing;
                })
                .orElseGet(() -> VenueJpaMapper.toEntity(venue));
        delegate.save(entity);
        return venue;
    }

    @Override
    public Optional<Venue> findById(UUID id) {
        return delegate.findById(id).map(VenueJpaMapper::toDomain);
    }

    @Override
    public List<Venue> findAll() {
        return delegate.findAll().stream().map(VenueJpaMapper::toDomain).toList();
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
