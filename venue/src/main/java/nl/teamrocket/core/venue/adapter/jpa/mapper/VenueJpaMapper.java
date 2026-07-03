package nl.teamrocket.core.venue.adapter.jpa.mapper;

import nl.teamrocket.core.venue.adapter.jpa.entity.VenueJpaEntity;
import nl.teamrocket.core.venue.domain.model.Address;
import nl.teamrocket.core.venue.domain.model.GeoLocation;
import nl.teamrocket.core.venue.domain.model.Venue;

import java.util.HashSet;

/**
 * Mapping tussen domain {@link Venue} en JPA {@link VenueJpaEntity}.
 * Domein blijft JPA-vrij; mapping zit hier expliciet.
 */
public final class VenueJpaMapper {

    private VenueJpaMapper() {}

    public static VenueJpaEntity toEntity(Venue v) {
        Address a = v.getAddress();
        GeoLocation l = v.getLocation();
        return new VenueJpaEntity(
                v.getId(),
                v.getName(),
                a.street(), a.houseNumber(), a.postalCode(), a.city(), a.country(),
                l != null ? l.latitude() : null,
                l != null ? l.longitude() : null,
                v.getCapacity(),
                new HashSet<>(v.getAmenities()),
                v.getExternalVersion(),
                v.getCachedAt()
        );
    }

    public static void updateEntity(VenueJpaEntity entity, Venue v) {
        Address a = v.getAddress();
        GeoLocation l = v.getLocation();
        entity.setName(v.getName());
        entity.setStreet(a.street());
        entity.setHouseNumber(a.houseNumber());
        entity.setPostalCode(a.postalCode());
        entity.setCity(a.city());
        entity.setCountry(a.country());
        entity.setLatitude(l != null ? l.latitude() : null);
        entity.setLongitude(l != null ? l.longitude() : null);
        entity.setCapacity(v.getCapacity());
        entity.setAmenities(new HashSet<>(v.getAmenities()));
        entity.setExternalVersion(v.getExternalVersion());
        entity.setCachedAt(v.getCachedAt());
    }

    public static Venue toDomain(VenueJpaEntity e) {
        Address address = new Address(
                e.getStreet(), e.getHouseNumber(), e.getPostalCode(),
                e.getCity(), e.getCountry());
        GeoLocation location = (e.getLatitude() != null && e.getLongitude() != null)
                ? new GeoLocation(e.getLatitude(), e.getLongitude())
                : null;
        return Venue.reconstitute(
                e.getId(),
                e.getName(),
                address,
                location,
                e.getCapacity(),
                e.getAmenities(),
                e.getExternalVersion(),
                e.getCachedAt()
        );
    }
}
