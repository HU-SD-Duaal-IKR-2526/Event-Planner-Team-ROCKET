package nl.teamrocket.identity.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordCommand(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 128) String newPassword
) {}
