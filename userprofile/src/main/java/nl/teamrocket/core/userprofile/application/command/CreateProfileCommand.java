package nl.teamrocket.core.userprofile.application.command;
import java.util.UUID;
/** Triggered internally when Identity publishes AccountRegistered. Creates a stub profile. */
public record CreateProfileCommand(UUID accountId, String email) {}
