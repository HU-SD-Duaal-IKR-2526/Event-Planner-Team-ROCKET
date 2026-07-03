package nl.teamrocket.core.venue.domain.model;

/**
 * Value Object: WGS-84 coördinaat (latitude/longitude in graden).
 *
 * In de doelarchitectuur (data-distributiedoc §2.3.2) wordt geospatiaal zoeken
 * uitgevoerd door PostGIS (ST_DWithin). In deze implementatie wordt de afstand
 * client-side berekend via {@link nl.teamrocket.core.venue.domain.service.DistanceCalculator}
 * (Haversine), zodat het systeem ook draait zonder PostGIS-extensie — zie
 * README §"Afwijkingen van het ontwerp".
 *
 * Invarianten:
 *  - latitude  ∈ [-90, 90]
 *  - longitude ∈ [-180, 180]
 */
public record GeoLocation(double latitude, double longitude) {

    public GeoLocation {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("latitude moet tussen -90 en 90 liggen, was " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("longitude moet tussen -180 en 180 liggen, was " + longitude);
        }
    }
}
