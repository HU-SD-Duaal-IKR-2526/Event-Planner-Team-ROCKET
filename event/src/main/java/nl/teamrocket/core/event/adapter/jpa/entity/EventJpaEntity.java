package nl.teamrocket.core.event.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.teamrocket.core.event.domain.model.EventStatus;
import nl.teamrocket.core.event.domain.model.Visibility;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA-persistence representatie. Bewust gescheiden van het domain {@code Event}
 * zodat JPA-annotaties en lifecycle de domeinregels niet vervuilen
 * (architectuurdoc §4.2: secondary adapter laag).
 */
@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_status", columnList = "status"),
        @Index(name = "idx_events_organizer", columnList = "organizer_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EventJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Column(name = "venue_id")
    private UUID venueId;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    // ── EventMetadata: ingebed als kolommen (geen aparte tabel) ──────────────
    @Column(name = "dress_code")
    private String dressCode;

    @Column(name = "speaker")
    private String speaker;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "livestream_url")
    private String livestreamUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 16)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private EventStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Version
    @Column(name = "version")
    private Long version;
}
