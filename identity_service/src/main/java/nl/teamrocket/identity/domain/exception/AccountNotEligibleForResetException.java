package nl.teamrocket.identity.domain.exception;

import nl.teamrocket.identity.domain.model.AccountStatus;
import java.util.UUID;

public class AccountNotEligibleForResetException extends RuntimeException {
    public AccountNotEligibleForResetException(UUID id, AccountStatus status) { super("Account " + id + " with status " + status + " is not eligible for password reset"); }
}
