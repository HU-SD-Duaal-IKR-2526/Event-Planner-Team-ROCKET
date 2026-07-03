package nl.teamrocket.identity.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import nl.teamrocket.identity.domain.model.AccountStatus;
import nl.teamrocket.identity.domain.model.Role;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity for Account aggregate.
 * Kept in the adapter layer; domain model is never exposed to JPA annotations.
 */
@Entity
@Table(
    name = "accounts",
    schema = "identity",
    uniqueConstraints = @UniqueConstraint(name = "uq_accounts_email", columnNames = "email")
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "account_roles",
        schema = "identity",
        joinColumns = @JoinColumn(name = "account_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expires_at")
    private Instant verificationTokenExpiresAt;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private Instant resetTokenExpiresAt;

    @Column(name = "reset_token_used")
    private boolean resetTokenUsed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private long version;
}
