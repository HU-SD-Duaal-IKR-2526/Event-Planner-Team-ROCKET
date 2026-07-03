package nl.teamrocket.identity.application.handler;

import java.util.Set;
import java.util.UUID;

/**
 * Result returned by LoginHandler and RefreshTokenHandler.
 * Contains both the short-lived access token (JWT) and the long-lived refresh token (opaque).
 */
public record LoginResult(
        UUID    accountId,
        String  email,
        Set<String> roles,
        String  accessToken,
        long    expiresInSeconds,
        String  refreshToken
) {}
