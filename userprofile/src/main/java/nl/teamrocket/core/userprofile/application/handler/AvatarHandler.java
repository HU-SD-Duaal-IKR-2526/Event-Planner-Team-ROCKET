package nl.teamrocket.core.userprofile.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.userprofile.application.command.RemoveAvatarCommand;
import nl.teamrocket.core.userprofile.application.command.SetAvatarCommand;
import nl.teamrocket.core.userprofile.application.event.UserProfileDomainEvents;
import nl.teamrocket.core.userprofile.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.core.userprofile.application.port.outbound.UserProfileRepository;
import nl.teamrocket.core.userprofile.domain.exception.ProfileNotFoundException;
import nl.teamrocket.core.userprofile.domain.model.Avatar;
import nl.teamrocket.core.shared.CorrelationContext;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Handles avatar set and remove.
 * The SetAvatarCommand receives a URL + storageKey already uploaded to Object Storage
 * by the REST adapter (upload to S3 first, then call this handler with the result URL).
 */
@Slf4j @Service @RequiredArgsConstructor
public class AvatarHandler {
    private final UserProfileRepository repo;
    private final DomainEventPublisher  publisher;

    public void setAvatar(SetAvatarCommand cmd) {
        var profile = repo.findByAccountId(cmd.targetAccountId())
                .orElseThrow(() -> new ProfileNotFoundException(cmd.targetAccountId()));
        Avatar avatar = Avatar.create(cmd.url(), cmd.contentType(), cmd.sizeBytes());
        profile.setAvatar(cmd.requestingAccountId(), avatar);
        profile = repo.save(profile);
        publisher.publish("profile.avatar-updated.v1",
                new UserProfileDomainEvents.AvatarUpdated(
                        profile.getAccountId(), avatar.getUrl(), Instant.now(), CorrelationContext.current()));
        log.info("Avatar set for account {}", cmd.targetAccountId());
    }

    public void removeAvatar(RemoveAvatarCommand cmd) {
        var profile = repo.findByAccountId(cmd.targetAccountId())
                .orElseThrow(() -> new ProfileNotFoundException(cmd.targetAccountId()));
        profile.removeAvatar(cmd.requestingAccountId());
        repo.save(profile);
        publisher.publish("profile.avatar-updated.v1",
                new UserProfileDomainEvents.AvatarUpdated(
                        cmd.targetAccountId(), null, Instant.now(), CorrelationContext.current()));
        log.info("Avatar removed for account {}", cmd.targetAccountId());
    }
}
