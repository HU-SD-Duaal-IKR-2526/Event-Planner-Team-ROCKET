package nl.teamrocket.core.event.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.event.application.command.CancelEventCommand;
import nl.teamrocket.core.event.application.command.CreateEventCommand;
import nl.teamrocket.core.event.application.command.PublishEventCommand;
import nl.teamrocket.core.event.application.command.UpdateEventCommand;
import nl.teamrocket.core.event.application.port.inbound.EventCommandPort;
import nl.teamrocket.core.event.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.core.event.application.port.outbound.EventRepository;
import nl.teamrocket.core.event.domain.event.EventDomainEvent;
import nl.teamrocket.core.event.domain.exception.EventNotFoundException;
import nl.teamrocket.core.event.domain.model.Event;
import nl.teamrocket.core.event.domain.model.EventMetadata;
import nl.teamrocket.core.event.domain.model.EventStatus;
import nl.teamrocket.core.event.domain.model.EventTimeSlot;
import nl.teamrocket.core.event.domain.service.EventStatusPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service: orchestreert het Event-aggregate.
 *
 * Architectuurdoc §4.2 (EventCommandService): voert business-flow uit, handelt
 * transacties, publiceert domain events. Hexagonaal — kent enkel de outbound
 * ports {@link EventRepository} en {@link DomainEventPublisher}, geen infrastructuur.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventCommandService implements EventCommandPort {

    private final EventRepository repository;
    private final DomainEventPublisher publisher;

    @Override
    @Transactional
    public Event create(CreateEventCommand cmd) {
        EventTimeSlot slot = new EventTimeSlot(cmd.startsAt(), cmd.endsAt());
        EventMetadata metadata = new EventMetadata(
                cmd.dressCode(), cmd.speaker(), cmd.capacity(), cmd.livestreamUrl());

        Event event = Event.create(
                UUID.randomUUID(),
                cmd.organizerId(),
                cmd.venueId(),
                cmd.title(),
                cmd.description(),
                slot,
                metadata,
                cmd.visibility());

        Event saved = repository.save(event);
        publishPendingEvents(saved);
        log.info("Event created: id={} organizer={} title={}", saved.getId(),
                saved.getOrganizerId(), saved.getTitle());
        return saved;
    }

    @Override
    @Transactional
    public Event update(UpdateEventCommand cmd) {
        Event event = repository.findById(cmd.eventId())
                .orElseThrow(() -> new EventNotFoundException(cmd.eventId()));

        EventTimeSlot newSlot = (cmd.startsAt() != null && cmd.endsAt() != null)
                ? new EventTimeSlot(cmd.startsAt(), cmd.endsAt())
                : null;

        EventMetadata existing = event.getMetadata();
        EventMetadata newMetadata = anyMetadataFieldChanged(cmd, existing)
                ? new EventMetadata(
                    cmd.dressCode() != null ? cmd.dressCode() : existing.dressCode(),
                    cmd.speaker()    != null ? cmd.speaker()    : existing.speaker(),
                    cmd.capacity()   != null ? cmd.capacity()   : existing.capacity(),
                    cmd.livestreamUrl() != null ? cmd.livestreamUrl() : existing.livestreamUrl())
                : null;

        event.update(cmd.title(), cmd.description(), newSlot, newMetadata, cmd.visibility());
        Event saved = repository.save(event);
        publishPendingEvents(saved);
        return saved;
    }

    @Override
    @Transactional
    public Event publish(PublishEventCommand cmd) {
        Event event = loadOrThrow(cmd.eventId());
        event.publish(Instant.now());
        Event saved = repository.save(event);
        publishPendingEvents(saved);
        log.info("Event published: id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Event cancel(CancelEventCommand cmd) {
        Event event = loadOrThrow(cmd.eventId());
        event.cancel(cmd.reason());
        Event saved = repository.save(event);
        publishPendingEvents(saved);
        log.info("Event cancelled: id={} reason={}", saved.getId(), cmd.reason());
        return saved;
    }

    @Override
    @Transactional
    public void delete(UUID eventId) {
        if (!repository.existsById(eventId)) {
            throw new EventNotFoundException(eventId);
        }
        repository.deleteById(eventId);
        log.info("Event deleted: id={}", eventId);
    }

    @Override
    @Transactional
    public Event autoStatusUpdate(UUID eventId) {
        Event event = loadOrThrow(eventId);
        EventStatus next = EventStatusPolicy.nextStatusAt(event, Instant.now());
        if (next == EventStatus.COMPLETED) {
            event.complete();
            Event saved = repository.save(event);
            log.info("Event auto-completed: id={}", saved.getId());
            return saved;
        }
        return event;
    }

    private Event loadOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    private void publishPendingEvents(Event event) {
        List<EventDomainEvent> domainEvents = event.pullDomainEvents();
        for (EventDomainEvent de : domainEvents) {
            publisher.publish(de);
        }
    }

    private static boolean anyMetadataFieldChanged(UpdateEventCommand cmd, EventMetadata current) {
        return cmd.dressCode() != null || cmd.speaker() != null
                || cmd.capacity() != null || cmd.livestreamUrl() != null;
    }
}
