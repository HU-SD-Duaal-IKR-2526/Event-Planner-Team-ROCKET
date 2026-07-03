package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class AccountNotVerifiedException extends RuntimeException {
    public AccountNotVerifiedException(UUID id) { super("Account not yet verified: " + id); }
}
