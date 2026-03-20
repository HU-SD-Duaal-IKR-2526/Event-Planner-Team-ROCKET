package com.elton.eventplanner.event.infrastructure.persistence;

import com.elton.eventplanner.event.domain.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {

    List<EventJpaEntity> findByStatus(EventStatus status);
}
