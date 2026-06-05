package nl.hu.ikr.schedule.repository;

import nl.hu.ikr.schedule.domain.UserUpcomingEvent;
import nl.hu.ikr.schedule.domain.UserUpcomingEvent.UserUpcomingEventId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserUpcomingEventRepository extends JpaRepository<UserUpcomingEvent, UserUpcomingEventId> {
    List<UserUpcomingEvent> findByIdUserId(UUID userId);
}

