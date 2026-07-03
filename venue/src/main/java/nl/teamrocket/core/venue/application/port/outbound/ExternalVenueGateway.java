package nl.teamrocket.core.venue.application.port.outbound;

import nl.teamrocket.core.venue.application.query.AvailabilityResult;
import nl.teamrocket.core.venue.domain.model.Venue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: koppeling met het externe venue-systeem.
 *
 * Dit is het hart van de Anti-Corruption Layer (architectuurdoc §3.2.2,
 * data-distributiedoc §2.3.2). De application laag spreekt domein-types
 * ({@link Venue}, {@link AvailabilityResult}); de adapter is verantwoordelijk
 * voor het mappen van/naar het externe API-model en het isoleren van
 * externe wijzigingen.
 *
 * Concrete adapter zou een HTTP-client zijn naar de externe SaaS. In deze
 * fase: in-memory stub (zie README §"Afwijkingen").
 */
public interface ExternalVenueGateway {

    /** Haalt een verse snapshot van een venue op bij het externe systeem. */
    Optional<Venue> fetch(UUID externalId);

    /** Vraagt het externe systeem of de venue beschikbaar is in {@code [from, to]}. */
    AvailabilityResult checkAvailability(UUID externalId, Instant from, Instant to);
}
