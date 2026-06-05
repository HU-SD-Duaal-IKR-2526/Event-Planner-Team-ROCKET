package nl.teamrocket.core.event.adapter.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.teamrocket.core.event.adapter.rest.dto.EventDtos.CancelEventRequest;
import nl.teamrocket.core.event.adapter.rest.dto.EventDtos.CreateEventRequest;
import nl.teamrocket.core.event.adapter.rest.dto.EventDtos.EventResponse;
import nl.teamrocket.core.event.adapter.rest.dto.EventDtos.UpdateEventRequest;
import nl.teamrocket.core.event.application.command.CancelEventCommand;
import nl.teamrocket.core.event.application.command.CreateEventCommand;
import nl.teamrocket.core.event.application.command.PublishEventCommand;
import nl.teamrocket.core.event.application.command.UpdateEventCommand;
import nl.teamrocket.core.event.application.port.inbound.EventCommandPort;
import nl.teamrocket.core.event.application.port.inbound.EventQueryPort;
import nl.teamrocket.core.event.application.query.GetEventQuery;
import nl.teamrocket.core.event.application.query.ListEventsQuery;
import nl.teamrocket.core.event.domain.model.Event;
import nl.teamrocket.core.event.domain.model.EventStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Primary adapter: REST endpoints voor de Event-module.
 *
 * Roept enkel via de inbound ports {@link EventCommandPort} / {@link EventQueryPort}
 * (architectuurdoc §4.2). Kent het domeinmodel; vertaalt naar REST DTOs.
 *
 * Endpoints (gealigneerd met communicatiedoc §3.2 en architectuurdoc §5.1):
 *   POST   /events                  → create (DRAFT)
 *   GET    /events                  → list  (optionele query-params status, organizerId)
 *   GET    /events/{id}             → detail
 *   PUT    /events/{id}             → update (titel/beschrijving/tijd/metadata)
 *   POST   /events/{id}/publish     → DRAFT → PLANNED
 *   POST   /events/{id}/cancel      → → CANCELLED
 *   POST   /events/{id}/refresh-status → auto-status (scheduler-hook)
 *   DELETE /events/{id}             → verwijder
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventRestController {

    private final EventCommandPort commandPort;
    private final EventQueryPort queryPort;

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest req) {
        Event created = commandPort.create(new CreateEventCommand(
                req.organizerId(), req.venueId(), req.title(), req.description(),
                req.startsAt(), req.endsAt(), req.capacity(),
                req.dressCode(), req.speaker(), req.livestreamUrl(),
                req.visibility()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(EventResponse.from(created));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> list(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) UUID organizerId) {
        List<EventResponse> events = queryPort.list(new ListEventsQuery(status, organizerId))
                .stream().map(EventResponse::from).toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(EventResponse.from(
                queryPort.findById(new GetEventQuery(id))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody UpdateEventRequest req) {
        Event updated = commandPort.update(new UpdateEventCommand(
                id, req.title(), req.description(), req.startsAt(), req.endsAt(),
                req.capacity(), req.dressCode(), req.speaker(), req.livestreamUrl(),
                req.visibility()));
        return ResponseEntity.ok(EventResponse.from(updated));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(EventResponse.from(
                commandPort.publish(new PublishEventCommand(id))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EventResponse> cancel(@PathVariable UUID id,
                                                @Valid @RequestBody(required = false) CancelEventRequest req) {
        String reason = req != null ? req.reason() : null;
        return ResponseEntity.ok(EventResponse.from(
                commandPort.cancel(new CancelEventCommand(id, reason))));
    }

    @PostMapping("/{id}/refresh-status")
    public ResponseEntity<EventResponse> refreshStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(EventResponse.from(commandPort.autoStatusUpdate(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        commandPort.delete(id);
        return ResponseEntity.noContent().build();
    }
}
