package nl.teamrocket.core.userprofile.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.userprofile.application.command.CreateProfileCommand;
import nl.teamrocket.core.userprofile.application.event.UserProfileDomainEvents;
import nl.teamrocket.core.userprofile.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.core.userprofile.application.port.outbound.UserProfileRepository;
import nl.teamrocket.core.userprofile.domain.model.UserProfile;
import nl.teamrocket.core.shared.CorrelationContext;
import org.springframework.stereotype.Service;

/**
 * Creates a stub UserProfile when AccountRegistered event arrives from Identity BC.
 * Idempotent: skips silently if profile already exists.
 */
@Slf4j @Service @RequiredArgsConstructor
public class CreateProfileHandler {
    private final UserProfileRepository repo;
    private final DomainEventPublisher  publisher;

    public void handle(CreateProfileCommand cmd) {
        if (repo.existsByAccountId(cmd.accountId())) {
            log.debug("Profile already exists for account {}, skipping", cmd.accountId());
            return;
        }
        String localPart = cmd.email() != null && cmd.email().contains("@")
                ? cmd.email().substring(0, cmd.email().indexOf('@')) : "user";
        UserProfile profile = UserProfile.createStub(cmd.accountId(), localPart);
        profile = repo.save(profile);
        publisher.publish("profile.created.v1",
                UserProfileDomainEvents.ProfileCreated.from(profile, CorrelationContext.current()));
        log.info("Stub profile created for account {}", cmd.accountId());
    }
}
