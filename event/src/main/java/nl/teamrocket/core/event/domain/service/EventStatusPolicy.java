package nl.teamrocket.core.event.domain.service;

import nl.teamrocket.core.event.domain.model.Event;
import nl.teamrocket.core.event.domain.model.EventStatus;

import java.time.Instant;

/**
 * Domain Service: bepaalt de automatische statusovergang op basis van de huidige tijd
 * (architectuurdoc §5.4.1, brownfield {@code EventService.autoStatusUpdate}).
 *
 * Aanroepers: een scheduler in de applicatie-laag periodiek voor PLANNED events,
 * of de {@code EventCommandService} direct na een mutatie. We hebben deze logica
 * bewust uit het aggregate getrokken (i.t.t. de brownfield-implementatie) omdat
 * het beslissingsgedrag is dat van buitenaf wordt getriggerd, niet een intrinsieke
 * invariant van het aggregate.
 */
public final class EventStatusPolicy {

    private EventStatusPolicy() {}

    /**
     * Geeft terug naar welke status het event zou moeten overgaan, of {@code null}
     * als er geen overgang nodig is. CANCELLED-events blijven altijd CANCELLED.
     */
    public static EventStatus nextStatusAt(Event event, Instant now) {
        if (event.getStatus() == EventStatus.CANCELLED) {
            return null;
        }
        boolean inPast = event.getTimeSlot().isInPast(now);
        if (inPast && event.getStatus() != EventStatus.COMPLETED) {
            return EventStatus.COMPLETED;
        }
        return null;
    }
}
