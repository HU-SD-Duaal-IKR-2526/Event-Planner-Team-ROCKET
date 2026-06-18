package nl.teamrocket.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: long-lived opaque refresh token.
 *
 * The token value is a random UUID stored hashed in the database.
 * Validity is checked by DB lookup — if found and not expired/revoked, it is valid.
 *
 * Invariants:
 *  - Valid for 7 days by default
 *  - Bound to exactly one Account
 *  - Revoked on logout or password change
 *  - Rotated on every use (old token invalidated, new one issued)
 *
 * EXPLICITLY NOT: this is not an access token (JWT). Access tokens are short-lived
 *                 and self-contained; refresh tokens are long-lived and opaque.
 */
public final class RefreshToken {

    private final String tokenHash;   // stored as hash, never plaintext
    private final UUID   accountId;
    private final Instant expiresAt;
    private final boolean revoked;
    private final Instant createdAt;

    public RefreshToken(String tokenHash, UUID accountId,
                        Instant expiresAt, boolean revoked, Instant createdAt) {
        Objects.requireNonNull(tokenHash,  "tokenHash must not be null");
        Objects.requireNonNull(accountId,  "accountId must not be null");
        Objects.requireNonNull(expiresAt,  "expiresAt must not be null");
        Objects.requireNonNull(createdAt,  "createdAt must not be null");
        this.tokenHash = tokenHash;
        this.accountId = accountId;
        this.expiresAt = expiresAt;
        this.revoked   = revoked;
        this.createdAt = createdAt;
    }

    /** Factory — hash the raw token before constructing. */
    public static RefreshToken create(String rawTokenHash, UUID accountId) {
        Instant now = Instant.now();
        return new RefreshToken(
                rawTokenHash,
                accountId,
                now.plusSeconds(604_800L), // 7 days
                false,
                now
        );
    }

    /** Return a revoked copy (immutable). */
    public RefreshToken revoke() {
        return new RefreshToken(tokenHash, accountId, expiresAt, true, createdAt);
    }

    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }

    public String  getTokenHash() { return tokenHash; }
    public UUID    getAccountId() { return accountId; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked()    { return revoked; }
    public Instant getCreatedAt() { return createdAt; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof RefreshToken r)) return false;
        return Objects.equals(tokenHash, r.tokenHash);
    }
    @Override public int hashCode() { return Objects.hash(tokenHash); }
    @Override public String toString() { return "RefreshToken{account=" + accountId + ", revoked=" + revoked + "}"; }
}
