package nl.hu.ikr.registration.rest.dto;

import nl.hu.ikr.registration.application.dto.RegistrationResult;
import nl.hu.ikr.registration.domain.RegistrationStatus;

import java.time.Instant;
import java.util.UUID;

public class RegistrationResponse {

    private UUID registrationId;
    private RegistrationStatus status;
    private Integer position;
    private Instant createdAt;

    public static RegistrationResponse from(RegistrationResult result) {
        RegistrationResponse r = new RegistrationResponse();
        r.registrationId = result.registrationId();
        r.status = result.status();
        r.position = result.position();
        r.createdAt = result.createdAt();
        return r;
    }

    public UUID getRegistrationId() { return registrationId; }
    public RegistrationStatus getStatus() { return status; }
    public Integer getPosition()    { return position; }
    public Instant getCreatedAt()   { return createdAt; }
}

