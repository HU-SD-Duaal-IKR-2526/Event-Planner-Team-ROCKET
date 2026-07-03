package nl.teamrocket.core.userprofile.domain.exception;

public class InvalidDisplayNameException extends RuntimeException { public InvalidDisplayNameException(String name, int min, int max) { super("DisplayName must be between " + min + " and " + max + " chars. Got length: " + name.length()); } }
