package com.elton.eventplanner.event.infrastructure.persistence;

import com.elton.eventplanner.event.domain.model.EventStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {

    List<EventJpaEntity> findByStatus(EventStatus status);
}
