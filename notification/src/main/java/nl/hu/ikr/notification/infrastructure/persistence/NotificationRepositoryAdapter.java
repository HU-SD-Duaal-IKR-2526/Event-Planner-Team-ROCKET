package nl.hu.ikr.notification.infrastructure.persistence;

import nl.hu.ikr.notification.domain.Notification;
import nl.hu.ikr.notification.infrastructure.persistence.NotificationRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final MongoNotificationRepository mongoRepository;

    public NotificationRepositoryAdapter(
            MongoNotificationRepository mongoRepository
    ) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Notification save(Notification notification) {

        NotificationDocument document =
                NotificationMapper.toDocument(notification);

        NotificationDocument saved =
                mongoRepository.save(document);

        return NotificationMapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<Notification> findByUserId(UUID userId) {

        return mongoRepository.findByUserId(userId.toString())
                .stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }
}