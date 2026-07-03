package nl.hu.ikr.notification.infrastructure.persistence;

import nl.hu.ikr.notification.domain.Notification;

import java.util.UUID;

public class NotificationMapper {

    private NotificationMapper() {}

    public static NotificationDocument toDocument(Notification notification) {

        NotificationDocument document = new NotificationDocument();

        document.setId(notification.getId().toString());
        document.setUserId(notification.getUserId().toString());
        document.setTitle(notification.getTitle());
        document.setMessage(notification.getMessage());
        document.setType(notification.getType());
        document.setStatus(notification.getStatus());
        document.setCreatedAt(notification.getCreatedAt());

        return document;
    }

    public static Notification toDomain(NotificationDocument document) {

        return new Notification(
                UUID.fromString(document.getId()),
                UUID.fromString(document.getUserId()),
                document.getTitle(),
                document.getMessage(),
                document.getType(),
                document.getStatus(),
                document.getCreatedAt()
        );
    }
}