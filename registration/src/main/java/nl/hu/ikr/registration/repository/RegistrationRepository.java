package nl.hu.ikr.registration.repository;

import nl.hu.ikr.registration.domain.Registration;
import nl.hu.ikr.registration.domain.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {

    Optional<Registration> findByIdempotencyKey(String idempotencyKey);

    long countByEventIdAndStatusIn(UUID eventId, Collection<RegistrationStatus> statuses);

    /**
     * FIFO: oudste wachtende voor dit event ophalen.
     */
    @Query("""
            SELECT r FROM Registration r
            WHERE r.eventId = :eventId
              AND r.status = nl.hu.ikr.registration.domain.RegistrationStatus.WAITLISTED
            ORDER BY r.createdAt ASC
            LIMIT 1
            """)
    Optional<Registration> findNextWaitlisted(@Param("eventId") UUID eventId);
}

