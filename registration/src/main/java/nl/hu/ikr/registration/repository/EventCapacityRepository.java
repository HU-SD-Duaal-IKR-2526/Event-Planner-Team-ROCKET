package nl.hu.ikr.registration.repository;

import nl.hu.ikr.registration.domain.EventCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EventCapacityRepository extends JpaRepository<EventCapacity, UUID> {

    /**
     * Vergrendelt de rij pessimistisch voor capaciteitscheck.
     * Vereist actieve @Transactional context.
     */
    @Query(value = "SELECT * FROM event_capacity WHERE event_id = :eventId FOR UPDATE",
           nativeQuery = true)
    Optional<EventCapacity> lockByEventId(@Param("eventId") UUID eventId);
}

