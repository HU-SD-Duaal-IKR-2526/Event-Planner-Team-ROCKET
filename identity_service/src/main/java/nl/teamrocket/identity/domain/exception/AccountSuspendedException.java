package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class AccountSuspendedException extends RuntimeException {
    public AccountSuspendedException(UUID id) { super("Account is suspended: " + id); }
}
