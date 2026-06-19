package nl.hu.ikr.notification.domain;

import java.time.Instant;
import java.util.UUID;


public class Notification {
    public Notification(UUID id, UUID userId, String title, String message, NotificationType type, NotificationStatus status, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    private UUID id;

    private UUID userId;

    private String title;

    private String message;

    private NotificationType type;

    private NotificationStatus status;

    private Instant createdAt;

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}