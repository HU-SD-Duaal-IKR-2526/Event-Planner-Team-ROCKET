package nl.teamrocket.identity.domain.exception;



public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Invalid e-mail or password"); }
}
