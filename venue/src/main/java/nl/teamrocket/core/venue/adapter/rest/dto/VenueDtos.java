package nl.teamrocket.core.venue.adapter.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import nl.teamrocket.core.venue.application.query.AvailabilityResult;
import nl.teamrocket.core.venue.domain.model.Address;
import nl.teamrocket.core.venue.domain.model.Amenity;
import nl.teamrocket.core.venue.domain.model.GeoLocation;
import nl.teamrocket.core.venue.domain.model.Venue;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Verzamelklasse voor alle REST DTOs van de Venue-module. Domeintypes lekken
 * niet door de REST-grens.
 */
public final class VenueDtos {

    private VenueDtos() {}

    public record AddressDto(
            String street,
            String houseNumber,
            String postalCode,
            @NotBlank String city,
            @NotBlank String country
    ) {
        public Address toDomain() {
            return new Address(street, houseNumber, postalCode, city, country);
        }
        public static AddressDto from(Address a) {
            return new AddressDto(a.street(), a.houseNumber(), a.postalCode(), a.city(), a.country());
        }
    }

    public record GeoLocationDto(
            @DecimalMin("-90") @DecimalMax("90") double latitude,
            @DecimalMin("-180") @DecimalMax("180") double longitude
    ) {
        public GeoLocation toDomain() { return new GeoLocation(latitude, longitude); }
        public static GeoLocationDto from(GeoLocation l) {
            return new GeoLocationDto(l.latitude(), l.longitude());
        }
    }

    public record RegisterVenueRequest(
            @NotNull UUID externalId,
            @NotBlank @Size(max = 200) String name,
            @Valid @NotNull AddressDto address,
            @Valid GeoLocationDto location,
            @Positive int capacity,
            Set<Amenity> amenities,
            @PositiveOrZero long externalVersion
    ) {}

    public record VenueResponse(
            UUID id,
            String name,
            AddressDto address,
            GeoLocationDto location,
            int capacity,
            Set<Amenity> amenities,
            long externalVersion,
            Instant cachedAt
    ) {
        public static VenueResponse from(Venue v) {
            return new VenueResponse(
                    v.getId(),
                    v.getName(),
                    AddressDto.from(v.getAddress()),
                    v.getLocation() != null ? GeoLocationDto.from(v.getLocation()) : null,
                    v.getCapacity(),
                    v.getAmenities(),
                    v.getExternalVersion(),
                    v.getCachedAt()
            );
        }
    }

    public record AvailabilityResponse(
            boolean available,
            UUID conflictingBookingId,
            String reason
    ) {
        public static AvailabilityResponse from(AvailabilityResult r) {
            return new AvailabilityResponse(r.available(), r.conflictingBookingId(), r.reason());
        }
    }

    public record ApiError(int status, String message) {}
}
