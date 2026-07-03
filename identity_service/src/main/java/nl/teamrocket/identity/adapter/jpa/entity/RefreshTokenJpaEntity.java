package nl.teamrocket.identity.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for refresh tokens.
 * Stored in identity.refresh_tokens table.
 * Token value is hashed (SHA-256) before storage.
 */
@Entity
@Table(
    name = "refresh_tokens",
    schema = "identity",
    indexes = @Index(name = "idx_refresh_tokens_account", columnList = "account_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshTokenJpaEntity {

    @Id
    @Column(name = "id", updatable = false)
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;   // SHA-256 hex of the raw token

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
