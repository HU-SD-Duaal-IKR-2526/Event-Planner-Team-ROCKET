package nl.hu.ikr.schedule.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Schedule aggregate root — beheert sessies voor één event.
 * Overlap-detectie en headcount zijn de kernverantwoordelijkheden.
 */
@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "event_title")
    private String eventTitle;

    @Column(name = "event_starts_at")
    private Instant eventStartsAt;

    @Column(name = "event_ends_at")
    private Instant eventEndsAt;

    @Column(nullable = false)
    private int headcount = 0;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private List<Session> sessions = new ArrayList<>();

    protected Schedule() {}

    // ── Factory ────────────────────────────────────────────────────────────────

    public static Schedule create(UUID eventId, String title, Instant startsAt, Instant endsAt) {
        Schedule s = new Schedule();
        s.id = UUID.randomUUID();
        s.eventId = eventId;
        s.eventTitle = title;
        s.eventStartsAt = startsAt;
        s.eventEndsAt = endsAt;
        s.lastUpdated = Instant.now();
        return s;
    }

    // ── Commands ───────────────────────────────────────────────────────────────

    /**
     * Voegt een sessie toe. Gooit exception bij room- of speaker-overlap.
     */
    public Session addSession(String title, UUID roomId, UUID speakerId, TimeSlot slot) {
        Session newSession = new Session(this.id, title, roomId, speakerId, slot);

        for (Session existing : sessions) {
            if (existing.roomOverlapsWith(newSession)) {
                throw new RoomOverlapException(
                        "Ruimte " + roomId + " is al bezet in tijdslot " + slot);
            }
            if (existing.speakerOverlapsWith(newSession)) {
                throw new SpeakerOverlapException(
                        "Spreker " + speakerId + " heeft al een sessie in tijdslot " + slot);
            }
        }

        sessions.add(newSession);
        this.lastUpdated = Instant.now();
        return newSession;
    }

    /**
     * Verhoog of verlaag headcount; minimum is 0.
     */
    public void applyHeadcountDelta(int delta) {
        this.headcount = Math.max(0, this.headcount + delta);
        this.lastUpdated = Instant.now();
    }

    public void setEventTitle(String title) {
        this.eventTitle = title;
        this.lastUpdated = Instant.now();
    }

    public void markCancelled() {
        this.sessions.clear();
        this.lastUpdated = Instant.now();
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public UUID getId()            { return id; }
    public UUID getEventId()       { return eventId; }
    public String getEventTitle()  { return eventTitle; }
    public Instant getEventStartsAt() { return eventStartsAt; }
    public Instant getEventEndsAt()   { return eventEndsAt; }
    public int getHeadcount()      { return headcount; }
    public Instant getLastUpdated() { return lastUpdated; }
    public Long getVersion()       { return version; }
    public List<Session> getSessions() { return Collections.unmodifiableList(sessions); }

    // ── Excepties ──────────────────────────────────────────────────────────────

    public static class RoomOverlapException extends RuntimeException {
        public RoomOverlapException(String msg) { super(msg); }
    }

    public static class SpeakerOverlapException extends RuntimeException {
        public SpeakerOverlapException(String msg) { super(msg); }
    }
}

