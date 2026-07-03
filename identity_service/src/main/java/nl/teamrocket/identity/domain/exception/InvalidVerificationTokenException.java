package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class InvalidVerificationTokenException extends RuntimeException {
    public InvalidVerificationTokenException(UUID id) { super("Invalid verification token for account: " + id); }
}
