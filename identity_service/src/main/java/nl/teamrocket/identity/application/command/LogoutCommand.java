package nl.teamrocket.identity.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LogoutCommand(
        @NotNull UUID accountId,
        @NotBlank String accessTokenJti,  // jti claim from current JWT — to blacklist it
        @NotBlank String refreshToken
) {}
