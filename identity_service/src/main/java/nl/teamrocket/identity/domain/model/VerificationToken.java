package nl.teamrocket.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: short-lived email verification token.
 * Invariant: valid for less than 24 hours; single use.
 * EXPLICITLY NOT: this is not a session token and not a password-reset token.
 *                 Password reset tokens are a separate VO (ResetToken).
 */
public final class VerificationToken {

    private final String token;
    private final Instant expiresAt;

    public VerificationToken(String token, Instant expiresAt) {
        Objects.requireNonNull(token, "Token value must not be null");
        Objects.requireNonNull(expiresAt, "ExpiresAt must not be null");
        this.token = token;
        this.expiresAt = expiresAt;
    }

    /** Factory: generate a fresh token valid for 24 hours from now. */
    public static VerificationToken generate() {
        return new VerificationToken(
                UUID.randomUUID().toString(),
                Instant.now().plusSeconds(86_400)   // 24 hours
        );
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationToken that)) return false;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "VerificationToken{token='[PROTECTED]', expiresAt=" + expiresAt + "}";
    }
}
