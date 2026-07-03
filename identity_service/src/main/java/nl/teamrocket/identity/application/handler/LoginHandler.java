package nl.teamrocket.identity.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.adapter.jpa.entity.RefreshTokenJpaEntity;
import nl.teamrocket.identity.adapter.jpa.repository.SpringDataRefreshTokenRepository;
import nl.teamrocket.identity.application.command.LoginCommand;
import nl.teamrocket.identity.application.event.IdentityDomainEvents;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.identity.domain.exception.AccountLockedException;
import nl.teamrocket.identity.domain.exception.InvalidCredentialsException;
import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.domain.model.AccountStatus;
import nl.teamrocket.identity.domain.model.Email;
import nl.teamrocket.identity.domain.model.Role;
import nl.teamrocket.identity.security.CorrelationContext;
import nl.teamrocket.identity.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the Login use case.
 *
 * Flow:
 *  1. Find account by e-mail
 *  2. Verify password (record failed attempt on mismatch → auto-lock at 5)
 *  3. Assert account is ACTIVE
 *  4. Issue RS256 access token (JWT) + opaque refresh token
 *  5. Persist refresh token (hashed) in DB
 *  6. Publish AccountLoggedIn domain event → consumed by Audit BC
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHandler {

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SpringDataRefreshTokenRepository refreshTokenRepo;

    @Transactional
    public LoginResult handle(LoginCommand cmd) {
        Email email = new Email(cmd.email());
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        boolean matches = passwordEncoder.matches(
                cmd.rawPassword(), account.getPasswordHash().getHash());

        if (!matches) {
            account.recordFailedLogin();
            accountRepository.save(account);
            if (account.getStatus() == AccountStatus.LOCKED) {
                eventPublisher.publish("account.locked.v1",
                        IdentityDomainEvents.AccountLocked.from(account, CorrelationContext.current()));
                log.warn("Account locked: {}", account.getId());
            }
            throw new InvalidCredentialsException();
        }

        account.assertActive();
        account.recordSuccessfulLogin();
        accountRepository.save(account);

        // Issue RS256 access token
        var roles = account.getRoles().stream().map(Role::name).collect(Collectors.toSet());
        String accessToken = jwtTokenProvider.generateAccessToken(
                account.getId(), account.getEmail().getValue(), roles);

        // Issue opaque refresh token — stored hashed in DB
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken();
        refreshTokenRepo.save(RefreshTokenJpaEntity.builder()
                .id(UUID.randomUUID())
                .tokenHash(RefreshTokenHandler.sha256(rawRefreshToken))
                .accountId(account.getId())
                .expiresAt(Instant.now().plusSeconds(604_800L)) // 7 days
                .revoked(false)
                .createdAt(Instant.now())
                .build());

        // Publish to Audit BC
        eventPublisher.publish("account.login.v1",
                IdentityDomainEvents.AccountLoggedIn.from(account, CorrelationContext.current()));

        log.info("Login successful for account: {}", account.getId());
        return new LoginResult(
                account.getId(),
                account.getEmail().getValue(),
                roles,
                accessToken,
                jwtTokenProvider.getExpirationSeconds(),
                rawRefreshToken
        );
    }
}
