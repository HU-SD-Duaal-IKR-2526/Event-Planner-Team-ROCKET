package nl.teamrocket.core.userprofile.adapter.mongo.repository;

import nl.teamrocket.core.userprofile.adapter.mongo.document.UserProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataProfileRepository extends MongoRepository<UserProfileDocument, String> {
    Optional<UserProfileDocument> findByAccountId(UUID accountId);
    boolean existsByAccountId(UUID accountId);
    void deleteByAccountId(UUID accountId);
}
