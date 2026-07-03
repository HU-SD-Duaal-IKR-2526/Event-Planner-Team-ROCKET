package nl.teamrocket.identity.adapter.jpa.repository;

import nl.teamrocket.identity.adapter.jpa.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {

    Optional<AccountJpaEntity> findByEmail(String email);

    Optional<AccountJpaEntity> findByVerificationToken(String verificationToken);

    Optional<AccountJpaEntity> findByResetToken(String resetToken);

    boolean existsByEmail(String email);
}
