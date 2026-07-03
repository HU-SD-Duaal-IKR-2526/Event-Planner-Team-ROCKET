package nl.hu.ikr.schedule.repository;

import nl.hu.ikr.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    Optional<Schedule> findByEventId(UUID eventId);
}

