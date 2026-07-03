package nl.teamrocket.core.userprofile.domain.exception;

public class AvatarUploadException extends RuntimeException { public AvatarUploadException(String reason) { super("Avatar upload failed: " + reason); } }
