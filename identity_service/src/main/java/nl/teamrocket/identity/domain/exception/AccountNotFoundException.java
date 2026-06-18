package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID id) { super("Account not found: " + id); }
    public AccountNotFoundException(String email) { super("Account not found for e-mail: " + email); }
}
