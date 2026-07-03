package nl.hu.ikr.notification.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface MongoNotificationRepository extends MongoRepository<NotificationDocument, UUID> {

    List<NotificationDocument> findByUserId(String userId);
}