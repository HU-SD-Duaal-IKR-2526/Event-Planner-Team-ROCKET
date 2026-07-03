package nl.teamrocket.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: short-lived password reset token.
 * Valid for 1 hour; single use.
 * EXPLICITLY NOT: this is not a VerificationToken and not a session token.
 */
public final class ResetToken {

    private final String token;
    private final Instant expiresAt;
    private final boolean used;

    public ResetToken(String token, Instant expiresAt, boolean used) {
        Objects.requireNonNull(token, "Token value must not be null");
        Objects.requireNonNull(expiresAt, "ExpiresAt must not be null");
        this.token = token;
        this.expiresAt = expiresAt;
        this.used = used;
    }

    /** Factory: generate a fresh reset token valid for 1 hour. */
    public static ResetToken generate() {
        return new ResetToken(
                UUID.randomUUID().toString(),
                Instant.now().plusSeconds(3_600),   // 1 hour
                false
        );
    }

    /** Returns a new (immutable) copy with used=true. */
    public ResetToken markUsed() {
        return new ResetToken(this.token, this.expiresAt, true);
    }

    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResetToken that)) return false;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "ResetToken{token='[PROTECTED]', expiresAt=" + expiresAt + ", used=" + used + "}";
    }
}
