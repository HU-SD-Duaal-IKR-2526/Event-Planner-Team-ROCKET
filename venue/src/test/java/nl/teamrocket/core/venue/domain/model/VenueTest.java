package nl.teamrocket.core.venue.domain.model;

import nl.teamrocket.core.venue.domain.exception.InvalidCapacityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VenueTest {

    private final UUID id = UUID.randomUUID();
    private final Address address = new Address("Padualaan", "99", "3584 CH", "Utrecht", "NL");
    private final GeoLocation utrecht = new GeoLocation(52.0852, 5.1740);

    @Test
    @DisplayName("cacheNew: maakt aggregate met initiele velden")
    void cacheNewCreatesAggregate() {
        Venue v = Venue.cacheNew(id, "HU", address, utrecht, 100,
                EnumSet.of(Amenity.WIFI), 1L);

        assertThat(v.getId()).isEqualTo(id);
        assertThat(v.getName()).isEqualTo("HU");
        assertThat(v.getCapacity()).isEqualTo(100);
        assertThat(v.getAmenities()).containsExactly(Amenity.WIFI);
        assertThat(v.getExternalVersion()).isEqualTo(1L);
        assertThat(v.getCachedAt()).isNotNull();
    }

    @Test
    @DisplayName("cacheNew: weigert capaciteit <= 0")
    void cacheNewRejectsZeroCapacity() {
        assertThatThrownBy(() -> Venue.cacheNew(id, "HU", address, utrecht, 0,
                EnumSet.noneOf(Amenity.class), 1L))
                .isInstanceOf(InvalidCapacityException.class);
    }

    @Test
    @DisplayName("hasCapacityFor: true bij <= capacity, false anders")
    void capacityCheck() {
        Venue v = Venue.cacheNew(id, "HU", address, utrecht, 100,
                EnumSet.noneOf(Amenity.class), 1L);
        assertThat(v.hasCapacityFor(50)).isTrue();
        assertThat(v.hasCapacityFor(100)).isTrue();
        assertThat(v.hasCapacityFor(101)).isFalse();
        assertThat(v.hasCapacityFor(0)).isFalse();
    }

    @Test
    @DisplayName("refreshFromExternal: hogere versie werkt cache bij")
    void refreshAppliesNewerVersion() {
        Venue v = Venue.cacheNew(id, "HU", address, utrecht, 100,
                EnumSet.of(Amenity.WIFI), 1L);

        boolean updated = v.refreshFromExternal(
                "HU Padualaan 99", address, utrecht, 150,
                EnumSet.of(Amenity.WIFI, Amenity.AV_EQUIPMENT), 2L);

        assertThat(updated).isTrue();
        assertThat(v.getName()).isEqualTo("HU Padualaan 99");
        assertThat(v.getCapacity()).isEqualTo(150);
        assertThat(v.getAmenities()).containsExactlyInAnyOrder(Amenity.WIFI, Amenity.AV_EQUIPMENT);
        assertThat(v.getExternalVersion()).isEqualTo(2L);
    }

    @Test
    @DisplayName("refreshFromExternal: oudere of gelijke versie wordt genegeerd (idempotent)")
    void refreshIgnoresOlderVersion() {
        Venue v = Venue.cacheNew(id, "HU", address, utrecht, 100,
                EnumSet.of(Amenity.WIFI), 5L);

        boolean updated1 = v.refreshFromExternal("Other", address, utrecht, 200,
                EnumSet.noneOf(Amenity.class), 5L); // gelijke versie
        boolean updated2 = v.refreshFromExternal("Other", address, utrecht, 200,
                EnumSet.noneOf(Amenity.class), 3L); // oudere versie

        assertThat(updated1).isFalse();
        assertThat(updated2).isFalse();
        assertThat(v.getName()).isEqualTo("HU");
        assertThat(v.getCapacity()).isEqualTo(100);
    }

    @Test
    @DisplayName("GeoLocation: weigert invalide lat/lon")
    void geoLocationInvariants() {
        assertThatThrownBy(() -> new GeoLocation(91, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeoLocation(-91, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeoLocation(0, 181)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeoLocation(0, -181)).isInstanceOf(IllegalArgumentException.class);
    }
}
