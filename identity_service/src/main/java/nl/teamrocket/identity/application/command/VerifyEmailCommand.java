package nl.teamrocket.identity.application.command;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailCommand(@NotBlank String token) {}
