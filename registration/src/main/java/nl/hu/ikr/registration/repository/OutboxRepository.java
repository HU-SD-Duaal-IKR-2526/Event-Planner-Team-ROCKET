package nl.hu.ikr.registration.repository;

import nl.hu.ikr.registration.domain.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

    /**
     * Haalt maximaal 50 PENDING berichten op, oudste eerst.
     */
    @Query("""
            SELECT m FROM OutboxMessage m
            WHERE m.status = 'PENDING'
            ORDER BY m.createdAt ASC
            LIMIT 50
            """)
    List<OutboxMessage> findPending();
}

