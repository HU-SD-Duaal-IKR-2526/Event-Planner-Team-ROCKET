package com.elton.eventplanner.event.application;

import com.elton.eventplanner.entities.enums.UserRole;
import com.elton.eventplanner.event.application.dto.CreateEventCommand;
import com.elton.eventplanner.event.application.dto.EventResult;
import com.elton.eventplanner.event.application.dto.UpdateEventCommand;
import com.elton.eventplanner.event.domain.events.EventCreatedDomainEvent;
import com.elton.eventplanner.event.domain.exception.EventNotFoundException;
import com.elton.eventplanner.event.domain.model.Event;
import com.elton.eventplanner.event.domain.model.EventStatus;
import com.elton.eventplanner.event.domain.repository.EventRepository;
import com.elton.eventplanner.event.domain.valueobject.EventDate;
import com.elton.eventplanner.event.domain.valueobject.EventDescription;
import com.elton.eventplanner.event.domain.valueobject.EventId;
import com.elton.eventplanner.event.domain.valueobject.EventName;
import com.elton.eventplanner.repositories.UserRepository;
import com.elton.eventplanner.services.exceptions.EntityNotFoundException;
import com.elton.eventplanner.services.exceptions.InvalidEnumValueException;
import com.elton.eventplanner.services.exceptions.RoleNotAllowedException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EventApplicationService(EventRepository eventRepository, UserRepository userRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<EventResult> findAll() {
        return eventRepository.findByStatus(EventStatus.PLANNED).stream()
                .map(this::toResult)
                .toList();
    }

    public EventResult findById(Long id) {
        return eventRepository.findById(new EventId(id))
                .map(this::toResult)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    public EventResult create(CreateEventCommand cmd) {
        validateUserCanManageEvent(cmd.userId());
        Event event = Event.create(
                new EventName(cmd.name()),
                new EventDate(cmd.date()),
                cmd.location(),
                new EventDescription(cmd.description()),
                cmd.userId()
        );
        Event saved = eventRepository.save(event);
        eventPublisher.publishEvent(new EventCreatedDomainEvent(saved.getId().getValue(), saved.getName().getValue()));
        return toResult(saved);
    }

    public EventResult update(UpdateEventCommand cmd) {
        Event event = eventRepository.findById(new EventId(cmd.id()))
                .orElseThrow(() -> new EventNotFoundException(cmd.id()));
        validateUserCanManageEvent(cmd.userId());
        event.update(
                new EventName(cmd.name()),
                new EventDate(cmd.date()),
                cmd.location(),
                new EventDescription(cmd.description()),
                parseStatus(cmd.status()),
                cmd.userId()
        );
        return toResult(eventRepository.save(event));
    }

    public void delete(Long id) {
        EventId eventId = new EventId(id);
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(id);
        }
        eventRepository.deleteById(eventId);
    }

    public EventResult cancel(Long id) {
        Event event = eventRepository.findById(new EventId(id))
                .orElseThrow(() -> new EventNotFoundException(id));
        event.cancel();
        Event saved = eventRepository.save(event);
        publishDomainEvents(saved);
        return toResult(saved);
    }

    public EventResult autoStatusUpdate(Long id) {
        Event event = eventRepository.findById(new EventId(id))
                .orElseThrow(() -> new EventNotFoundException(id));
        event.updateStatus(LocalDate.now());
        Event saved = eventRepository.save(event);
        publishDomainEvents(saved);
        return toResult(saved);
    }

    private void publishDomainEvents(Event event) {
        event.pullDomainEvents().forEach(eventPublisher::publishEvent);
    }

    private void validateUserCanManageEvent(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(userId));
        if (user.getRole().equals(UserRole.USER)) {
            throw new RoleNotAllowedException(user.getRole(), "manage an event");
        }
    }

    private EventStatus parseStatus(String status) {
        try {
            return EventStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidEnumValueException("Please use only \"PLANNED\", \"CANCELLED\" or \"COMPLETED\"");
        }
    }

    private EventResult toResult(Event event) {
        return new EventResult(
                event.getId() != null ? event.getId().getValue() : null,
                event.getName().getValue(),
                event.getDate().getValue(),
                event.getLocation(),
                event.getDescription().getValue(),
                event.getStatus().name(),
                event.getUserId()
        );
    }
}
