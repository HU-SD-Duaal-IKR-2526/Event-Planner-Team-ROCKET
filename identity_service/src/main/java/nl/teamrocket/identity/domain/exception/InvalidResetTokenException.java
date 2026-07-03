package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException(UUID id) { super("Invalid or missing reset token for account: " + id); }
}
