package nl.teamrocket.core.event.application.service;

import lombok.RequiredArgsConstructor;
import nl.teamrocket.core.event.application.port.inbound.EventQueryPort;
import nl.teamrocket.core.event.application.port.outbound.EventRepository;
import nl.teamrocket.core.event.application.query.GetEventQuery;
import nl.teamrocket.core.event.application.query.ListEventsQuery;
import nl.teamrocket.core.event.domain.exception.EventNotFoundException;
import nl.teamrocket.core.event.domain.model.Event;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-pad (CQRS-light). Read-only transacties (architectuurdoc §4.1.2).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventQueryService implements EventQueryPort {

    private final EventRepository repository;

    @Override
    public Event findById(GetEventQuery query) {
        return repository.findById(query.eventId())
                .orElseThrow(() -> new EventNotFoundException(query.eventId()));
    }

    @Override
    public List<Event> list(ListEventsQuery query) {
        if (query == null || (query.status() == null && query.organizerId() == null)) {
            return repository.findAll();
        }
        return repository.findByFilter(query.status(), query.organizerId());
    }
}
