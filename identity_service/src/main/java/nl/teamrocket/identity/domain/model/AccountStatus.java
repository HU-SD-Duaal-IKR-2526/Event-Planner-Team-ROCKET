package nl.teamrocket.identity.domain.model;

/**
 * Value Object (enum): lifecycle status of an Account.
 *
 * Allowed transitions:
 *   PENDING  → ACTIVE    (after email verification)
 *   ACTIVE   → LOCKED    (after 5 failed login attempts)
 *   LOCKED   → ACTIVE    (admin unlock)
 *   ACTIVE   → SUSPENDED (admin action)
 *   any      → DELETED   (soft-delete, GDPR)
 *
 * EXPLICITLY NOT: this is not HTTP status or log level.
 */
public enum AccountStatus {
    /**
     * Account created, email not yet verified.
     */
    PENDING,

    /**
     * Email verified; account fully operational.
     */
    ACTIVE,

    /**
     * Locked after 5 consecutive failed login attempts.
     * Requires admin action or password reset to unlock.
     */
    LOCKED,

    /**
     * Suspended by an administrator.
     */
    SUSPENDED,

    /**
     * Soft-deleted; credentials retained for audit but login impossible.
     */
    DELETED
}
