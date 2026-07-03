package nl.hu.ikr.schedule.application;

import nl.hu.ikr.schedule.application.dto.AddSessionCommand;
import nl.hu.ikr.schedule.domain.Schedule;
import nl.hu.ikr.schedule.domain.Session;
import nl.hu.ikr.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Write-commando's voor Schedule: alleen addSession voor nu.
 * ACL-check: slot moet binnen event.startsAt..event.endsAt vallen.
 */
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepo;

    public ScheduleService(ScheduleRepository scheduleRepo) {
        this.scheduleRepo = scheduleRepo;
    }

    @Transactional
    public Session addSession(AddSessionCommand cmd) {
        Schedule schedule = scheduleRepo.findByEventId(cmd.eventId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Schedule niet gevonden voor event: " + cmd.eventId()));

        // ACL-check: slot moet binnen event-datumrange vallen
        if (schedule.getEventStartsAt() != null && schedule.getEventEndsAt() != null) {
            if (cmd.slot().getStart().isBefore(schedule.getEventStartsAt()) ||
                cmd.slot().getEnd().isAfter(schedule.getEventEndsAt())) {
                throw new IllegalArgumentException(
                        "Tijdslot " + cmd.slot() + " valt buiten het event-bereik ["
                        + schedule.getEventStartsAt() + " – " + schedule.getEventEndsAt() + "]");
            }
        }

        Session session = schedule.addSession(
                cmd.title(), cmd.roomId(), cmd.speakerId(), cmd.slot());

        scheduleRepo.save(schedule);
        return session;
    }
}

