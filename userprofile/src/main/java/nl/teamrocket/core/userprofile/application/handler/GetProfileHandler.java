package nl.teamrocket.core.userprofile.application.handler;

import lombok.RequiredArgsConstructor;
import nl.teamrocket.core.userprofile.application.command.GetProfileQuery;
import nl.teamrocket.core.userprofile.application.port.outbound.UserProfileRepository;
import nl.teamrocket.core.userprofile.domain.exception.ProfileNotFoundException;
import nl.teamrocket.core.userprofile.domain.model.UserProfile;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class GetProfileHandler {
    private final UserProfileRepository repo;
    public UserProfile handle(GetProfileQuery query) {
        return repo.findByAccountId(query.accountId())
                .orElseThrow(() -> new ProfileNotFoundException(query.accountId()));
    }
}
