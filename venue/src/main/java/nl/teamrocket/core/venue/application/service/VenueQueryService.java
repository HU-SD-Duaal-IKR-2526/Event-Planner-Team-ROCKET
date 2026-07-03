package nl.teamrocket.core.venue.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.venue.application.port.inbound.VenueQueryPort;
import nl.teamrocket.core.venue.application.port.outbound.ExternalVenueGateway;
import nl.teamrocket.core.venue.application.port.outbound.VenueRepository;
import nl.teamrocket.core.venue.application.query.AvailabilityResult;
import nl.teamrocket.core.venue.application.query.CheckAvailabilityQuery;
import nl.teamrocket.core.venue.application.query.FindVenuesNearbyQuery;
import nl.teamrocket.core.venue.application.query.GetVenueQuery;
import nl.teamrocket.core.venue.domain.exception.VenueNotFoundException;
import nl.teamrocket.core.venue.domain.model.Venue;
import nl.teamrocket.core.venue.domain.service.DistanceCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Read-pad. Lees-only transacties (CQRS-light, architectuurdoc §4.1.2).
 *
 * Geospatiaal zoeken wordt hier in-memory uitgevoerd met
 * {@link DistanceCalculator}. In productie zou dit een PostGIS-query zijn met
 * spatiale index — zie README §"Afwijkingen van het ontwerp".
 *
 * Beschikbaarheid wordt expliciet aan het externe systeem gevraagd, niet uit de
 * lokale cache: bookings horen niet bij ons aggregate (data-distributiedoc §2.3.2).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueQueryService implements VenueQueryPort {

    private final VenueRepository repository;
    private final ExternalVenueGateway externalGateway;

    @Override
    public Venue findById(GetVenueQuery query) {
        return repository.findById(query.venueId())
                .orElseThrow(() -> new VenueNotFoundException(query.venueId()));
    }

    @Override
    public List<Venue> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Venue> findNearby(FindVenuesNearbyQuery query) {
        return repository.findAll().stream()
                .filter(v -> v.getLocation() != null)
                .filter(v -> DistanceCalculator.distanceInKm(query.center(), v.getLocation()) <= query.radiusKm())
                .sorted(Comparator.comparingDouble(
                        v -> DistanceCalculator.distanceInKm(query.center(), v.getLocation())))
                .toList();
    }

    @Override
    public AvailabilityResult checkAvailability(CheckAvailabilityQuery query) {
        // Eerst lokale bestaanscheck — anders heeft een availability-vraag geen betekenis.
        if (!repository.existsById(query.venueId())) {
            throw new VenueNotFoundException(query.venueId());
        }
        try {
            return externalGateway.checkAvailability(query.venueId(), query.from(), query.to());
        } catch (RuntimeException ex) {
            // Fail-closed: liever 'onbekend' dan 'beschikbaar' bij externe storing
            // (zie data-distributiedoc §6 — voorkomt dubbele boekingen).
            log.warn("Availability check faalde op extern systeem voor venue={}: {}",
                    query.venueId(), ex.getMessage());
            return AvailabilityResult.unknown("extern systeem niet bereikbaar");
        }
    }
}
