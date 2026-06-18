package nl.teamrocket.identity.domain.exception;

import java.util.UUID;

public class AccountAlreadyVerifiedException extends RuntimeException {
    public AccountAlreadyVerifiedException(UUID id) { super("Account already verified: " + id); }
}
