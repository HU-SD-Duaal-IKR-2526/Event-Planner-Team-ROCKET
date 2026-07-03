package nl.teamrocket.core.userprofile.adapter.mongo;

import lombok.RequiredArgsConstructor;
import nl.teamrocket.core.userprofile.adapter.mongo.mapper.ProfileDocumentMapper;
import nl.teamrocket.core.userprofile.adapter.mongo.repository.SpringDataProfileRepository;
import nl.teamrocket.core.userprofile.application.port.outbound.UserProfileRepository;
import nl.teamrocket.core.userprofile.domain.model.UserProfile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component @RequiredArgsConstructor
public class MongoUserProfileRepository implements UserProfileRepository {
    private final SpringDataProfileRepository repo;
    private final ProfileDocumentMapper mapper;

    @Override public UserProfile save(UserProfile p) {
        var saved = repo.save(mapper.toDocument(p));
        UserProfile result = mapper.toDomain(saved);
        result.setVersion(saved.getVersion());
        return result;
    }
    @Override public Optional<UserProfile> findByAccountId(UUID accountId) {
        return repo.findByAccountId(accountId).map(mapper::toDomain);
    }
    @Override public boolean existsByAccountId(UUID accountId) {
        return repo.existsByAccountId(accountId);
    }
    @Override public void deleteByAccountId(UUID accountId) {
        repo.deleteByAccountId(accountId);
    }
}
