package nl.teamrocket.core.event.domain.model;

import nl.teamrocket.core.event.domain.exception.InvalidEventTimeSlotException;

import java.time.Instant;
import java.util.Objects;

/**
 * Value Object: tijdvenster van een event.
 *
 * Invarianten (architectuurdoc §5.4.1 / data-distributiedoc §2.3.1):
 *  - {@code startsAt} en {@code endsAt} zijn verplicht en in UTC.
 *  - {@code endsAt} ligt strikt na {@code startsAt}.
 *
 * Tijden worden bewust opgeslagen als {@link Instant} (UTC), niet als {@link java.util.Date};
 * de brownfield-applicatie gebruikte {@code java.util.Date} zonder timezone, wat tot
 * subtiele bugs leidt bij gebruikers in andere tijdzones.
 */
public record EventTimeSlot(Instant startsAt, Instant endsAt) {

    public EventTimeSlot {
        Objects.requireNonNull(startsAt, "startsAt is verplicht");
        Objects.requireNonNull(endsAt,   "endsAt is verplicht");
        if (!endsAt.isAfter(startsAt)) {
            throw new InvalidEventTimeSlotException(
                    "endsAt (" + endsAt + ") moet na startsAt (" + startsAt + ") liggen");
        }
    }

    public boolean isInPast(Instant now) {
        return endsAt.isBefore(now);
    }

    public boolean isInFuture(Instant now) {
        return startsAt.isAfter(now);
    }
}
