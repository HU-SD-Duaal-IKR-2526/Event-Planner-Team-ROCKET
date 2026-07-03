package nl.hu.ikr.registration.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class EventCapacityTest {

    @Test
    void hasRoom_alsEerNogPlaats_geeftTrue() {
        EventCapacity cap = new EventCapacity(UUID.randomUUID(), 10, true);
        // 5 bezette seats, 0 plusOnes → 5+0+1=6 <= 10
        assertThat(cap.hasRoom(5, 0)).isTrue();
    }

    @Test
    void hasRoom_alsVol_geeftFalse() {
        EventCapacity cap = new EventCapacity(UUID.randomUUID(), 5, true);
        // 5 bezette seats, 0 plusOnes → 5+0+1=6 > 5
        assertThat(cap.hasRoom(5, 0)).isFalse();
    }

    @Test
    void hasRoom_metPlusOnes_rekenrtCorrect() {
        EventCapacity cap = new EventCapacity(UUID.randomUUID(), 10, true);
        // 8 bezette seats, 1 plusOne → 8+1+1=10 <= 10
        assertThat(cap.hasRoom(8, 1)).isTrue();
    }

    @Test
    void hasRoom_metPlusOnes_alsTeVeel_geeftFalse() {
        EventCapacity cap = new EventCapacity(UUID.randomUUID(), 10, true);
        // 8 bezette seats, 2 plusOnes → 8+2+1=11 > 10
        assertThat(cap.hasRoom(8, 2)).isFalse();
    }

    @Test
    void hasRoom_bijCapacityNul_geeftAltijdFalse() {
        EventCapacity cap = new EventCapacity(UUID.randomUUID(), 0, true);
        assertThat(cap.hasRoom(0, 0)).isFalse();
    }
}

