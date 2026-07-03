package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class VerificationTokenExpiredException extends RuntimeException {
    public VerificationTokenExpiredException(UUID id) { super("Verification token expired for account: " + id); }
}
