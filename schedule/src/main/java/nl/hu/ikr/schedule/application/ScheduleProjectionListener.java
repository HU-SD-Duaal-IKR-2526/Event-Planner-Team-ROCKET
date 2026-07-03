package nl.hu.ikr.schedule.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hu.ikr.schedule.application.dto.EventLifecycleMessage;
import nl.hu.ikr.schedule.application.dto.RegistrationEventMessage;
import nl.hu.ikr.schedule.domain.ProcessedMessage;
import nl.hu.ikr.schedule.domain.Schedule;
import nl.hu.ikr.schedule.domain.UserUpcomingEvent;
import nl.hu.ikr.schedule.repository.ProcessedMessageRepository;
import nl.hu.ikr.schedule.repository.ScheduleRepository;
import nl.hu.ikr.schedule.repository.UserUpcomingEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Luistert naar registration.# en event.# events en projecteert ze op het Schedule read-model.
 * Beide methoden zijn idempotent via processed_messages tabel.
 */
@Component
public class ScheduleProjectionListener {

    private static final Logger log = LoggerFactory.getLogger(ScheduleProjectionListener.class);

    private final ScheduleRepository scheduleRepo;
    private final UserUpcomingEventRepository upcomingRepo;
    private final ProcessedMessageRepository processedRepo;
    private final ObjectMapper objectMapper;

    public ScheduleProjectionListener(ScheduleRepository scheduleRepo,
                                      UserUpcomingEventRepository upcomingRepo,
                                      ProcessedMessageRepository processedRepo,
                                      ObjectMapper objectMapper) {
        this.scheduleRepo = scheduleRepo;
        this.upcomingRepo = upcomingRepo;
        this.processedRepo = processedRepo;
        this.objectMapper = objectMapper;
    }

    // ── Registration events ────────────────────────────────────────────────────

    @RabbitListener(queues = "q.schedule.registrations")
    @Transactional
    public void onRegistrationEvent(String message,
                                    @Header(value = "amqp_receivedRoutingKey", required = false)
                                    String routingKey) {
        try {
            RegistrationEventMessage msg = objectMapper.readValue(message, RegistrationEventMessage.class);
            String msgId = msg.resolveMessageId(routingKey != null ? routingKey : "unknown");

            // Idempotentiecheck
            if (processedRepo.existsByMessageId(msgId)) {
                log.debug("Bericht al verwerkt, overgeslagen: {}", msgId);
                return;
            }

            UUID eventId = UUID.fromString(msg.getEventId());
            String status = msg.getStatus();

            // Delta berekening:
            // RESERVED = eerste keer tellen (stoel bezet)
            // CONFIRMED = stoel was al geteld bij RESERVED, geen extra delta
            // CANCELLED = stoel vrijgeven
            int delta = switch (status != null ? status : "") {
                case "RESERVED"  -> msg.getGuestCount();
                case "CANCELLED" -> -msg.getGuestCount();
                default          -> 0; // CONFIRMED, WAITLISTED, NO_SHOW → geen delta
            };

            if (delta != 0) {
                scheduleRepo.findByEventId(eventId).ifPresent(schedule -> {
                    schedule.applyHeadcountDelta(delta);
                    scheduleRepo.save(schedule);
                });
            }

            // Update user_upcoming_events voor RESERVED
            if ("RESERVED".equals(status) && msg.getUserId() != null) {
                UUID userId = UUID.fromString(msg.getUserId());
                scheduleRepo.findByEventId(eventId).ifPresent(schedule -> {
                    UserUpcomingEvent upcoming = new UserUpcomingEvent(
                            userId, eventId,
                            schedule.getEventTitle(),
                            schedule.getEventStartsAt());
                    upcomingRepo.save(upcoming);
                });
            }

            // Verwijder uit upcoming bij CANCELLED
            if ("CANCELLED".equals(status) && msg.getUserId() != null) {
                UUID userId = UUID.fromString(msg.getUserId());
                UserUpcomingEvent.UserUpcomingEventId id =
                        new UserUpcomingEvent.UserUpcomingEventId(userId, eventId);
                upcomingRepo.deleteById(id);
            }

            processedRepo.save(new ProcessedMessage(msgId));
            log.debug("Registration event verwerkt: {} voor event {}", status, eventId);

        } catch (Exception e) {
            log.error("Fout bij verwerken van registration event: {}", e.getMessage(), e);
            // Niet opnieuw gooien — anders retry-loop
        }
    }

    // ── Event lifecycle events ─────────────────────────────────────────────────

    @RabbitListener(queues = "q.schedule.events")
    @Transactional
    public void onEventLifecycle(String message) {
        try {
            EventLifecycleMessage msg = objectMapper.readValue(message, EventLifecycleMessage.class);
            String msgId = msg.resolveMessageId();

            // Idempotentiecheck
            if (processedRepo.existsByMessageId(msgId)) {
                log.debug("Event lifecycle al verwerkt, overgeslagen: {}", msgId);
                return;
            }

            if (msg.getEventType() == null || msg.getEventId() == null) {
                log.warn("Event lifecycle bericht zonder eventType of eventId, overgeslagen");
                return;
            }

            UUID eventId = UUID.fromString(msg.getEventId());

            switch (msg.getEventType()) {
                case "event.published" -> {
                    Instant startsAt = msg.getStartsAt() != null ? Instant.parse(msg.getStartsAt()) : null;
                    Instant endsAt   = msg.getEndsAt()   != null ? Instant.parse(msg.getEndsAt())   : null;
                    Schedule schedule = Schedule.create(eventId, msg.getTitle(), startsAt, endsAt);
                    scheduleRepo.save(schedule);
                    log.info("Schedule aangemaakt voor event {}", eventId);
                }
                case "event.updated" -> {
                    scheduleRepo.findByEventId(eventId).ifPresent(schedule -> {
                        if (msg.getTitle() != null) {
                            schedule.setEventTitle(msg.getTitle());
                        }
                        scheduleRepo.save(schedule);
                    });
                    log.info("Schedule bijgewerkt voor event {}", eventId);
                }
                case "event.cancelled" -> {
                    scheduleRepo.findByEventId(eventId).ifPresent(schedule -> {
                        schedule.markCancelled();
                        scheduleRepo.save(schedule);
                    });
                    log.info("Schedule gecanceld voor event {}", eventId);
                }
                default -> log.warn("Onbekend event lifecycle type: {}", msg.getEventType());
            }

            processedRepo.save(new ProcessedMessage(msgId));

        } catch (Exception e) {
            log.error("Fout bij verwerken van event lifecycle bericht: {}", e.getMessage(), e);
            // Niet opnieuw gooien — anders retry-loop
        }
    }
}

