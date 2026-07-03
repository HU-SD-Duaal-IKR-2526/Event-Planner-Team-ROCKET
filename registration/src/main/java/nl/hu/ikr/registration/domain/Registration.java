package nl.hu.ikr.registration.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @Column(name = "plus_ones", nullable = false)
    private int plusOnes;

    @Column
    private String notes;

    @Column(nullable = false)
    private String channel;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Registration() {}

    /**
     * Factory method — gebruik dit in plaats van de constructor.
     */
    public static Registration create(UUID eventId,
                                      UUID userId,
                                      int plusOnes,
                                      String notes,
                                      String channel,
                                      String idempotencyKey) {
        Registration r = new Registration();
        r.id = UUID.randomUUID();
        r.eventId = eventId;
        r.userId = userId;
        r.plusOnes = plusOnes;
        r.notes = notes;
        r.channel = channel;
        r.idempotencyKey = idempotencyKey;
        r.status = RegistrationStatus.REQUESTED;
        r.createdAt = Instant.now();
        r.updatedAt = r.createdAt;
        return r;
    }

    // ── State transitions ──────────────────────────────────────────────────────

    public void reserve() {
        assertStatus(RegistrationStatus.REQUESTED, RegistrationStatus.WAITLISTED);
        this.status = RegistrationStatus.RESERVED;
        this.updatedAt = Instant.now();
    }

    public void confirm() {
        assertStatus(RegistrationStatus.RESERVED);
        this.status = RegistrationStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void waitlist() {
        assertStatus(RegistrationStatus.REQUESTED);
        this.status = RegistrationStatus.WAITLISTED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.status == RegistrationStatus.CANCELLED) {
            return; // idempotent
        }
        assertStatus(RegistrationStatus.RESERVED, RegistrationStatus.CONFIRMED,
                     RegistrationStatus.REQUESTED, RegistrationStatus.WAITLISTED);
        this.status = RegistrationStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void markNoShow() {
        assertStatus(RegistrationStatus.CONFIRMED);
        this.status = RegistrationStatus.NO_SHOW;
        this.updatedAt = Instant.now();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void assertStatus(RegistrationStatus... allowed) {
        for (RegistrationStatus s : allowed) {
            if (this.status == s) return;
        }
        throw new IllegalStateException(
                "Ongeldige status-overgang vanuit " + this.status);
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public UUID getId()              { return id; }
    public UUID getEventId()         { return eventId; }
    public UUID getUserId()          { return userId; }
    public RegistrationStatus getStatus() { return status; }
    public int getPlusOnes()         { return plusOnes; }
    public String getNotes()         { return notes; }
    public String getChannel()       { return channel; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt()    { return createdAt; }
    public Instant getUpdatedAt()    { return updatedAt; }
    public Long getVersion()         { return version; }
}

