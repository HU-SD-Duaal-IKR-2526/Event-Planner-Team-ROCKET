package nl.teamrocket.core.venue.application.query;

import nl.teamrocket.core.venue.domain.model.GeoLocation;

/**
 * Geospatiale zoekquery: alle venues binnen {@code radiusKm} van {@code center}.
 * Data-distributiedoc §2.3.2: in productie PostGIS ST_DWithin; in deze
 * implementatie Haversine in-memory.
 */
public record FindVenuesNearbyQuery(GeoLocation center, double radiusKm) {

    public FindVenuesNearbyQuery {
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("radiusKm moet > 0, was " + radiusKm);
        }
        if (radiusKm > 1000.0) {
            throw new IllegalArgumentException("radiusKm max 1000 km voor performance");
        }
    }
}
