package nl.hu.ikr.notification.infrastructure.persistence;

import nl.hu.ikr.notification.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findByUserId(UUID userId);
}