package nl.teamrocket.identity.domain.model;

import java.util.Objects;

/**
 * Value Object: Hashed password.
 * Invariant: never stores plaintext — only the BCrypt hash.
 * The hash is already computed before this VO is constructed.
 */
public final class PasswordHash {

    private final String hash;

    public PasswordHash(String hash) {
        Objects.requireNonNull(hash, "Password hash must not be null");
        if (hash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    /** Equality on hash value, not identity. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PasswordHash that)) return false;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    /** Never expose the hash in toString — security hygiene. */
    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
