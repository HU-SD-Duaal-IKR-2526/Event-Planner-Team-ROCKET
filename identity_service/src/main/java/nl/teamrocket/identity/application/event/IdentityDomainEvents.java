package nl.teamrocket.identity.application.event;

import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.domain.model.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain events published by the Identity BC to RabbitMQ.
 * These are the Published Language consumed by Audit and other BCs.
 * Routing keys follow the pattern: "account.{verb}.v1"
 */
public final class IdentityDomainEvents {

    private IdentityDomainEvents() {}

    /** account.registered.v1 — published after a new account is saved */
    public record AccountRegistered(
            UUID accountId,
            String email,
            Set<String> roles,
            Instant registeredAt,
            String correlationId
    ) {
        public static AccountRegistered from(Account account, String correlationId) {
            return new AccountRegistered(
                    account.getId(),
                    account.getEmail().getValue(),
                    account.getRoles().stream().map(Role::name).collect(Collectors.toSet()),
                    account.getCreatedAt(),
                    correlationId
            );
        }
    }

    /** account.verified.v1 — published after successful email verification */
    public record AccountVerified(
            UUID accountId,
            String email,
            Instant verifiedAt,
            String correlationId
    ) {
        public static AccountVerified from(Account account, String correlationId) {
            return new AccountVerified(
                    account.getId(),
                    account.getEmail().getValue(),
                    Instant.now(),
                    correlationId
            );
        }
    }

    /** account.login.v1 — published after each successful login (for audit) */
    public record AccountLoggedIn(
            UUID accountId,
            String email,
            Instant loggedInAt,
            String correlationId
    ) {
        public static AccountLoggedIn from(Account account, String correlationId) {
            return new AccountLoggedIn(
                    account.getId(),
                    account.getEmail().getValue(),
                    Instant.now(),
                    correlationId
            );
        }
    }

    /** account.locked.v1 — published when account is auto-locked */
    public record AccountLocked(
            UUID accountId,
            String email,
            Instant lockedAt,
            String correlationId
    ) {
        public static AccountLocked from(Account account, String correlationId) {
            return new AccountLocked(
                    account.getId(),
                    account.getEmail().getValue(),
                    Instant.now(),
                    correlationId
            );
        }
    }

    /** account.password-reset-requested.v1 */
    public record PasswordResetRequested(
            UUID accountId,
            String email,
            Instant requestedAt,
            String correlationId
    ) {
        public static PasswordResetRequested from(Account account, String correlationId) {
            return new PasswordResetRequested(
                    account.getId(),
                    account.getEmail().getValue(),
                    Instant.now(),
                    correlationId
            );
        }
    }

    /** account.role-assigned.v1 */
    public record RoleAssigned(
            UUID accountId,
            String role,
            Instant assignedAt,
            String correlationId
    ) {}
}
