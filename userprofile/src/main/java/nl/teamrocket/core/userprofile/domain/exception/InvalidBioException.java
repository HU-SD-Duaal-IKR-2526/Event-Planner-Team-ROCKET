package nl.teamrocket.core.userprofile.domain.exception;

public class InvalidBioException extends RuntimeException { public InvalidBioException(int length, int max) { super("Bio exceeds max " + max + " chars. Got: " + length); } }
