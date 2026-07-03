package nl.hu.ikr.schedule.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Projectie-entiteit: komende events per gebruiker.
 * Gevoed door registration.reserved.v1 events.
 */
@Entity
@Table(name = "user_upcoming_events")
public class UserUpcomingEvent {

    @EmbeddedId
    private UserUpcomingEventId id;

    @Column
    private String title;

    @Column(name = "starts_at")
    private Instant startsAt;

    protected UserUpcomingEvent() {}

    public UserUpcomingEvent(UUID userId, UUID eventId, String title, Instant startsAt) {
        this.id = new UserUpcomingEventId(userId, eventId);
        this.title = title;
        this.startsAt = startsAt;
    }

    public UUID getUserId()   { return id.getUserId(); }
    public UUID getEventId()  { return id.getEventId(); }
    public String getTitle()  { return title; }
    public Instant getStartsAt() { return startsAt; }

    @Embeddable
    public static class UserUpcomingEventId implements java.io.Serializable {
        @Column(name = "user_id")
        private UUID userId;

        @Column(name = "event_id")
        private UUID eventId;

        protected UserUpcomingEventId() {}

        public UserUpcomingEventId(UUID userId, UUID eventId) {
            this.userId = userId;
            this.eventId = eventId;
        }

        public UUID getUserId()  { return userId; }
        public UUID getEventId() { return eventId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserUpcomingEventId that)) return false;
            return java.util.Objects.equals(userId, that.userId) &&
                   java.util.Objects.equals(eventId, that.eventId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(userId, eventId);
        }
    }
}

