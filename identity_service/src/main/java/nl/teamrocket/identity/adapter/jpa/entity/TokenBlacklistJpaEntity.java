package nl.teamrocket.identity.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for blacklisted access tokens.
 * When a user logs out or changes password, the current access token's
 * jti (JWT ID) is blacklisted until its natural expiry.
 *
 * Architecture doc: "token-blacklist" is listed as an Identity responsibility.
 * Entries are cleaned up automatically after the token's exp timestamp.
 */
@Entity
@Table(
    name = "token_blacklist",
    schema = "identity",
    indexes = @Index(name = "idx_blacklist_jti", columnList = "jti")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TokenBlacklistJpaEntity {

    @Id
    private UUID id;

    /** JWT ID (jti claim) — the unique identifier of the blacklisted token. */
    @Column(name = "jti", nullable = false, unique = true, length = 36)
    private String jti;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** When the original JWT expires — entries can be safely deleted after this. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "blacklisted_at", nullable = false, updatable = false)
    private Instant blacklistedAt;
}
