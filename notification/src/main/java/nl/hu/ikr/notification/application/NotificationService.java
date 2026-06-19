package nl.hu.ikr.notification.application;

import nl.hu.ikr.notification.domain.*;
import nl.hu.ikr.notification.infrastructure.persistence.*;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public Notification createNotification(
            UUID userId,
            String title,
            String message,
            NotificationType type
    ) {

        Notification notification = new Notification(
                UUID.randomUUID(),
                userId,
                title,
                message,
                type,
                NotificationStatus.PENDING,
                Instant.now()
        );

        return repository.save(notification);
    }

    public List<Notification> getNotifications(UUID userId) {
        return repository.findByUserId(userId);
    }
}