package nl.teamrocket.core.userprofile.application.command;
import java.util.UUID;
public record RemoveAvatarCommand(UUID requestingAccountId, UUID targetAccountId) {}
