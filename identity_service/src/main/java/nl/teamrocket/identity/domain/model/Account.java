package nl.teamrocket.identity.domain.model;

import nl.teamrocket.identity.domain.exception.*;

import java.time.Instant;
import java.util.*;

/**
 * Aggregate Root: Account.
 *
 * An Account is a digital access right to the platform, uniquely identified
 * by an e-mail address and secured with a password.
 *
 * Invariants enforced here:
 *  - E-mail is unique within the system (domain-wide, enforced via DB constraint + this)
 *  - Status: PENDING → ACTIVE after verification
 *  - Locked after MAX_FAILED_ATTEMPTS consecutive failed logins
 *  - VerificationToken valid < 24h, single-use
 *  - ResetToken valid < 1h, single-use
 *
 * EXPLICITLY NOT: Account is NOT UserProfile.
 *                 Account is purely authentication; profile info lives in User/Profile BC.
 */
public class Account {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    // ── Identity ──────────────────────────────────────────
    private final UUID id;
    private Email email;
    private PasswordHash passwordHash;
    private AccountStatus status;
    private Set<Role> roles;

    // ── Auth state ────────────────────────────────────────
    private int failedLoginAttempts;
    private VerificationToken verificationToken;
    private ResetToken resetToken;

    // ── Audit fields ──────────────────────────────────────
    private final Instant createdAt;
    private Instant updatedAt;

    // ── Domain events (collected, published by application layer) ──
    private final List<Object> domainEvents = new ArrayList<>();

    // ── Private constructor (use factory methods) ─────────
    private Account(UUID id, Email email, PasswordHash passwordHash,
                    AccountStatus status, Set<Role> roles, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.roles = new HashSet<>(roles);
        this.failedLoginAttempts = 0;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    // ════════════════════════════════════════════════════════
    // Factory methods
    // ════════════════════════════════════════════════════════

    /**
     * Register a new account. Status starts at PENDING; verification token is generated.
     * The caller is responsible for ensuring e-mail uniqueness (DB constraint is the final guard).
     */
    public static Account register(Email email, PasswordHash passwordHash) {
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(passwordHash, "PasswordHash is required");

        Instant now = Instant.now();
        Account account = new Account(
                UUID.randomUUID(),
                email,
                passwordHash,
                AccountStatus.PENDING,
                Set.of(Role.GUEST),
                now
        );
        account.verificationToken = VerificationToken.generate();
        return account;
    }

    /**
     * Reconstitute an Account from persistence (used by JPA adapter).
     * No domain events raised.
     */
    public static Account reconstitute(
            UUID id, Email email, PasswordHash passwordHash,
            AccountStatus status, Set<Role> roles,
            int failedLoginAttempts,
            VerificationToken verificationToken,
            ResetToken resetToken,
            Instant createdAt, Instant updatedAt) {
        Account account = new Account(id, email, passwordHash, status, roles, createdAt);
        account.failedLoginAttempts = failedLoginAttempts;
        account.verificationToken = verificationToken;
        account.resetToken = resetToken;
        account.updatedAt = updatedAt;
        return account;
    }

    // ════════════════════════════════════════════════════════
    // Business behaviour
    // ════════════════════════════════════════════════════════

    /**
     * Verify e-mail with the supplied token.
     * Transitions PENDING → ACTIVE.
     */
    public void verifyEmail(String token) {
        if (status != AccountStatus.PENDING) {
            throw new AccountAlreadyVerifiedException(id);
        }
        if (verificationToken == null) {
            throw new NoVerificationTokenException(id);
        }
        if (verificationToken.isExpired()) {
            throw new VerificationTokenExpiredException(id);
        }
        if (!verificationToken.getToken().equals(token)) {
            throw new InvalidVerificationTokenException(id);
        }
        this.status = AccountStatus.ACTIVE;
        this.verificationToken = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Record a successful login — resets the failed-attempt counter.
     */
    public void recordSuccessfulLogin() {
        assertActive();
        this.failedLoginAttempts = 0;
        this.updatedAt = Instant.now();
    }

    /**
     * Record a failed login attempt. Locks account after MAX_FAILED_ATTEMPTS.
     */
    public void recordFailedLogin() {
        if (status == AccountStatus.LOCKED || status == AccountStatus.DELETED) {
            return; // already locked, nothing to change
        }
        this.failedLoginAttempts++;
        this.updatedAt = Instant.now();
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.status = AccountStatus.LOCKED;
        }
    }

    /**
     * Generate a new password-reset token. Old one (if any) is replaced.
     * Works for ACTIVE and LOCKED accounts (reset also unlocks).
     */
    public ResetToken initiatePasswordReset() {
        if (status == AccountStatus.DELETED || status == AccountStatus.SUSPENDED) {
            throw new AccountNotEligibleForResetException(id, status);
        }
        this.resetToken = ResetToken.generate();
        this.updatedAt = Instant.now();
        return this.resetToken;
    }

    /**
     * Apply a new password using a valid reset token.
     * If account was LOCKED, it transitions back to ACTIVE.
     */
    public void resetPassword(String token, PasswordHash newPasswordHash) {
        Objects.requireNonNull(newPasswordHash, "New password hash is required");
        if (resetToken == null || !resetToken.getToken().equals(token)) {
            throw new InvalidResetTokenException(id);
        }
        if (!resetToken.isValid()) {
            throw new ResetTokenExpiredException(id);
        }
        this.passwordHash = newPasswordHash;
        this.resetToken = resetToken.markUsed();
        this.failedLoginAttempts = 0;
        if (this.status == AccountStatus.LOCKED) {
            this.status = AccountStatus.ACTIVE;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Assign a role to this account (admin operation).
     */
    public void assignRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");
        this.roles.add(role);
        this.updatedAt = Instant.now();
    }

    /**
     * Revoke a role from this account.
     */
    public void revokeRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");
        this.roles.remove(role);
        this.updatedAt = Instant.now();
    }

    /**
     * Regenerate a verification token (e.g. user asks for a new one after expiry).
     */
    public VerificationToken regenerateVerificationToken() {
        if (status != AccountStatus.PENDING) {
            throw new AccountAlreadyVerifiedException(id);
        }
        this.verificationToken = VerificationToken.generate();
        this.updatedAt = Instant.now();
        return this.verificationToken;
    }

    /**
     * Soft-delete (GDPR). Credentials stay for audit trail but login is impossible.
     */
    public void softDelete() {
        this.status = AccountStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    // ════════════════════════════════════════════════════════
    // Guards
    // ════════════════════════════════════════════════════════

    public void assertActive() {
        switch (status) {
            case PENDING   -> throw new AccountNotVerifiedException(id);
            case LOCKED    -> throw new AccountLockedException(id);
            case SUSPENDED -> throw new AccountSuspendedException(id);
            case DELETED   -> throw new AccountDeletedException(id);
            default        -> { /* ACTIVE — ok */ }
        }
    }

    // ════════════════════════════════════════════════════════
    // Domain events
    // ════════════════════════════════════════════════════════

    public void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    // ════════════════════════════════════════════════════════
    // Getters (no setters — mutations through behaviour methods)
    // ════════════════════════════════════════════════════════

    public UUID getId() { return id; }
    public Email getEmail() { return email; }
    public PasswordHash getPasswordHash() { return passwordHash; }
    public AccountStatus getStatus() { return status; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public VerificationToken getVerificationToken() { return verificationToken; }
    public ResetToken getResetToken() { return resetToken; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Account{id=" + id + ", email=" + email + ", status=" + status + "}";
    }
}
