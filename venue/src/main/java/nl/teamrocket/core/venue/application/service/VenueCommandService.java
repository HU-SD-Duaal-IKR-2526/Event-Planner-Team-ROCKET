package nl.teamrocket.core.venue.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.venue.application.command.RefreshFromExternalCommand;
import nl.teamrocket.core.venue.application.command.RegisterVenueCommand;
import nl.teamrocket.core.venue.application.command.RemoveVenueCommand;
import nl.teamrocket.core.venue.application.port.inbound.VenueCommandPort;
import nl.teamrocket.core.venue.application.port.outbound.ExternalVenueGateway;
import nl.teamrocket.core.venue.application.port.outbound.VenueRepository;
import nl.teamrocket.core.venue.domain.exception.VenueNotFoundException;
import nl.teamrocket.core.venue.domain.model.Venue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Orchestreert mutaties op de lokale venue-cache.
 *
 * Architectuurdoc §4.2: deze service kent enkel de outbound ports
 * {@link VenueRepository} en {@link ExternalVenueGateway} — geen infrastructuur.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VenueCommandService implements VenueCommandPort {

    private final VenueRepository repository;
    private final ExternalVenueGateway externalGateway;

    @Override
    @Transactional
    public Venue register(RegisterVenueCommand cmd) {
        Venue venue = Venue.cacheNew(
                cmd.externalId(),
                cmd.name(),
                cmd.address(),
                cmd.location(),
                cmd.capacity(),
                cmd.amenities(),
                cmd.externalVersion()
        );
        Venue saved = repository.save(venue);
        log.info("Venue registered: id={} name={} capacity={}",
                saved.getId(), saved.getName(), saved.getCapacity());
        return saved;
    }

    @Override
    @Transactional
    public boolean refreshFromExternal(RefreshFromExternalCommand cmd) {
        Optional<Venue> remote = externalGateway.fetch(cmd.externalId());
        if (remote.isEmpty()) {
            log.warn("External system heeft geen venue voor id={}", cmd.externalId());
            return false;
        }
        Venue remoteVenue = remote.get();

        Optional<Venue> localOpt = repository.findById(cmd.externalId());
        if (localOpt.isEmpty()) {
            // Eerste keer cachen
            repository.save(remoteVenue);
            log.info("Venue initial cache from external: id={}", cmd.externalId());
            return true;
        }

        Venue local = localOpt.get();
        boolean updated = local.refreshFromExternal(
                remoteVenue.getName(),
                remoteVenue.getAddress(),
                remoteVenue.getLocation(),
                remoteVenue.getCapacity(),
                remoteVenue.getAmenities(),
                remoteVenue.getExternalVersion()
        );
        if (updated) {
            repository.save(local);
            log.info("Venue refreshed from external: id={} externalVersion={}",
                    cmd.externalId(), remoteVenue.getExternalVersion());
        } else {
            log.debug("Refresh genegeerd (oudere of gelijke externalVersion): id={}",
                    cmd.externalId());
        }
        return updated;
    }

    @Override
    @Transactional
    public void remove(RemoveVenueCommand cmd) {
        if (!repository.existsById(cmd.venueId())) {
            throw new VenueNotFoundException(cmd.venueId());
        }
        repository.deleteById(cmd.venueId());
        log.info("Venue removed from cache: id={}", cmd.venueId());
    }
}
