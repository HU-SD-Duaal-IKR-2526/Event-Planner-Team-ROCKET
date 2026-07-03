package nl.hu.ikr.registration.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hu.ikr.registration.application.dto.RegistrationResult;
import nl.hu.ikr.registration.application.dto.ReserveRegistrationCommand;
import nl.hu.ikr.registration.domain.EventCapacity;
import nl.hu.ikr.registration.domain.OutboxMessage;
import nl.hu.ikr.registration.domain.Registration;
import nl.hu.ikr.registration.domain.RegistrationStatus;
import nl.hu.ikr.registration.repository.EventCapacityRepository;
import nl.hu.ikr.registration.repository.OutboxRepository;
import nl.hu.ikr.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Kern van de SAGA: capaciteitscheck, idempotentie, outbox — alles in één transactie.
 */
@Service
public class ReserveRegistrationHandler {

    private final RegistrationRepository registrationRepo;
    private final EventCapacityRepository eventCapacityRepo;
    private final OutboxRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public ReserveRegistrationHandler(RegistrationRepository registrationRepo,
                                      EventCapacityRepository eventCapacityRepo,
                                      OutboxRepository outboxRepo,
                                      ObjectMapper objectMapper) {
        this.registrationRepo = registrationRepo;
        this.eventCapacityRepo = eventCapacityRepo;
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RegistrationResult handle(ReserveRegistrationCommand cmd) {
        // 1. Idempotentiecheck
        Optional<Registration> existing = registrationRepo.findByIdempotencyKey(cmd.idempotencyKey());
        if (existing.isPresent()) {
            Registration r = existing.get();
            return toResult(r, null, false);
        }

        // 2. Lock capaciteitsrij (SELECT FOR UPDATE)
        EventCapacity cap = eventCapacityRepo.lockByEventId(cmd.eventId())
                .orElseThrow(() -> new EventNotFoundException(
                        "Event niet gevonden of niet ingeschreven voor capaciteit: " + cmd.eventId()));

        if (!cap.isOpen()) {
            throw new EventNotOpenException("Event is gesloten voor inschrijvingen: " + cmd.eventId());
        }

        // 3. Tel bezette plaatsen
        long takenSeats = registrationRepo.countByEventIdAndStatusIn(
                cmd.eventId(), List.of(RegistrationStatus.RESERVED, RegistrationStatus.CONFIRMED));

        // 4. Bepaal status
        Registration registration = Registration.create(
                cmd.eventId(), cmd.userId(), cmd.plusOnes(),
                cmd.notes(), cmd.channel(), cmd.idempotencyKey());

        boolean hasRoom = cap.hasRoom((int) takenSeats, cmd.plusOnes());
        if (hasRoom) {
            registration.reserve();
        } else {
            registration.waitlist();
        }

        // 5. Sla op
        registrationRepo.save(registration);

        // 6. Schrijf outbox bericht (in dezelfde transactie!)
        String routingKey = (registration.getStatus() == RegistrationStatus.RESERVED)
                ? "registration.reserved.v1"
                : "registration.waitlisted.v1";

        Integer position = null;
        if (registration.getStatus() == RegistrationStatus.WAITLISTED) {
            position = (int) (registrationRepo.countByEventIdAndStatusIn(
                    cmd.eventId(), List.of(RegistrationStatus.WAITLISTED)));
        }

        String payload = buildPayload(registration, routingKey, position);
        outboxRepo.save(OutboxMessage.pending(routingKey, payload));

        // 7. Return result
        return toResult(registration, position, true);
    }

    /**
     * Compenserende actie: annuleer een registratie en publiceer cancelled-event.
     */
    @Transactional
    public void compensate(UUID registrationId) {
        Registration registration = registrationRepo.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(
                        "Registratie niet gevonden: " + registrationId));

        registration.cancel();
        registrationRepo.save(registration);

        String payload = buildCancelledPayload(registration);
        outboxRepo.save(OutboxMessage.pending("registration.cancelled.v1", payload));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private RegistrationResult toResult(Registration r, Integer position, boolean isNew) {
        return new RegistrationResult(r.getId(), r.getStatus(), position, r.getCreatedAt(), isNew);
    }

    private String buildPayload(Registration r, String routingKey, Integer position) {
        try {
            if ("registration.reserved.v1".equals(routingKey)) {
                return objectMapper.writeValueAsString(Map.of(
                        "registrationId", r.getId(),
                        "eventId", r.getEventId(),
                        "userId", r.getUserId(),
                        "status", r.getStatus(),
                        "guestCount", r.getPlusOnes() + 1,
                        "reservedAt", r.getUpdatedAt().toString()
                ));
            } else {
                return objectMapper.writeValueAsString(Map.of(
                        "registrationId", r.getId(),
                        "eventId", r.getEventId(),
                        "userId", r.getUserId(),
                        "status", r.getStatus(),
                        "guestCount", r.getPlusOnes() + 1,
                        "position", position != null ? position : 0,
                        "waitlistedAt", r.getUpdatedAt().toString()
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Kon outbox payload niet serialiseren", e);
        }
    }

    private String buildCancelledPayload(Registration r) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "registrationId", r.getId(),
                    "eventId", r.getEventId(),
                    "userId", r.getUserId(),
                    "status", "CANCELLED",
                    "guestCount", r.getPlusOnes() + 1,
                    "cancelledAt", r.getUpdatedAt().toString()
            ));
        } catch (Exception e) {
            throw new RuntimeException("Kon cancelled payload niet serialiseren", e);
        }
    }

    // ── Excepties ──────────────────────────────────────────────────────────────

    public static class EventNotFoundException extends RuntimeException {
        public EventNotFoundException(String msg) { super(msg); }
    }

    public static class EventNotOpenException extends RuntimeException {
        public EventNotOpenException(String msg) { super(msg); }
    }

    public static class RegistrationNotFoundException extends RuntimeException {
        public RegistrationNotFoundException(String msg) { super(msg); }
    }
}

