package nl.hu.ikr.schedule.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hu.ikr.schedule.TestObjectMapper;
import nl.hu.ikr.schedule.application.dto.EventLifecycleMessage;
import nl.hu.ikr.schedule.application.dto.RegistrationEventMessage;
import nl.hu.ikr.schedule.domain.ProcessedMessage;
import nl.hu.ikr.schedule.domain.Schedule;
import nl.hu.ikr.schedule.domain.UserUpcomingEvent;
import nl.hu.ikr.schedule.repository.ProcessedMessageRepository;
import nl.hu.ikr.schedule.repository.ScheduleRepository;
import nl.hu.ikr.schedule.repository.UserUpcomingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleProjectionListenerTest {

    @Mock private ScheduleRepository scheduleRepo;
    @Mock private UserUpcomingEventRepository upcomingRepo;
    @Mock private ProcessedMessageRepository processedRepo;

    @InjectMocks
    private ScheduleProjectionListener listener;

    private final ObjectMapper objectMapper = TestObjectMapper.create();
    private final UUID eventId = UUID.randomUUID();
    private final UUID userId  = UUID.randomUUID();
    private final UUID registrationId = UUID.randomUUID();

    private Schedule schedule;

    @BeforeEach
    void setup() throws Exception {
        // Injecteer echte ObjectMapper via constructor
        listener = new ScheduleProjectionListener(
                scheduleRepo, upcomingRepo, processedRepo, objectMapper);

        schedule = Schedule.create(eventId, "TestEvent",
                Instant.parse("2025-06-01T08:00:00Z"),
                Instant.parse("2025-06-01T18:00:00Z"));
    }

    // ── onRegistrationEvent — headcount ───────────────────────────────────────

    @Test
    void reserved_verhoogtHeadcount() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(false);
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        String msg = objectMapper.writeValueAsString(buildRegistrationMsg("RESERVED", 2));
        listener.onRegistrationEvent(msg, "registration.reserved.v1");

        assertThat(schedule.getHeadcount()).isEqualTo(2);
        verify(scheduleRepo).save(schedule);
    }

    @Test
    void confirmed_verhoogtHeadcountNIET() throws Exception {
        schedule.applyHeadcountDelta(2); // al geteld bij RESERVED
        when(processedRepo.existsByMessageId(any())).thenReturn(false);
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        String msg = objectMapper.writeValueAsString(buildRegistrationMsg("CONFIRMED", 2));
        listener.onRegistrationEvent(msg, "registration.confirmed.v1");

        // headcount blijft 2, geen extra delta
        assertThat(schedule.getHeadcount()).isEqualTo(2);
        verify(scheduleRepo, never()).save(any()); // delta=0, save niet aangeroepen
    }

    @Test
    void cancelled_verlaagHeadcount() throws Exception {
        schedule.applyHeadcountDelta(3);
        when(processedRepo.existsByMessageId(any())).thenReturn(false);
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        String msg = objectMapper.writeValueAsString(buildRegistrationMsg("CANCELLED", 3));
        listener.onRegistrationEvent(msg, "registration.cancelled.v1");

        assertThat(schedule.getHeadcount()).isZero();
        verify(scheduleRepo).save(schedule);
    }

    @Test
    void waitlisted_verandertHeadcountNiet() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(false);

        String msg = objectMapper.writeValueAsString(buildRegistrationMsg("WAITLISTED", 1));
        listener.onRegistrationEvent(msg, "registration.waitlisted.v1");

        verify(scheduleRepo, never()).save(any());
    }

    // ── onRegistrationEvent — idempotentie ────────────────────────────────────

    @Test
    void dubbelBericht_wordtOvergeslagen() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(true);

        String msg = objectMapper.writeValueAsString(buildRegistrationMsg("RESERVED", 1));
        listener.onRegistrationEvent(msg, "registration.reserved.v1");

        verify(scheduleRepo, never()).findByEventId(any());
        verify(scheduleRepo, never()).save(any());
    }

    @Test
    void reserved_slaatProcessedMessageOp() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(false);
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        String msg = objectMapper.writeValueAsString(buildRegistrationMsg("RESERVED", 1));
        listener.onRegistrationEvent(msg, "registration.reserved.v1");

        verify(processedRepo).save(any(ProcessedMessage.class));
    }

    // ── onRegistrationEvent — user_upcoming_events ────────────────────────────

    @Test
    void reserved_voegUserUpcomingToe() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(false);
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        String msg = objectMapper.writeValueAsString(buildRegistrationMsg("RESERVED", 1));
        listener.onRegistrationEvent(msg, "registration.reserved.v1");

        ArgumentCaptor<UserUpcomingEvent> captor = ArgumentCaptor.forClass(UserUpcomingEvent.class);
        verify(upcomingRepo).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getEventId()).isEqualTo(eventId);
    }

    @Test
    void cancelled_verwijdertUserUpcoming() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(false);
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        String msg = objectMapper.writeValueAsString(buildRegistrationMsg("CANCELLED", 1));
        listener.onRegistrationEvent(msg, "registration.cancelled.v1");

        verify(upcomingRepo).deleteById(new UserUpcomingEvent.UserUpcomingEventId(userId, eventId));
    }

    // ── onEventLifecycle ──────────────────────────────────────────────────────

    @Test
    void eventPublished_maaktScheduleAan() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(false);

        String msg = objectMapper.writeValueAsString(buildEventMsg("event.published"));
        listener.onEventLifecycle(msg);

        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepo).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo(eventId);
        assertThat(captor.getValue().getEventTitle()).isEqualTo("TestEvent");
    }

    @Test
    void eventUpdated_updatesTitle() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(false);
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        EventLifecycleMessage upd = buildEventMsg("event.updated");
        upd.setTitle("Nieuwe Naam");
        listener.onEventLifecycle(objectMapper.writeValueAsString(upd));

        assertThat(schedule.getEventTitle()).isEqualTo("Nieuwe Naam");
        verify(scheduleRepo).save(schedule);
    }

    @Test
    void eventCancelled_markeertSchedule() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(false);
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        String msg = objectMapper.writeValueAsString(buildEventMsg("event.cancelled"));
        listener.onEventLifecycle(msg);

        assertThat(schedule.getSessions()).isEmpty();
        verify(scheduleRepo).save(schedule);
    }

    @Test
    void eventLifecycle_dubbelBericht_wordtOvergeslagen() throws Exception {
        when(processedRepo.existsByMessageId(any())).thenReturn(true);

        String msg = objectMapper.writeValueAsString(buildEventMsg("event.published"));
        listener.onEventLifecycle(msg);

        verify(scheduleRepo, never()).save(any());
    }

    @Test
    void ongeldigJson_gooit_geenException() {
        assertThatNoException().isThrownBy(
                () -> listener.onRegistrationEvent("{invalid_json}", "registration.reserved.v1"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private java.util.Map<String, Object> buildRegistrationMsg(String status, int guestCount) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("registrationId", registrationId.toString());
        m.put("eventId", eventId.toString());
        m.put("userId", userId.toString());
        m.put("status", status);
        m.put("guestCount", guestCount);
        return m;
    }

    private EventLifecycleMessage buildEventMsg(String type) {
        EventLifecycleMessage m = new EventLifecycleMessage();
        m.setEventType(type);
        m.setEventId(eventId.toString());
        m.setTitle("TestEvent");
        m.setStartsAt("2025-06-01T08:00:00Z");
        m.setEndsAt("2025-06-01T18:00:00Z");
        return m;
    }
}

