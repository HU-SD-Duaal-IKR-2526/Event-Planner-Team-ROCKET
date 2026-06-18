package nl.teamrocket.identity.adapter.rest.dto;

import jakarta.validation.constraints.*;
import nl.teamrocket.identity.domain.model.Role;

import java.util.Set;
import java.util.UUID;

public final class IdentityDtos {
    private IdentityDtos() {}

    // ── Requests ──────────────────────────────────────────────────

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 128) String password
    ) {}

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {}

    public record VerifyEmailRequest(@NotBlank String token) {}

    public record RequestPasswordResetRequest(@Email @NotBlank String email) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 128) String newPassword
    ) {}

    public record ResendVerificationRequest(@Email @NotBlank String email) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record LogoutRequest(
            @NotBlank String refreshToken,
            String accessTokenJti   // optional: jti claim from current JWT
    ) {}

    public record AssignRoleRequest(@NotNull Role role) {}

    // ── Responses ─────────────────────────────────────────────────

    public record RegisterResponse(UUID accountId, String message) {}

    /**
     * Login/refresh response — contains both access token (short-lived JWT)
     * and refresh token (opaque, long-lived, store in HttpOnly cookie).
     */
    public record LoginResponse(
            UUID accountId,
            String email,
            Set<String> roles,
            String accessToken,
            long expiresInSeconds,
            String refreshToken,
            String tokenType
    ) {}

    public record AccountResponse(
            UUID accountId,
            String email,
            String status,
            Set<String> roles
    ) {}

    public record MessageResponse(String message) {}
}
