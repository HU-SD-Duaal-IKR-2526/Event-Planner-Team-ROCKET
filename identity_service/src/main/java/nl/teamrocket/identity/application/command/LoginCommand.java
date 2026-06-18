package nl.teamrocket.identity.application.command;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank String email,
        @NotBlank String rawPassword
) {}
