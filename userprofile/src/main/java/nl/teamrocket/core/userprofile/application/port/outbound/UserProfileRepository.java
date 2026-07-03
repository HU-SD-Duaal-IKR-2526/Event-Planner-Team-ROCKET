package nl.teamrocket.core.userprofile.application.port.outbound;
import nl.teamrocket.core.userprofile.domain.model.UserProfile;
import java.util.Optional;
import java.util.UUID;
public interface UserProfileRepository {
    UserProfile save(UserProfile profile);
    Optional<UserProfile> findByAccountId(UUID accountId);
    boolean existsByAccountId(UUID accountId);
    void deleteByAccountId(UUID accountId);
}
