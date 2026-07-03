package nl.teamrocket.core.event.domain.model;

import nl.teamrocket.core.event.domain.event.EventCancelled;
import nl.teamrocket.core.event.domain.event.EventCreated;
import nl.teamrocket.core.event.domain.event.EventDomainEvent;
import nl.teamrocket.core.event.domain.event.EventPublished;
import nl.teamrocket.core.event.domain.exception.InvalidEventCapacityException;
import nl.teamrocket.core.event.domain.exception.InvalidEventStatusTransitionException;
import nl.teamrocket.core.event.domain.exception.InvalidEventTimeSlotException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure domain-tests: geen Spring, geen database. Snel en zonder externe afhankelijkheden,
 * conform de testdekking-eis op kritieke businesslogica (documentatiedoc §"Testdekking").
 */
class EventTest {

    private final UUID organizerId = UUID.randomUUID();
    private final UUID venueId = UUID.randomUUID();

    @Test
    @DisplayName("create: produceert DRAFT event en EventCreated domain event")
    void createProducesDraftAndDomainEvent() {
        Event event = newDraftEvent();

        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(event.getOrganizerId()).isEqualTo(organizerId);
        assertThat(event.getVenueId()).isEqualTo(venueId);

        List<EventDomainEvent> events = event.pullDomainEvents();
        assertThat(events).hasSize(1).first().isInstanceOf(EventCreated.class);
    }

    @Test
    @DisplayName("create: weigert capaciteit <= 0")
    void createRejectsZeroCapacity() {
        EventTimeSlot slot = newSlot();
        assertThatThrownBy(() -> Event.create(UUID.randomUUID(), organizerId, venueId,
                "title", null, slot, new EventMetadata(null, null, 0, null), Visibility.PUBLIC))
                .isInstanceOf(InvalidEventCapacityException.class);
    }

    @Test
    @DisplayName("EventTimeSlot: weigert endsAt < startsAt")
    void timeSlotRejectsEndBeforeStart() {
        Instant start = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant end = start.minus(1, ChronoUnit.HOURS);
        assertThatThrownBy(() -> new EventTimeSlot(start, end))
                .isInstanceOf(InvalidEventTimeSlotException.class);
    }

    @Test
    @DisplayName("publish: DRAFT -> PLANNED + EventPublished event")
    void publishTransitionsDraftToPlanned() {
        Event event = newDraftEvent();
        event.pullDomainEvents(); // clear create event

        event.publish(Instant.now());

        assertThat(event.getStatus()).isEqualTo(EventStatus.PLANNED);
        assertThat(event.getPublishedAt()).isNotNull();
        assertThat(event.pullDomainEvents()).hasSize(1).first().isInstanceOf(EventPublished.class);
    }

    @Test
    @DisplayName("publish: weigert events waarvan eindtijd in het verleden ligt")
    void publishRejectsPastEvent() {
        Instant pastStart = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant pastEnd = pastStart.plus(1, ChronoUnit.HOURS);
        Event event = Event.create(UUID.randomUUID(), organizerId, venueId,
                "old", null, new EventTimeSlot(pastStart, pastEnd),
                EventMetadata.empty(), Visibility.PUBLIC);
        assertThatThrownBy(() -> event.publish(Instant.now()))
                .isInstanceOf(InvalidEventStatusTransitionException.class);
    }

    @Test
    @DisplayName("cancel: vanuit PLANNED produceert EventCancelled en blokkeert verdere mutaties")
    void cancelLocksAggregate() {
        Event event = newDraftEvent();
        event.publish(Instant.now());
        event.pullDomainEvents();

        event.cancel("locatie viel weg");

        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(event.getCancellationReason()).isEqualTo("locatie viel weg");
        assertThat(event.pullDomainEvents()).hasSize(1).first().isInstanceOf(EventCancelled.class);

        assertThatThrownBy(() -> event.update("nieuwe titel", null, null, null, null))
                .isInstanceOf(InvalidEventStatusTransitionException.class);
        assertThatThrownBy(() -> event.cancel("nogmaals"))
                .isInstanceOf(InvalidEventStatusTransitionException.class);
    }

    @Test
    @DisplayName("status-machine: CANCELLED kan niet gepubliceerd worden")
    void cancelledCannotBePublished() {
        Event event = newDraftEvent();
        event.cancel("oeps");
        assertThatThrownBy(() -> event.publish(Instant.now()))
                .isInstanceOf(InvalidEventStatusTransitionException.class);
    }

    @Test
    @DisplayName("update: titel-wijziging wordt doorgevoerd in DRAFT")
    void updateTitleInDraft() {
        Event event = newDraftEvent();
        event.update("nieuwe titel", null, null, null, null);
        assertThat(event.getTitle()).isEqualTo("nieuwe titel");
    }

    private Event newDraftEvent() {
        return Event.create(UUID.randomUUID(), organizerId, venueId,
                "Conferentie Q2", "Jaarlijkse meetup", newSlot(),
                new EventMetadata("smart casual", "Joshua Larez", 100, null),
                Visibility.PUBLIC);
    }

    private EventTimeSlot newSlot() {
        Instant start = Instant.now().plus(7, ChronoUnit.DAYS);
        return new EventTimeSlot(start, start.plus(3, ChronoUnit.HOURS));
    }
}
