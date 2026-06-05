package nl.teamrocket.core.venue.domain.service;

import nl.teamrocket.core.venue.domain.model.GeoLocation;

/**
 * Domain Service: Haversine-afstand tussen twee WGS-84 punten in kilometers.
 *
 * Wordt door {@code VenueQueryService} gebruikt om "venues binnen N km"
 * te bepalen wanneer PostGIS niet beschikbaar is (zie README §"Afwijkingen").
 * In de doelarchitectuur (data-distributiedoc §2.3.2) doet PostGIS dit via
 * {@code ST_DWithin} in de database.
 */
public final class DistanceCalculator {

    /** Aardstraal in km — gemiddelde, voldoende nauwkeurig voor venue-zoek. */
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private DistanceCalculator() {}

    public static double distanceInKm(GeoLocation a, GeoLocation b) {
        double lat1 = Math.toRadians(a.latitude());
        double lat2 = Math.toRadians(b.latitude());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b.longitude() - a.longitude());

        double sinDLat = Math.sin(dLat / 2.0);
        double sinDLon = Math.sin(dLon / 2.0);

        double aa = sinDLat * sinDLat
                + Math.cos(lat1) * Math.cos(lat2) * sinDLon * sinDLon;
        double c = 2.0 * Math.atan2(Math.sqrt(aa), Math.sqrt(1.0 - aa));

        return EARTH_RADIUS_KM * c;
    }
}
