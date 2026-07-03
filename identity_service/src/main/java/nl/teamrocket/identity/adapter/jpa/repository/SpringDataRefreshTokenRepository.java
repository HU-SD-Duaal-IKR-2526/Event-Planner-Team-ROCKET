package nl.teamrocket.identity.adapter.jpa.repository;

import nl.teamrocket.identity.adapter.jpa.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);
    void deleteByAccountId(UUID accountId);
    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.expiresAt < :now")
    void deleteExpired(Instant now);
}
