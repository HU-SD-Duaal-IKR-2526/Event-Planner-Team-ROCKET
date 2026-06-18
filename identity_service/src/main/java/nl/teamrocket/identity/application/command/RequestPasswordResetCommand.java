package nl.teamrocket.identity.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetCommand(@Email @NotBlank String email) {}
