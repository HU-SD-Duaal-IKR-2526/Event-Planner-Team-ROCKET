package nl.teamrocket.identity.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.adapter.jpa.entity.RefreshTokenJpaEntity;
import nl.teamrocket.identity.adapter.jpa.entity.TokenBlacklistJpaEntity;
import nl.teamrocket.identity.adapter.jpa.repository.SpringDataRefreshTokenRepository;
import nl.teamrocket.identity.adapter.jpa.repository.SpringDataTokenBlacklistRepository;
import nl.teamrocket.identity.application.command.LogoutCommand;
import nl.teamrocket.identity.application.command.RefreshTokenCommand;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.domain.exception.AccountNotFoundException;
import nl.teamrocket.identity.domain.exception.InvalidResetTokenException;
import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.domain.model.Role;
import nl.teamrocket.identity.security.JwtTokenProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles refresh token rotation and logout (token blacklisting).
 *
 * Refresh flow (token rotation):
 *  1. Receive opaque refresh token from client
 *  2. Hash it → look up in DB
 *  3. Validate (not expired, not revoked)
 *  4. Revoke old refresh token
 *  5. Issue new access token (RS256 JWT) + new refresh token
 *  6. Return both to client
 *
 * Logout flow:
 *  1. Blacklist the current access token's jti until its natural expiry
 *  2. Revoke the refresh token
 *
 * Architecture doc requirement: "token-blacklist" is listed as Identity responsibility.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenHandler {

    private final SpringDataRefreshTokenRepository refreshRepo;
    private final SpringDataTokenBlacklistRepository blacklistRepo;
    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // ── Refresh ──────────────────────────────────────────────────────

    @Transactional
    public LoginResult refresh(RefreshTokenCommand cmd) {
        String hash = sha256(cmd.refreshToken());
        RefreshTokenJpaEntity entity = refreshRepo.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidResetTokenException(null));

        if (entity.isRevoked() || entity.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidResetTokenException(entity.getAccountId());
        }

        Account account = accountRepository.findById(entity.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(entity.getAccountId()));
        account.assertActive();

        // Rotate: revoke old, create new
        entity.setRevoked(true);
        refreshRepo.save(entity);

        String newRawRefreshToken = jwtTokenProvider.generateRefreshToken();
        RefreshTokenJpaEntity newEntity = RefreshTokenJpaEntity.builder()
                .id(UUID.randomUUID())
                .tokenHash(sha256(newRawRefreshToken))
                .accountId(account.getId())
                .expiresAt(Instant.now().plusSeconds(604_800L))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        refreshRepo.save(newEntity);

        var roles = account.getRoles().stream().map(Role::name).collect(Collectors.toSet());
        String accessToken = jwtTokenProvider.generateAccessToken(
                account.getId(), account.getEmail().getValue(), roles);

        log.info("Refresh token rotated for account {}", account.getId());
        return new LoginResult(account.getId(), account.getEmail().getValue(),
                roles, accessToken, jwtTokenProvider.getExpirationSeconds(),
                newRawRefreshToken);
    }

    // ── Logout ───────────────────────────────────────────────────────

    @Transactional
    public void logout(LogoutCommand cmd) {
        // 1. Blacklist the access token's jti
        if (cmd.accessTokenJti() != null && !cmd.accessTokenJti().isBlank()) {
            if (!blacklistRepo.existsByJti(cmd.accessTokenJti())) {
                blacklistRepo.save(TokenBlacklistJpaEntity.builder()
                        .id(UUID.randomUUID())
                        .jti(cmd.accessTokenJti())
                        .accountId(cmd.accountId())
                        .expiresAt(Instant.now().plusSeconds(jwtTokenProvider.getExpirationSeconds()))
                        .blacklistedAt(Instant.now())
                        .build());
            }
        }

        // 2. Revoke refresh token
        String hash = sha256(cmd.refreshToken());
        refreshRepo.findByTokenHash(hash).ifPresent(entity -> {
            entity.setRevoked(true);
            refreshRepo.save(entity);
        });

        log.info("Account {} logged out, token blacklisted", cmd.accountId());
    }

    /** Check whether an access token's jti has been blacklisted (called by security filter). */
    public boolean isBlacklisted(String jti) {
        return blacklistRepo.existsByJti(jti);
    }

    // ── Cleanup ──────────────────────────────────────────────────────

    /** Scheduled cleanup: remove expired refresh tokens and blacklist entries daily. */
    @Scheduled(cron = "0 0 3 * * *")   // 03:00 every day
    @Transactional
    public void cleanupExpired() {
        Instant now = Instant.now();
        refreshRepo.deleteExpired(now);
        blacklistRepo.deleteExpired(now);
        log.info("Expired token cleanup completed");
    }

    // ── Helpers ──────────────────────────────────────────────────────

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
