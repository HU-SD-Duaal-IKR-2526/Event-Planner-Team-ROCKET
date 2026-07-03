package nl.hu.ikr.registration.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hu.ikr.registration.domain.OutboxMessage;
import nl.hu.ikr.registration.domain.Registration;
import nl.hu.ikr.registration.domain.RegistrationStatus;
import nl.hu.ikr.registration.repository.OutboxRepository;
import nl.hu.ikr.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Bevestigt een RESERVED registratie na betaling.
 */
@Service
public class ConfirmRegistrationHandler {

    private final RegistrationRepository registrationRepo;
    private final OutboxRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public ConfirmRegistrationHandler(RegistrationRepository registrationRepo,
                                      OutboxRepository outboxRepo,
                                      ObjectMapper objectMapper) {
        this.registrationRepo = registrationRepo;
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handle(UUID registrationId) {
        Registration registration = registrationRepo.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Registratie niet gevonden: " + registrationId));

        if (registration.getStatus() != RegistrationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Bevestigen niet mogelijk vanuit status: " + registration.getStatus());
        }

        registration.confirm();
        registrationRepo.save(registration);

        String payload = buildConfirmedPayload(registration);
        outboxRepo.save(OutboxMessage.pending("registration.confirmed.v1", payload));
    }

    private String buildConfirmedPayload(Registration r) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "registrationId", r.getId(),
                    "eventId", r.getEventId(),
                    "userId", r.getUserId(),
                    "confirmedAt", r.getUpdatedAt().toString(),
                    "guestCount", r.getPlusOnes() + 1,
                    "channel", r.getChannel()
            ));
        } catch (Exception e) {
            throw new RuntimeException("Kon confirmed payload niet serialiseren", e);
        }
    }
}

