package nl.hu.ikr.notification.infrastructure.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.hu.ikr.notification.domain.NotificationStatus;
import nl.hu.ikr.notification.domain.NotificationType;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "notifications")
public class NotificationDocument {

    @Id
    private String id;

    private String userId;

    private String title;

    private String message;

    private NotificationType type;

    private NotificationStatus status;

    private Instant createdAt;
}