package com.elton.eventplanner.event.application;

import com.elton.eventplanner.entities.User;
import com.elton.eventplanner.entities.enums.UserRole;
import com.elton.eventplanner.event.application.dto.CreateEventCommand;
import com.elton.eventplanner.event.application.dto.EventResult;
import com.elton.eventplanner.event.application.dto.UpdateEventCommand;
import com.elton.eventplanner.event.domain.events.EventCancelledDomainEvent;
import com.elton.eventplanner.event.domain.events.EventCreatedDomainEvent;
import com.elton.eventplanner.event.domain.exception.EventAlreadyCancelledException;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventApplicationServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private EventApplicationService service;

    private Event plannedEvent;
    private User adminUser;

    @BeforeEach
    void setUp() {
        plannedEvent = new Event(
                new EventId(1L),
                new EventName("Team Rocket Kickoff"),
                new EventDate(LocalDate.now().plusDays(10)),
                "Amsterdam",
                new EventDescription("Een kickoff meeting voor Team Rocket."),
                EventStatus.PLANNED,
                1L
        );
        adminUser = new User(1L, "admin", "pass", UserRole.ADM);
    }

    @Test
    void findAll_returnsPlannedEvents() {
        when(eventRepository.findByStatus(EventStatus.PLANNED)).thenReturn(List.of(plannedEvent));

        List<EventResult> results = service.findAll();

        assertEquals(1, results.size());
        assertEquals("Team Rocket Kickoff", results.get(0).name());
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(eventRepository.findByStatus(EventStatus.PLANNED)).thenReturn(List.of());

        List<EventResult> results = service.findAll();

        assertEquals(0, results.size());
    }

    @Test
    void findById_found_returnsResult() {
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(plannedEvent));

        EventResult result = service.findById(1L);

        assertEquals(1L, result.id());
        assertEquals("Team Rocket Kickoff", result.name());
        assertEquals("Amsterdam", result.location());
    }

    @Test
    void findById_notFound_throwsEventNotFoundException() {
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void create_validAdminUser_returnsResult() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(eventRepository.save(any())).thenReturn(plannedEvent);

        EventResult result = service.create(new CreateEventCommand(
                "Team Rocket Kickoff",
                LocalDate.now().plusDays(10),
                "Amsterdam",
                "Een kickoff meeting voor Team Rocket.",
                1L
        ));

        assertEquals("Team Rocket Kickoff", result.name());
        verify(eventRepository).save(any());
    }

    @Test
    void create_publishesEventCreatedDomainEvent() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(eventRepository.save(any())).thenReturn(plannedEvent);

        service.create(new CreateEventCommand(
                "Team Rocket Kickoff",
                LocalDate.now().plusDays(10),
                "Amsterdam",
                "Een kickoff meeting voor Team Rocket.",
                1L
        ));

        ArgumentCaptor<EventCreatedDomainEvent> captor = ArgumentCaptor.forClass(EventCreatedDomainEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(1L, captor.getValue().eventId());
        assertEquals("Team Rocket Kickoff", captor.getValue().eventName());
    }

    @Test
    void create_userRole_throwsRoleNotAllowedException() {
        User regularUser = new User(2L, "user", "pass", UserRole.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        assertThrows(RoleNotAllowedException.class, () -> service.create(new CreateEventCommand(
                "Team Rocket Kickoff",
                LocalDate.now().plusDays(10),
                "Amsterdam",
                "Een kickoff meeting voor Team Rocket.",
                2L
        )));
    }

    @Test
    void create_userNotFound_throwsEntityNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create(new CreateEventCommand(
                "Team Rocket Kickoff",
                LocalDate.now().plusDays(10),
                "Amsterdam",
                "Een kickoff meeting voor Team Rocket.",
                99L
        )));
    }

    @Test
    void update_valid_returnsResult() {
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(plannedEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(eventRepository.save(any())).thenReturn(plannedEvent);

        EventResult result = service.update(new UpdateEventCommand(
                1L, "Updated Event Name", LocalDate.now().plusDays(20),
                "Rotterdam", "Bijgewerkte beschrijving voor dit event.", "PLANNED", 1L
        ));

        assertNotNull(result);
        verify(eventRepository).save(any());
    }

    @Test
    void update_eventNotFound_throwsEventNotFoundException() {
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> service.update(new UpdateEventCommand(
                99L, "Updated Event Name", LocalDate.now().plusDays(20),
                "Rotterdam", "Bijgewerkte beschrijving voor dit event.", "PLANNED", 1L
        )));
    }

    @Test
    void update_userNotFound_throwsEntityNotFoundException() {
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(plannedEvent));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(new UpdateEventCommand(
                1L, "Updated Event Name", LocalDate.now().plusDays(20),
                "Rotterdam", "Bijgewerkte beschrijving voor dit event.", "PLANNED", 99L
        )));
    }

    @Test
    void update_userRole_throwsRoleNotAllowedException() {
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(plannedEvent));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User(2L, "user", "pass", UserRole.USER)));

        assertThrows(RoleNotAllowedException.class, () -> service.update(new UpdateEventCommand(
                1L, "Updated Event Name", LocalDate.now().plusDays(20),
                "Rotterdam", "Bijgewerkte beschrijving voor dit event.", "PLANNED", 2L
        )));
    }

    @Test
    void update_ownerEditingOwnEvent_succeeds() {
        Event ownedEvent = new Event(
                new EventId(1L), new EventName("Team Rocket Kickoff"),
                new EventDate(LocalDate.now().plusDays(10)), "Amsterdam",
                new EventDescription("Een kickoff meeting voor Team Rocket."),
                EventStatus.PLANNED, 3L
        );
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(ownedEvent));
        when(userRepository.findById(3L)).thenReturn(Optional.of(new User(3L, "owner", "pass", UserRole.OWNER)));
        when(eventRepository.save(any())).thenReturn(ownedEvent);

        assertNotNull(service.update(new UpdateEventCommand(
                1L, "Updated Event Name", LocalDate.now().plusDays(20),
                "Rotterdam", "Bijgewerkte beschrijving voor dit event.", "PLANNED", 3L
        )));
    }

    @Test
    void update_ownerEditingOtherOwnersEvent_throwsRoleNotAllowedException() {
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(plannedEvent));
        when(userRepository.findById(3L)).thenReturn(Optional.of(new User(3L, "owner2", "pass", UserRole.OWNER)));

        assertThrows(RoleNotAllowedException.class, () -> service.update(new UpdateEventCommand(
                1L, "Updated Event Name", LocalDate.now().plusDays(20),
                "Rotterdam", "Bijgewerkte beschrijving voor dit event.", "PLANNED", 3L
        )));
    }

    @Test
    void update_invalidStatus_throwsInvalidEnumValueException() {
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(plannedEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(InvalidEnumValueException.class, () -> service.update(new UpdateEventCommand(
                1L, "Updated Event Name", LocalDate.now().plusDays(20),
                "Rotterdam", "Bijgewerkte beschrijving voor dit event.", "INVALID_STATUS", 1L
        )));
    }

    @Test
    void delete_exists_deletesEvent() {
        when(eventRepository.existsById(new EventId(1L))).thenReturn(true);

        service.delete(1L);

        verify(eventRepository).deleteById(new EventId(1L));
    }

    @Test
    void delete_notExists_throwsEventNotFoundException() {
        when(eventRepository.existsById(any())).thenReturn(false);

        assertThrows(EventNotFoundException.class, () -> service.delete(99L));
    }

    @Test
    void cancel_valid_returnsCancelledResult() {
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(plannedEvent));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EventResult result = service.cancel(1L);

        assertEquals("CANCELLED", result.status());
    }

    @Test
    void cancel_publishesDomainEvent() {
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(plannedEvent));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.cancel(1L);

        verify(eventPublisher).publishEvent(any(EventCancelledDomainEvent.class));
    }

    @Test
    void cancel_notFound_throwsEventNotFoundException() {
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> service.cancel(99L));
    }

    @Test
    void cancel_alreadyCancelled_throwsEventAlreadyCancelledException() {
        Event cancelledEvent = new Event(
                new EventId(1L), new EventName("Team Rocket Kickoff"),
                new EventDate(LocalDate.now().plusDays(10)), "Amsterdam",
                new EventDescription("Een kickoff meeting voor Team Rocket."),
                EventStatus.CANCELLED, 1L
        );
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(cancelledEvent));

        assertThrows(EventAlreadyCancelledException.class, () -> service.cancel(1L));
    }

    @Test
    void autoStatusUpdate_pastDate_setsCompleted() {
        Event pastEvent = new Event(
                new EventId(2L),
                new EventName("Past Event Name"),
                new EventDate(LocalDate.now().minusDays(1)),
                "Utrecht",
                new EventDescription("Dit event is al voorbij geweest."),
                EventStatus.PLANNED,
                1L
        );
        when(eventRepository.findById(new EventId(2L))).thenReturn(Optional.of(pastEvent));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EventResult result = service.autoStatusUpdate(2L);

        assertEquals("COMPLETED", result.status());
    }

    @Test
    void autoStatusUpdate_futureDate_keepsPlanned() {
        when(eventRepository.findById(new EventId(1L))).thenReturn(Optional.of(plannedEvent));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EventResult result = service.autoStatusUpdate(1L);

        assertEquals("PLANNED", result.status());
    }

    @Test
    void autoStatusUpdate_notFound_throwsEventNotFoundException() {
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> service.autoStatusUpdate(99L));
    }
}
