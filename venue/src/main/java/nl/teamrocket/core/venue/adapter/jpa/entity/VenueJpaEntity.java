package nl.teamrocket.core.venue.adapter.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.teamrocket.core.venue.domain.model.Amenity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA-persistence model. Bewust gescheiden van {@code Venue} aggregate
 * (architectuurdoc §4.2 — secondary adapter layer).
 *
 * Latitude/longitude liggen als losse {@code double}-kolommen; PostGIS-geometry
 * is in deze fase niet vereist (zie README §"Afwijkingen").
 */
@Entity
@Table(name = "venues", indexes = {
        @Index(name = "idx_venues_city", columnList = "city"),
        @Index(name = "idx_venues_country", columnList = "country")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VenueJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    // Address
    @Column(name = "street")
    private String street;

    @Column(name = "house_number")
    private String houseNumber;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "country", nullable = false)
    private String country;

    // GeoLocation
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Amenity.class)
    @CollectionTable(name = "venue_amenities", joinColumns = @JoinColumn(name = "venue_id"))
    @Column(name = "amenity", length = 32)
    @Enumerated(EnumType.STRING)
    private Set<Amenity> amenities = new HashSet<>();

    @Column(name = "external_version", nullable = false)
    private long externalVersion;

    @Column(name = "cached_at", nullable = false)
    private Instant cachedAt;
}
