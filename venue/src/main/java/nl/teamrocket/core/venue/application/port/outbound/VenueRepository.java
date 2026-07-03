package nl.teamrocket.core.venue.application.port.outbound;

import nl.teamrocket.core.venue.domain.model.Venue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: persistence van de lokale read-cache van venues.
 * Geen JPA-types — application laag blijft framework-vrij.
 */
public interface VenueRepository {

    Venue save(Venue venue);

    Optional<Venue> findById(UUID id);

    List<Venue> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
