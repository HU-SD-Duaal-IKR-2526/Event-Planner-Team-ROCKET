package nl.teamrocket.identity.adapter.jpa.repository;

import nl.teamrocket.identity.adapter.jpa.entity.TokenBlacklistJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface SpringDataTokenBlacklistRepository extends JpaRepository<TokenBlacklistJpaEntity, UUID> {
    boolean existsByJti(String jti);
    @Modifying
    @Query("DELETE FROM TokenBlacklistJpaEntity t WHERE t.expiresAt < :now")
    void deleteExpired(Instant now);
}
