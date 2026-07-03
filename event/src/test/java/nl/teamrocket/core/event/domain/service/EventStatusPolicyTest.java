package nl.teamrocket.core.event.domain.service;

import nl.teamrocket.core.event.domain.model.Event;
import nl.teamrocket.core.event.domain.model.EventMetadata;
import nl.teamrocket.core.event.domain.model.EventStatus;
import nl.teamrocket.core.event.domain.model.EventTimeSlot;
import nl.teamrocket.core.event.domain.model.Visibility;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventStatusPolicyTest {

    @Test
    void completedWhenEndsAtPassed() {
        Instant past = Instant.now().minus(2, ChronoUnit.DAYS);
        Event event = Event.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Past event", null,
                new EventTimeSlot(past, past.plus(1, ChronoUnit.HOURS)),
                EventMetadata.empty(), Visibility.PUBLIC);
        // PLANNED zetten via reflection-free pad: nieuwe aggregate met PLANNED status
        // niet mogelijk via public API als slot in verleden ligt, dus pak DRAFT in past.
        assertThat(EventStatusPolicy.nextStatusAt(event, Instant.now()))
                .isEqualTo(EventStatus.COMPLETED);
    }

    @Test
    void noTransitionWhenEventInFuture() {
        Instant future = Instant.now().plus(2, ChronoUnit.DAYS);
        Event event = Event.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Future event", null,
                new EventTimeSlot(future, future.plus(2, ChronoUnit.HOURS)),
                EventMetadata.empty(), Visibility.PUBLIC);
        assertThat(EventStatusPolicy.nextStatusAt(event, Instant.now())).isNull();
    }

    @Test
    void cancelledStaysCancelled() {
        Instant past = Instant.now().minus(2, ChronoUnit.DAYS);
        Event event = Event.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Cancelled past event", null,
                new EventTimeSlot(past, past.plus(1, ChronoUnit.HOURS)),
                EventMetadata.empty(), Visibility.PUBLIC);
        event.cancel("test");
        assertThat(EventStatusPolicy.nextStatusAt(event, Instant.now())).isNull();
    }
}
