package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class NoVerificationTokenException extends RuntimeException {
    public NoVerificationTokenException(UUID id) { super("No verification token found for account: " + id); }
}
