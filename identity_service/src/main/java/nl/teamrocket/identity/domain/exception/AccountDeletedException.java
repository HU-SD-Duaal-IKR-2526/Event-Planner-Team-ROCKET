package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class AccountDeletedException extends RuntimeException {
    public AccountDeletedException(UUID id) { super("Account has been deleted: " + id); }
}
