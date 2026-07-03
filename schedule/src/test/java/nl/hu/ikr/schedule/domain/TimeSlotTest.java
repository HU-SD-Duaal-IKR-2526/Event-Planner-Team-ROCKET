package nl.hu.ikr.schedule.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class TimeSlotTest {

    private static final Instant T1 = Instant.parse("2025-06-01T09:00:00Z");
    private static final Instant T2 = Instant.parse("2025-06-01T10:00:00Z");
    private static final Instant T3 = Instant.parse("2025-06-01T11:00:00Z");
    private static final Instant T4 = Instant.parse("2025-06-01T12:00:00Z");

    @Test
    void constructor_geldigeWaarden_maaktSlot() {
        assertThatNoException().isThrownBy(() -> new TimeSlot(T1, T2));
    }

    @Test
    void constructor_startNaEnd_gooit() {
        assertThatThrownBy(() -> new TimeSlot(T2, T1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_startGelijkAanEnd_gooit() {
        assertThatThrownBy(() -> new TimeSlot(T1, T1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_nullStart_gooit() {
        assertThatThrownBy(() -> new TimeSlot(null, T2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void overlapsWith_volleOverlap_geeftTrue() {
        TimeSlot groot = new TimeSlot(T1, T4);
        TimeSlot klein = new TimeSlot(T2, T3);
        assertThat(groot.overlapsWith(klein)).isTrue();
        assertThat(klein.overlapsWith(groot)).isTrue();
    }

    @Test
    void overlapsWith_gedeelteOverlapLinks_geeftTrue() {
        // A: T1–T3, B: T2–T4 → overlappen in T2–T3
        TimeSlot a = new TimeSlot(T1, T3);
        TimeSlot b = new TimeSlot(T2, T4);
        assertThat(a.overlapsWith(b)).isTrue();
    }

    @Test
    void overlapsWith_aangrenzend_geeftFalse() {
        // A eindigt precies waar B begint — geen overlap
        TimeSlot a = new TimeSlot(T1, T2);
        TimeSlot b = new TimeSlot(T2, T3);
        assertThat(a.overlapsWith(b)).isFalse();
        assertThat(b.overlapsWith(a)).isFalse();
    }

    @Test
    void overlapsWith_geenOverlap_geeftFalse() {
        TimeSlot a = new TimeSlot(T1, T2);
        TimeSlot b = new TimeSlot(T3, T4);
        assertThat(a.overlapsWith(b)).isFalse();
    }

    @Test
    void overlapsWith_zichzelf_geeftTrue() {
        TimeSlot a = new TimeSlot(T1, T3);
        assertThat(a.overlapsWith(a)).isTrue();
    }
}

