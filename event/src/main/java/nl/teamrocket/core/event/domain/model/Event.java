package nl.teamrocket.core.event.domain.model;

import nl.teamrocket.core.event.domain.event.EventCancelled;
import nl.teamrocket.core.event.domain.event.EventCreated;
import nl.teamrocket.core.event.domain.event.EventDomainEvent;
import nl.teamrocket.core.event.domain.event.EventPublished;
import nl.teamrocket.core.event.domain.event.EventUpdated;
import nl.teamrocket.core.event.domain.exception.InvalidEventCapacityException;
import nl.teamrocket.core.event.domain.exception.InvalidEventStatusTransitionException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root: Event.
 *
 * Verantwoordelijkheid: bewaakt de invariants en levenscyclus van één event
 * (architectuurdoc §4.2 / §5.1 / §5.4.1).
 *
 * Invariants:
 *  - {@code id}, {@code organizerId}, {@code title} en {@code timeSlot} zijn altijd
 *    gezet vanaf het moment van aanmaak.
 *  - {@code endsAt} ligt strikt na {@code startsAt} (afgedwongen door {@link EventTimeSlot}).
 *  - Indien {@code metadata.capacity()} gezet is, dan {@code > 0}.
 *  - Status-transities zijn beperkt: DRAFT → PLANNED → CANCELLED | COMPLETED.
 *    Vanuit CANCELLED of COMPLETED zijn geen mutaties meer toegestaan.
 *  - Het venueId is een referentie-by-ID naar het Venue-aggregate (cross-context;
 *    nooit een direct object). Data-distributiedoc §2.3.2.
 *
 * Concurrency: het JPA-adapter mapt {@link #version} op {@code @Version} (optimistic
 * locking, data-distributiedoc §2.3.1). Conflicterende writes leiden tot
 * {@code OptimisticLockingFailureException} in de application laag.
 *
 * EXPLICIET NIET de verantwoordelijkheid van dit aggregate:
 *  - Het tellen van bevestigde registraties (dat doet Registration BC).
 *  - Het reserveren van een venue-tijdslot (dat is een synchrone call naar Venue BC
 *    vanuit de orchestration-saga, data-distributiedoc §5.2.3).
 *  - Het publiceren van events op de bus (dat doet de application laag via outbox).
 */
public class Event {

    private final UUID id;
    private final UUID organizerId;
    private UUID venueId;

    private String title;
    private String description;
    private EventTimeSlot timeSlot;
    private EventMetadata metadata;
    private Visibility visibility;
    private EventStatus status;

    private final Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;
    private Instant cancelledAt;
    private String cancellationReason;

    private Long version;

    /** Niet-persisted: door application laag opgehaald en gepubliceerd na save. */
    private final transient List<EventDomainEvent> domainEvents = new ArrayList<>();

    // ── Constructors ─────────────────────────────────────────────────────────

    private Event(UUID id, UUID organizerId, UUID venueId, String title, String description,
                  EventTimeSlot timeSlot, EventMetadata metadata, Visibility visibility,
                  EventStatus status, Instant createdAt, Instant updatedAt,
                  Instant publishedAt, Instant cancelledAt, String cancellationReason,
                  Long version) {
        this.id = id;
        this.organizerId = organizerId;
        this.venueId = venueId;
        this.title = title;
        this.description = description;
        this.timeSlot = timeSlot;
        this.metadata = metadata != null ? metadata : EventMetadata.empty();
        this.visibility = visibility != null ? visibility : Visibility.PRIVATE;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.publishedAt = publishedAt;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.version = version;
    }

    // ── Factories ────────────────────────────────────────────────────────────

    /**
     * Factory voor een nieuw event. Het event start in DRAFT (architectuurdoc §5.4.1
     * en data-distributiedoc §5.2.3: orchestration-saga maakt eerst tentatief aan).
     */
    public static Event create(UUID id, UUID organizerId, UUID venueId,
                               String title, String description,
                               EventTimeSlot timeSlot, EventMetadata metadata,
                               Visibility visibility) {
        Objects.requireNonNull(id, "id is verplicht");
        Objects.requireNonNull(organizerId, "organizerId is verplicht");
        validateTitle(title);
        Objects.requireNonNull(timeSlot, "timeSlot is verplicht");
        validateCapacity(metadata);

        Instant now = Instant.now();
        Event event = new Event(id, organizerId, venueId, title, description,
                timeSlot, metadata, visibility, EventStatus.DRAFT,
                now, now, null, null, null, null);
        event.registerEvent(new EventCreated(
                id, organizerId, venueId, title,
                timeSlot.startsAt(), timeSlot.endsAt(), now));
        return event;
    }

