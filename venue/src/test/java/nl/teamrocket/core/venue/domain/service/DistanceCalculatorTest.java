package nl.teamrocket.core.venue.domain.service;

import nl.teamrocket.core.venue.domain.model.GeoLocation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DistanceCalculatorTest {

    @Test
    void distanceUtrechtToAmsterdamIsAbout35Km() {
        GeoLocation utrecht = new GeoLocation(52.0907, 5.1214);
        GeoLocation amsterdam = new GeoLocation(52.3676, 4.9041);
        double km = DistanceCalculator.distanceInKm(utrecht, amsterdam);
        // Werkelijke afstand ~35 km; binnen 5% tolerantie.
        assertThat(km).isBetween(33.0, 37.0);
    }

    @Test
    void distanceSamePointIsZero() {
        GeoLocation p = new GeoLocation(52.0852, 5.1740);
        assertThat(DistanceCalculator.distanceInKm(p, p)).isEqualTo(0.0);
    }

    @Test
    void distanceIsSymmetric() {
        GeoLocation a = new GeoLocation(52.0, 5.0);
        GeoLocation b = new GeoLocation(53.0, 6.0);
        assertThat(DistanceCalculator.distanceInKm(a, b))
                .isEqualTo(DistanceCalculator.distanceInKm(b, a));
    }
}
