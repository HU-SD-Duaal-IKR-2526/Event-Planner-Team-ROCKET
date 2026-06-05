package nl.teamrocket.core.event.adapter.jpa.repository;

import nl.teamrocket.core.event.adapter.jpa.entity.EventJpaEntity;
import nl.teamrocket.core.event.domain.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {

    /**
     * Filter-query met null-tolerantie: ontbrekende filters worden genegeerd.
     * Bewust JPQL ipv Specification — overzichtelijk voor twee parameters.
     */
    @Query("""
            SELECT e FROM EventJpaEntity e
             WHERE (:status IS NULL OR e.status = :status)
               AND (:organizerId IS NULL OR e.organizerId = :organizerId)
             ORDER BY e.startsAt ASC
            """)
    List<EventJpaEntity> findByFilter(@Param("status") EventStatus status,
                                      @Param("organizerId") UUID organizerId);
}
