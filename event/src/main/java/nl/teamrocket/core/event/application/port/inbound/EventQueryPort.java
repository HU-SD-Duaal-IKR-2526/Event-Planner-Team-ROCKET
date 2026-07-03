package nl.teamrocket.core.event.application.port.inbound;

import nl.teamrocket.core.event.application.query.GetEventQuery;
import nl.teamrocket.core.event.application.query.ListEventsQuery;
import nl.teamrocket.core.event.domain.model.Event;

import java.util.List;

/**
 * Inbound port: read-pad (CQRS-light, architectuurdoc §4.1.2).
 */
public interface EventQueryPort {

    Event findById(GetEventQuery query);

    List<Event> list(ListEventsQuery query);
}
