package nl.teamrocket.identity.domain.exception;



public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(String email) { super("Account with e-mail already exists: " + email); }
}
