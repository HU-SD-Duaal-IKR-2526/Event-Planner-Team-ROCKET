package nl.teamrocket.identity.adapter.jpa.mapper;

import nl.teamrocket.identity.adapter.jpa.entity.AccountJpaEntity;
import nl.teamrocket.identity.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Maps between Account aggregate (domain) and AccountJpaEntity (infrastructure).
 * Lives in the adapter layer; domain model has no knowledge of this.
 */
@Component
public class AccountMapper {

    public AccountJpaEntity toEntity(Account account) {
        return AccountJpaEntity.builder()
                .id(account.getId())
                .email(account.getEmail().getValue())
                .passwordHash(account.getPasswordHash().getHash())
                .status(account.getStatus())
                .roles(new HashSet<>(account.getRoles()))
                .failedLoginAttempts(account.getFailedLoginAttempts())
                .verificationToken(account.getVerificationToken() != null
                        ? account.getVerificationToken().getToken() : null)
                .verificationTokenExpiresAt(account.getVerificationToken() != null
                        ? account.getVerificationToken().getExpiresAt() : null)
                .resetToken(account.getResetToken() != null
                        ? account.getResetToken().getToken() : null)
                .resetTokenExpiresAt(account.getResetToken() != null
                        ? account.getResetToken().getExpiresAt() : null)
                .resetTokenUsed(account.getResetToken() != null
                        && account.getResetToken().isUsed())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public Account toDomain(AccountJpaEntity e) {
        VerificationToken verificationToken = null;
        if (e.getVerificationToken() != null) {
            verificationToken = new VerificationToken(
                    e.getVerificationToken(), e.getVerificationTokenExpiresAt());
        }

        ResetToken resetToken = null;
        if (e.getResetToken() != null) {
            resetToken = new ResetToken(
                    e.getResetToken(), e.getResetTokenExpiresAt(), e.isResetTokenUsed());
        }

        return Account.reconstitute(
                e.getId(),
                new Email(e.getEmail()),
                new PasswordHash(e.getPasswordHash()),
                e.getStatus(),
                e.getRoles(),
                e.getFailedLoginAttempts(),
                verificationToken,
                resetToken,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
