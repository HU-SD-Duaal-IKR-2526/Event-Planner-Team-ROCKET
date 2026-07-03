package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class ResetTokenExpiredException extends RuntimeException {
    public ResetTokenExpiredException(UUID id) { super("Reset token has expired or already been used for account: " + id); }
}
