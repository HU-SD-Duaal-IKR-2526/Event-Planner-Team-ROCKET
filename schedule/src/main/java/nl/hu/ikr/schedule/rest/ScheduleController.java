package nl.hu.ikr.schedule.rest;

import nl.hu.ikr.schedule.domain.Schedule;
import nl.hu.ikr.schedule.domain.Session;
import nl.hu.ikr.schedule.domain.UserUpcomingEvent;
import nl.hu.ikr.schedule.repository.ScheduleRepository;
import nl.hu.ikr.schedule.repository.UserUpcomingEventRepository;
import nl.hu.ikr.schedule.rest.dto.EventScheduleResponse;
import nl.hu.ikr.schedule.rest.dto.SessionDto;
import nl.hu.ikr.schedule.rest.dto.UpcomingEventDto;
import nl.hu.ikr.schedule.rest.dto.UserUpcomingResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Open Host Service (OHS) — anderen bevragen Schedule via HTTP GET.
 * Beide endpoints geven Cache-Control: max-age=30 en ETag terug.
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleRepository scheduleRepo;
    private final UserUpcomingEventRepository upcomingRepo;

    public ScheduleController(ScheduleRepository scheduleRepo,
                               UserUpcomingEventRepository upcomingRepo) {
        this.scheduleRepo = scheduleRepo;
        this.upcomingRepo = upcomingRepo;
    }

    /**
     * GET /schedule/events/{eventId}
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventScheduleResponse> getEventSchedule(
            @PathVariable UUID eventId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Optional<Schedule> opt = scheduleRepo.findByEventId(eventId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Schedule schedule = opt.get();
        String etag = "\"" + schedule.getLastUpdated().toEpochMilli() + "\"";

        // ETag-check: client heeft al de nieuwste versie
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        List<SessionDto> timeline = schedule.getSessions().stream()
                .sorted(Comparator.comparing(Session::getSlotStart))
                .map(s -> new SessionDto(
                        s.getId(), s.getTitle(),
                        s.getSlotStart(), s.getSlotEnd(),
                        s.getRoomId(), s.getSpeakerId()))
                .toList();

        EventScheduleResponse body = new EventScheduleResponse(
                schedule.getEventId(),
                schedule.getEventTitle(),
                timeline,
                schedule.getHeadcount(),
                schedule.getLastUpdated());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .eTag(etag)
                .body(body);
    }

    /**
     * GET /schedule/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserUpcomingResponse> getUserSchedule(
            @PathVariable UUID userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<UserUpcomingEvent> events = upcomingRepo.findByIdUserId(userId);

        // ETag gebaseerd op combinatie van event-ids (simpele hash)
        String etag = "\"" + events.stream()
                .map(e -> e.getEventId().toString())
                .sorted()
                .reduce("", String::concat)
                .hashCode() + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        List<UpcomingEventDto> upcoming = events.stream()
                .sorted(Comparator.comparing(e -> e.getStartsAt() != null
                        ? e.getStartsAt()
                        : java.time.Instant.EPOCH))
                .map(e -> new UpcomingEventDto(e.getEventId(), e.getTitle(), e.getStartsAt()))
                .toList();

        UserUpcomingResponse body = new UserUpcomingResponse(userId, upcoming);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .eTag(etag)
                .body(body);
    }
}

