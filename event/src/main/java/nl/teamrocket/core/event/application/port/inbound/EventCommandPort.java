package nl.teamrocket.core.event.application.port.inbound;

import nl.teamrocket.core.event.application.command.CancelEventCommand;
import nl.teamrocket.core.event.application.command.CreateEventCommand;
import nl.teamrocket.core.event.application.command.PublishEventCommand;
import nl.teamrocket.core.event.application.command.UpdateEventCommand;
import nl.teamrocket.core.event.domain.model.Event;

import java.util.UUID;

/**
 * Inbound port: use-cases die het Event-aggregate muteren.
 *
 * Architectuurdoc §4.2: primary adapters (REST, AMQP-listener, scheduler) roepen
 * altijd via deze interface aan; ze kennen nooit de implementatie.
 */
public interface EventCommandPort {

    Event create(CreateEventCommand command);

    Event update(UpdateEventCommand command);

    Event publish(PublishEventCommand command);

    Event cancel(CancelEventCommand command);

    void delete(UUID eventId);

    /**
     * Triggert {@code EventStatusPolicy}; promoveert events naar COMPLETED zodra
     * hun eindtijd verstreken is. Aangeroepen door scheduler of admin endpoint.
     */
    Event autoStatusUpdate(UUID eventId);
}
