package com.elton.eventplanner.event.presentation;

import com.elton.eventplanner.event.application.EventApplicationService;
import com.elton.eventplanner.event.application.dto.CreateEventCommand;
import com.elton.eventplanner.event.application.dto.UpdateEventCommand;
import com.elton.eventplanner.event.presentation.request.CreateEventRequest;
import com.elton.eventplanner.event.presentation.request.UpdateEventRequest;
import com.elton.eventplanner.event.presentation.response.EventResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventApplicationService eventService;

    public EventController(EventApplicationService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> findAll() {
        List<EventResponse> events =
                eventService.findAll().stream().map(EventResponse::from).toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(EventResponse.from(eventService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        CreateEventCommand cmd =
                new CreateEventCommand(
                        request.name(),
                        request.date(),
                        request.location(),
                        request.description(),
                        request.userId());
        EventResponse response = EventResponse.from(eventService.create(cmd));
        URI uri =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response.id())
                        .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateEventRequest request) {
        UpdateEventCommand cmd =
                new UpdateEventCommand(
                        id,
                        request.name(),
                        request.date(),
                        request.location(),
                        request.description(),
                        request.status(),
                        request.userId());
        return ResponseEntity.ok(EventResponse.from(eventService.update(cmd)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<EventResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(EventResponse.from(eventService.cancel(id)));
    }

    @PutMapping("/autostatusupdate/{id}")
    public ResponseEntity<EventResponse> autoStatusUpdate(@PathVariable Long id) {
        return ResponseEntity.ok(EventResponse.from(eventService.autoStatusUpdate(id)));
    }
}
