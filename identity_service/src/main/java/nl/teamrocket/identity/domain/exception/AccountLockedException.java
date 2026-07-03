package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException(UUID id) { super("Account is locked: " + id); }
}
