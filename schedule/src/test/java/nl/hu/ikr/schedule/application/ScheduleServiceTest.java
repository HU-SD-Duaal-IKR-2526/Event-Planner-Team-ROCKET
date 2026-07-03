package nl.hu.ikr.schedule.application;

import nl.hu.ikr.schedule.application.dto.AddSessionCommand;
import nl.hu.ikr.schedule.domain.Schedule;
import nl.hu.ikr.schedule.domain.Session;
import nl.hu.ikr.schedule.domain.TimeSlot;
import nl.hu.ikr.schedule.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock private ScheduleRepository scheduleRepo;
    @InjectMocks private ScheduleService scheduleService;

    private final UUID eventId   = UUID.randomUUID();
    private final UUID roomId    = UUID.randomUUID();
    private final UUID speakerId = UUID.randomUUID();

    private Schedule schedule;

    @BeforeEach
    void setup() {
        schedule = Schedule.create(eventId, "TestEvent",
                Instant.parse("2025-06-01T08:00:00Z"),
                Instant.parse("2025-06-01T18:00:00Z"));
    }

    @Test
    void addSession_geldigeSlot_voegeToe() {
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));
        when(scheduleRepo.save(schedule)).thenReturn(schedule);

        TimeSlot slot = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"));
        AddSessionCommand cmd = new AddSessionCommand(eventId, "Keynote", roomId, speakerId, slot);

        Session result = scheduleService.addSession(cmd);

        assertThat(result.getTitle()).isEqualTo("Keynote");
        verify(scheduleRepo).save(schedule);
    }

    @Test
    void addSession_scheduleNietGevonden_gooit() {
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.empty());

        TimeSlot slot = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"));
        AddSessionCommand cmd = new AddSessionCommand(eventId, "Keynote", roomId, speakerId, slot);

        assertThatThrownBy(() -> scheduleService.addSession(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule niet gevonden");
    }

    @Test
    void addSession_slotVoorEventStart_gooit() {
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        // Slot begint VOOR event start
        TimeSlot slot = new TimeSlot(
                Instant.parse("2025-06-01T06:00:00Z"),
                Instant.parse("2025-06-01T07:00:00Z"));
        AddSessionCommand cmd = new AddSessionCommand(eventId, "Vroeg", roomId, speakerId, slot);

        assertThatThrownBy(() -> scheduleService.addSession(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("buiten het event-bereik");
    }

    @Test
    void addSession_slotNaEventEnd_gooit() {
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));

        // Slot eindigt NA event einde
        TimeSlot slot = new TimeSlot(
                Instant.parse("2025-06-01T17:00:00Z"),
                Instant.parse("2025-06-01T19:00:00Z"));
        AddSessionCommand cmd = new AddSessionCommand(eventId, "Laat", roomId, speakerId, slot);

        assertThatThrownBy(() -> scheduleService.addSession(cmd))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addSession_roomOverlap_gooit() {
        when(scheduleRepo.findByEventId(eventId)).thenReturn(Optional.of(schedule));
        when(scheduleRepo.save(schedule)).thenReturn(schedule);

        TimeSlot slot1 = new TimeSlot(
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:30:00Z"));
        TimeSlot slot2 = new TimeSlot(
                Instant.parse("2025-06-01T10:00:00Z"),
                Instant.parse("2025-06-01T11:00:00Z"));

        scheduleService.addSession(new AddSessionCommand(eventId, "A", roomId, speakerId, slot1));

        assertThatThrownBy(() -> scheduleService.addSession(
                new AddSessionCommand(eventId, "B", roomId, UUID.randomUUID(), slot2)))
                .isInstanceOf(Schedule.RoomOverlapException.class);
    }
}