    /**
     * Reconstrueert een aggregate uit persistentie. Gebruikt door de JPA-adapter.
     * Triggert geen domain events.
     */
    public static Event reconstitute(UUID id, UUID organizerId, UUID venueId,
                                     String title, String description,
                                     EventTimeSlot timeSlot, EventMetadata metadata,
                                     Visibility visibility, EventStatus status,
                                     Instant createdAt, Instant updatedAt,
                                     Instant publishedAt, Instant cancelledAt,
                                     String cancellationReason, Long version) {
        return new Event(id, organizerId, venueId, title, description, timeSlot, metadata,
                visibility, status, createdAt, updatedAt, publishedAt, cancelledAt,
                cancellationReason, version);
    }

    // ── Business behaviour ───────────────────────────────────────────────────

    /**
     * Wijzigt mutable velden. Alleen toegestaan in DRAFT of PLANNED.
     * Pass {@code null} voor velden die niet moeten wijzigen.
     */
    public void update(String newTitle, String newDescription, EventTimeSlot newTimeSlot,
                       EventMetadata newMetadata, Visibility newVisibility) {
        assertMutable();
        if (newTitle != null) {
            validateTitle(newTitle);
            this.title = newTitle;
        }
        if (newDescription != null) {
            this.description = newDescription;
        }
        if (newTimeSlot != null) {
            this.timeSlot = newTimeSlot;
        }
        if (newMetadata != null) {
            validateCapacity(newMetadata);
            this.metadata = newMetadata;
        }
        if (newVisibility != null) {
            this.visibility = newVisibility;
        }
        this.updatedAt = Instant.now();
        registerEvent(new EventUpdated(id, title, description,
                timeSlot.startsAt(), timeSlot.endsAt(),
                metadata != null ? metadata.capacity() : null,
                version != null ? version : 0L, updatedAt));
    }

    /**
     * Publiceert het event: DRAFT → PLANNED. Eis: een tijdslot in de toekomst en
     * een gekoppelde venueId (data-distributiedoc §5.2.3, orchestration-saga
     * heeft op dit punt de venue al bevestigd).
     */
    public void publish(Instant now) {
        assertTransition(EventStatus.PLANNED);
        if (timeSlot.isInPast(now)) {
            throw new InvalidEventStatusTransitionException(status, EventStatus.PLANNED);
        }
        this.status = EventStatus.PLANNED;
        this.publishedAt = now;
        this.updatedAt = now;
        registerEvent(new EventPublished(
                id, organizerId, venueId, title, description,
                timeSlot.startsAt(), timeSlot.endsAt(),
                metadata.capacity(), visibility, publishedAt, now));
    }

    /**
     * Annuleert een PLANNED of DRAFT event. Hierna geen mutaties meer mogelijk.
     */
    public void cancel(String reason) {
        if (status == EventStatus.CANCELLED || status == EventStatus.COMPLETED) {
            throw new InvalidEventStatusTransitionException(status, EventStatus.CANCELLED);
        }
        Instant now = Instant.now();
        this.status = EventStatus.CANCELLED;
        this.cancelledAt = now;
        this.cancellationReason = reason;
        this.updatedAt = now;
        registerEvent(new EventCancelled(id, reason, cancelledAt, now));
    }

    /**
     * Markeert het event als afgerond. Triggert geen domain event in deze versie;
     * Schedule/Audit kunnen dit afleiden uit de laatste {@link EventUpdated}.
     * Wordt aangeroepen door de scheduler via {@link EventStatusPolicy}.
     */
    public void complete() {
        assertTransition(EventStatus.COMPLETED);
        this.status = EventStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    // ── Invariant checks ─────────────────────────────────────────────────────

    private void assertMutable() {
        if (status == EventStatus.CANCELLED || status == EventStatus.COMPLETED) {
            throw new InvalidEventStatusTransitionException(status, status);
        }
    }

    private void assertTransition(EventStatus to) {
        boolean allowed = switch (status) {
            case DRAFT     -> to == EventStatus.PLANNED || to == EventStatus.CANCELLED;
            case PLANNED   -> to == EventStatus.CANCELLED || to == EventStatus.COMPLETED;
            case CANCELLED, COMPLETED -> false;
        };
        if (!allowed) {
            throw new InvalidEventStatusTransitionException(status, to);
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 120) {
            throw new IllegalArgumentException("title is verplicht en max 120 chars");
        }
    }

    private static void validateCapacity(EventMetadata metadata) {
        if (metadata != null && metadata.capacity() != null && metadata.capacity() <= 0) {
            throw new InvalidEventCapacityException(metadata.capacity());
        }
    }

    // ── Domain events ────────────────────────────────────────────────────────

    private void registerEvent(EventDomainEvent event) {
        domainEvents.add(event);
    }

    public List<EventDomainEvent> pullDomainEvents() {
        List<EventDomainEvent> copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    // ── Getters (geen setters: state wijzigt enkel via business methods) ─────

    public UUID getId() { return id; }
    public UUID getOrganizerId() { return organizerId; }
    public UUID getVenueId() { return venueId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public EventTimeSlot getTimeSlot() { return timeSlot; }
    public EventMetadata getMetadata() { return metadata; }
    public Visibility getVisibility() { return visibility; }
    public EventStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Event other && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
