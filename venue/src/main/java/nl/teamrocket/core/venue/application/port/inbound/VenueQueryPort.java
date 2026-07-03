package nl.teamrocket.core.venue.application.port.inbound;

import nl.teamrocket.core.venue.application.query.AvailabilityResult;
import nl.teamrocket.core.venue.application.query.CheckAvailabilityQuery;
import nl.teamrocket.core.venue.application.query.FindVenuesNearbyQuery;
import nl.teamrocket.core.venue.application.query.GetVenueQuery;
import nl.teamrocket.core.venue.domain.model.Venue;

import java.util.List;

/**
 * Inbound port: read-pad. Communicatiedoc §3.2.2 endpoints
 * {@code GET /venues/{id}} en {@code GET /venues/{id}/availability}.
 */
public interface VenueQueryPort {

    Venue findById(GetVenueQuery query);

    List<Venue> findAll();

    List<Venue> findNearby(FindVenuesNearbyQuery query);

    AvailabilityResult checkAvailability(CheckAvailabilityQuery query);
}
