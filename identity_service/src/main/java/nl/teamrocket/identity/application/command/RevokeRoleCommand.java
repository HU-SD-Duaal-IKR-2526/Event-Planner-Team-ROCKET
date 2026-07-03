package nl.teamrocket.identity.application.command;

import nl.teamrocket.identity.domain.model.Role;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RevokeRoleCommand(
        @NotNull UUID accountId,
        @NotNull Role role
) {}
