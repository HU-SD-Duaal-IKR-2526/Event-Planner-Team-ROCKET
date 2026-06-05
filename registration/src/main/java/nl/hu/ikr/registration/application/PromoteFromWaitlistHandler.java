package nl.hu.ikr.registration.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hu.ikr.registration.domain.OutboxMessage;
import nl.hu.ikr.registration.domain.Registration;
import nl.hu.ikr.registration.repository.OutboxRepository;
import nl.hu.ikr.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Promoot de oudste wachtende registratie naar RESERVED (FIFO).
 * Wordt getriggerd na annulering van een CONFIRMED of RESERVED registratie.
 */
@Service
public class PromoteFromWaitlistHandler {

    private final RegistrationRepository registrationRepo;
    private final OutboxRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public PromoteFromWaitlistHandler(RegistrationRepository registrationRepo,
                                      OutboxRepository outboxRepo,
                                      ObjectMapper objectMapper) {
        this.registrationRepo = registrationRepo;
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handle(UUID eventId) {
        Optional<Registration> next = registrationRepo.findNextWaitlisted(eventId);
        if (next.isEmpty()) {
            return; // geen wachtenden
        }

        Registration registration = next.get();
        registration.reserve();
        registrationRepo.save(registration);

        String payload = buildReservedPayload(registration);
        outboxRepo.save(OutboxMessage.pending("registration.reserved.v1", payload));
    }

    private String buildReservedPayload(Registration r) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "registrationId", r.getId(),
                    "eventId", r.getEventId(),
                    "userId", r.getUserId(),
                    "status", "RESERVED",
                    "guestCount", r.getPlusOnes() + 1,
                    "reservedAt", r.getUpdatedAt().toString()
            ));
        } catch (Exception e) {
            throw new RuntimeException("Kon reserved payload niet serialiseren", e);
        }
    }
}

