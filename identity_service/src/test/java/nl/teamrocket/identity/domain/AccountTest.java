package nl.teamrocket.identity.domain;

import nl.teamrocket.identity.domain.exception.*;
import nl.teamrocket.identity.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Account aggregate")
class AccountTest {

    private Account freshPendingAccount() {
        return Account.register(
                new Email("test@example.com"),
                new PasswordHash("$2a$12$hashed")
        );
    }

    private Account activeAccount() {
        Account a = freshPendingAccount();
        a.verifyEmail(a.getVerificationToken().getToken());
        return a;
    }

    // ── Registration ─────────────────────────────────────────────

    @Nested @DisplayName("register()")
    class Register {
        @Test void starts_in_PENDING_status() {
            assertThat(freshPendingAccount().getStatus()).isEqualTo(AccountStatus.PENDING);
        }
        @Test void assigns_GUEST_role_by_default() {
            assertThat(freshPendingAccount().getRoles()).containsExactly(Role.GUEST);
        }
        @Test void generates_verification_token() {
            assertThat(freshPendingAccount().getVerificationToken()).isNotNull();
            assertThat(freshPendingAccount().getVerificationToken().getToken()).isNotBlank();
        }
        @Test void throws_when_email_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Account.register(null, new PasswordHash("hash")));
        }
    }

    // ── Email verification ────────────────────────────────────────

    @Nested @DisplayName("verifyEmail()")
    class VerifyEmail {
        @Test void transitions_PENDING_to_ACTIVE() {
            Account a = freshPendingAccount();
            a.verifyEmail(a.getVerificationToken().getToken());
            assertThat(a.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(a.getVerificationToken()).isNull();
        }
        @Test void throws_with_wrong_token() {
            assertThatThrownBy(() -> freshPendingAccount().verifyEmail("wrong"))
                    .isInstanceOf(InvalidVerificationTokenException.class);
        }
        @Test void throws_when_already_active() {
            Account a = freshPendingAccount();
            a.verifyEmail(a.getVerificationToken().getToken());
            assertThatThrownBy(() -> a.verifyEmail("any"))
                    .isInstanceOf(AccountAlreadyVerifiedException.class);
        }
    }

    // ── Login locking ─────────────────────────────────────────────

    @Nested @DisplayName("recordFailedLogin() — locking")
    class FailedLogin {
        @Test void locks_after_5_failures() {
            Account a = activeAccount();
            for (int i = 0; i < 5; i++) a.recordFailedLogin();
            assertThat(a.getStatus()).isEqualTo(AccountStatus.LOCKED);
            assertThat(a.getFailedLoginAttempts()).isEqualTo(5);
        }
        @Test void does_not_lock_before_5_failures() {
            Account a = activeAccount();
            for (int i = 0; i < 4; i++) a.recordFailedLogin();
            assertThat(a.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        }
        @Test void successful_login_resets_counter() {
            Account a = activeAccount();
            a.recordFailedLogin();
            a.recordFailedLogin();
            a.recordSuccessfulLogin();
            assertThat(a.getFailedLoginAttempts()).isZero();
        }
    }

    // ── assertActive guards ───────────────────────────────────────

    @Nested @DisplayName("assertActive() guards")
    class AssertActive {
        @Test void PENDING_throws_AccountNotVerifiedException() {
            assertThatThrownBy(() -> freshPendingAccount().assertActive())
                    .isInstanceOf(AccountNotVerifiedException.class);
        }
        @Test void LOCKED_throws_AccountLockedException() {
            Account a = activeAccount();
            for (int i = 0; i < 5; i++) a.recordFailedLogin();
            assertThatThrownBy(a::assertActive).isInstanceOf(AccountLockedException.class);
        }
        @Test void ACTIVE_does_not_throw() {
            assertThatNoException().isThrownBy(() -> activeAccount().assertActive());
        }
    }

    // ── Password reset ────────────────────────────────────────────

    @Nested @DisplayName("resetPassword()")
    class ResetPassword {
        @Test void applies_new_password_and_unlocks_LOCKED_account() {
            Account a = activeAccount();
            for (int i = 0; i < 5; i++) a.recordFailedLogin();
            assertThat(a.getStatus()).isEqualTo(AccountStatus.LOCKED);

            ResetToken token = a.initiatePasswordReset();
            a.resetPassword(token.getToken(), new PasswordHash("$2a$12$newhash"));

            assertThat(a.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(a.getFailedLoginAttempts()).isZero();
        }
        @Test void throws_for_invalid_token() {
            Account a = activeAccount();
            a.initiatePasswordReset();
            assertThatThrownBy(() -> a.resetPassword("wrong", new PasswordHash("hash")))
                    .isInstanceOf(InvalidResetTokenException.class);
        }
        @Test void reset_token_is_single_use() {
            Account a = activeAccount();
            ResetToken token = a.initiatePasswordReset();
            a.resetPassword(token.getToken(), new PasswordHash("$2a$12$new"));
            // Second use with same token must fail
            assertThatThrownBy(() -> a.resetPassword(token.getToken(), new PasswordHash("$2a$12$other")))
                    .isInstanceOf(ResetTokenExpiredException.class);
        }
    }

    // ── Role management ───────────────────────────────────────────

    @Nested @DisplayName("Role management")
    class RoleManagement {
        @Test void assign_adds_role() {
            Account a = freshPendingAccount();
            a.assignRole(Role.ORGANIZER);
            assertThat(a.getRoles()).contains(Role.ORGANIZER);
        }
        @Test void revoke_removes_role() {
            Account a = freshPendingAccount();
            a.assignRole(Role.ORGANIZER);
            a.revokeRole(Role.ORGANIZER);
            assertThat(a.getRoles()).doesNotContain(Role.ORGANIZER);
        }
        @Test void role_is_platform_wide_not_per_event() {
            // GUEST, ORGANIZER, ADMIN, COMPLIANCE — not event-specific
            assertThat(Role.values()).containsExactlyInAnyOrder(
                    Role.GUEST, Role.ORGANIZER, Role.ADMIN, Role.COMPLIANCE);
        }
    }

    // ── Soft delete ───────────────────────────────────────────────

    @Nested @DisplayName("softDelete()")
    class SoftDelete {
        @Test void sets_DELETED_status() {
            Account a = activeAccount();
            a.softDelete();
            assertThat(a.getStatus()).isEqualTo(AccountStatus.DELETED);
        }
        @Test void DELETED_account_cannot_login() {
            Account a = activeAccount();
            a.softDelete();
            assertThatThrownBy(a::assertActive).isInstanceOf(AccountDeletedException.class);
        }
    }

    // ── VerificationToken invariants ──────────────────────────────

    @Nested @DisplayName("VerificationToken — Living Glossary invariants")
    class VerificationTokenInvariants {
        @Test void is_not_a_session_token() {
            // VerificationToken is EXPLICITLY NOT a session token
            // It expires after 24 hours
            VerificationToken vt = VerificationToken.generate();
            assertThat(vt.isExpired()).isFalse();
            assertThat(vt.getExpiresAt()).isAfter(java.time.Instant.now());
        }
        @Test void is_not_a_reset_token_separate_VO() {
            // ResetToken is a SEPARATE value object from VerificationToken
            Account a = activeAccount();
            ResetToken rt = a.initiatePasswordReset();
            assertThat(rt).isNotInstanceOf(VerificationToken.class);
        }
    }
}
