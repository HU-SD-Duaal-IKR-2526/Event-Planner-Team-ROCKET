package nl.teamrocket.core.event.application.port.outbound;

import nl.teamrocket.core.event.domain.model.Event;
import nl.teamrocket.core.event.domain.model.EventStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: persistence-abstractie van het Event-aggregate.
 *
 * Architectuurdoc §4.2: deze interface leeft in de application laag; concrete
 * implementatie (JPA/Postgres) zit in {@code adapter.jpa}. Daardoor kunnen
 * domain- en applicationtests draaien zonder Spring of database (mock deze port).
 */
public interface EventRepository {

    Event save(Event event);

    Optional<Event> findById(UUID id);

    List<Event> findAll();

    /** Filter-variant; null-velden worden niet meegefilterd. */
    List<Event> findByFilter(EventStatus status, UUID organizerId);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
