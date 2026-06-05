package nl.hu.ikr.schedule.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ScheduleTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final Instant EVENT_START = Instant.parse("2025-06-01T08:00:00Z");
    private static final Instant EVENT_END   = Instant.parse("2025-06-01T18:00:00Z");

    private Schedule schedule;

    @BeforeEach
    void setup() {
        schedule = Schedule.create(EVENT_ID, "Test Event", EVENT_START, EVENT_END);
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    @Test
    void create_zettAlleVeldenCorrect() {
        assertThat(schedule.getEventId()).isEqualTo(EVENT_ID);
        assertThat(schedule.getEventTitle()).isEqualTo("Test Event");
        assertThat(schedule.getEventStartsAt()).isEqualTo(EVENT_START);
        assertThat(schedule.getEventEndsAt()).isEqualTo(EVENT_END);
        assertThat(schedule.getHeadcount()).isZero();
        assertThat(schedule.getSessions()).isEmpty();
        assertThat(schedule.getLastUpdated()).isNotNull();
    }

    // ── addSession ────────────────────────────────────────────────────────────

    @Test
    void addSession_geenOverlap_voegeSessieToe() {
        UUID roomId    = UUID.randomUUID();
        UUID speakerId = UUID.randomUUID();
        TimeSlot slot  = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"));

        Session session = schedule.addSession("Keynote", roomId, speakerId, slot);

        assertThat(schedule.getSessions()).hasSize(1);
        assertThat(session.getTitle()).isEqualTo("Keynote");
        assertThat(session.getRoomId()).isEqualTo(roomId);
        assertThat(session.getSpeakerId()).isEqualTo(speakerId);
    }

    @Test
    void addSession_roomOverlap_gooit() {
        UUID roomId    = UUID.randomUUID();
        UUID speaker1  = UUID.randomUUID();
        UUID speaker2  = UUID.randomUUID();
        TimeSlot slot1 = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:30:00Z"));
        TimeSlot slot2 = new TimeSlot(
                Instant.parse("2025-06-01T10:00:00Z"),
                Instant.parse("2025-06-01T11:00:00Z")); // overlapt

        schedule.addSession("Sessie A", roomId, speaker1, slot1);

        assertThatThrownBy(() -> schedule.addSession("Sessie B", roomId, speaker2, slot2))
                .isInstanceOf(Schedule.RoomOverlapException.class);
        assertThat(schedule.getSessions()).hasSize(1); // niet toegevoegd
    }

    @Test
    void addSession_speakerOverlap_gooit() {
        UUID room1     = UUID.randomUUID();
        UUID room2     = UUID.randomUUID();
        UUID speakerId = UUID.randomUUID();
        TimeSlot slot1 = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:30:00Z"));
        TimeSlot slot2 = new TimeSlot(
                Instant.parse("2025-06-01T10:00:00Z"),
                Instant.parse("2025-06-01T11:00:00Z")); // overlapt

        schedule.addSession("Sessie A", room1, speakerId, slot1);

        assertThatThrownBy(() -> schedule.addSession("Sessie B", room2, speakerId, slot2))
                .isInstanceOf(Schedule.SpeakerOverlapException.class);
    }

    @Test
    void addSession_aangrenzendeSlotsZelfdeRuimte_geeftGeenOverlap() {
        UUID roomId    = UUID.randomUUID();
        UUID speaker1  = UUID.randomUUID();
        UUID speaker2  = UUID.randomUUID();
        TimeSlot slot1 = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"));
        TimeSlot slot2 = new TimeSlot(
                Instant.parse("2025-06-01T10:00:00Z"), // begint precies wanneer slot1 eindigt
                Instant.parse("2025-06-01T11:00:00Z"));

        schedule.addSession("Sessie A", roomId, speaker1, slot1);
        assertThatNoException().isThrownBy(
                () -> schedule.addSession("Sessie B", roomId, speaker2, slot2));
        assertThat(schedule.getSessions()).hasSize(2);
    }

    @Test
    void addSession_verschilendeRuimteEnSpreker_geenFout() {
        TimeSlot slot = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"));

        schedule.addSession("Sessie A", UUID.randomUUID(), UUID.randomUUID(), slot);
        assertThatNoException().isThrownBy(
                () -> schedule.addSession("Sessie B", UUID.randomUUID(), UUID.randomUUID(), slot));
        assertThat(schedule.getSessions()).hasSize(2);
    }

    @Test
    void addSession_updatesLastUpdated() throws InterruptedException {
        Instant before = schedule.getLastUpdated();
        Thread.sleep(5); // zorg voor meetbaar tijdsverschil
        TimeSlot slot = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"));
        schedule.addSession("Sessie", UUID.randomUUID(), UUID.randomUUID(), slot);
        assertThat(schedule.getLastUpdated()).isAfter(before);
    }

    // ── applyHeadcountDelta ───────────────────────────────────────────────────

    @Test
    void applyHeadcountDelta_positief_verhoogt() {
        schedule.applyHeadcountDelta(3);
        assertThat(schedule.getHeadcount()).isEqualTo(3);
    }

    @Test
    void applyHeadcountDelta_negatief_verlaagt() {
        schedule.applyHeadcountDelta(5);
        schedule.applyHeadcountDelta(-2);
        assertThat(schedule.getHeadcount()).isEqualTo(3);
    }

    @Test
    void applyHeadcountDelta_onderNul_wordtNul() {
        schedule.applyHeadcountDelta(2);
        schedule.applyHeadcountDelta(-10);
        assertThat(schedule.getHeadcount()).isZero();
    }

    @Test
    void applyHeadcountDelta_nulDelta_blijftGelijk() {
        schedule.applyHeadcountDelta(5);
        schedule.applyHeadcountDelta(0);
        assertThat(schedule.getHeadcount()).isEqualTo(5);
    }

    // ── setEventTitle ─────────────────────────────────────────────────────────

    @Test
    void setEventTitle_updatesTitle() {
        schedule.setEventTitle("Nieuw Titel");
        assertThat(schedule.getEventTitle()).isEqualTo("Nieuw Titel");
    }

    // ── markCancelled ─────────────────────────────────────────────────────────

    @Test
    void markCancelled_verwijdertAlleSessies() {
        TimeSlot slot = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"));
        schedule.addSession("Sessie", UUID.randomUUID(), UUID.randomUUID(), slot);
        assertThat(schedule.getSessions()).hasSize(1);

        schedule.markCancelled();

        assertThat(schedule.getSessions()).isEmpty();
    }
}

