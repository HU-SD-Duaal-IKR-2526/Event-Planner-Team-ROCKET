package nl.teamrocket.core.userprofile.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.userprofile.application.command.UpdateProfileCommand;
import nl.teamrocket.core.userprofile.application.event.UserProfileDomainEvents;
import nl.teamrocket.core.userprofile.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.core.userprofile.application.port.outbound.UserProfileRepository;
import nl.teamrocket.core.userprofile.domain.exception.ProfileNotFoundException;
import nl.teamrocket.core.userprofile.domain.model.Preferences;
import nl.teamrocket.core.userprofile.domain.model.UserProfile;
import nl.teamrocket.core.shared.CorrelationContext;
import org.springframework.stereotype.Service;

/**
 * Handles the UpdateProfile use case.
 * After saving, publishes ProfileUpdated event with all 7 required fields
 * (including dietaryPreferences and accessibilityNeeds for Registration BC).
 */
@Slf4j @Service @RequiredArgsConstructor
public class UpdateProfileHandler {
    private final UserProfileRepository repo;
    private final DomainEventPublisher  publisher;

    public UserProfile handle(UpdateProfileCommand cmd) {
        UserProfile profile = repo.findByAccountId(cmd.targetAccountId())
                .orElseThrow(() -> new ProfileNotFoundException(cmd.targetAccountId()));

        Preferences newPrefs = buildPrefsIfChanged(cmd, profile.getPreferences());
        profile.update(cmd.requestingAccountId(),
                cmd.displayName(), cmd.bio(),
                cmd.dietaryPreferences(), cmd.accessibilityNeeds(),
                newPrefs);
        profile = repo.save(profile);

        // Publish ProfileUpdated — Registration BC consumes this for guest-list read model
        publisher.publish("profile.updated.v1",
                UserProfileDomainEvents.ProfileUpdated.from(profile, CorrelationContext.current()));
        log.info("Profile updated for account {}", cmd.targetAccountId());
        return profile;
    }

    private Preferences buildPrefsIfChanged(UpdateProfileCommand cmd, Preferences current) {
        if (cmd.language() == null && cmd.timezone() == null
                && cmd.notificationChannel() == null
                && cmd.emailEnabled() == null && cmd.pushEnabled() == null) return null;
        return new Preferences(
                cmd.language() != null ? cmd.language() : current.getLanguage(),
                cmd.timezone() != null ? cmd.timezone() : current.getTimezone(),
                cmd.notificationChannel() != null ? cmd.notificationChannel() : current.getNotificationChannel(),
                cmd.emailEnabled() != null ? cmd.emailEnabled() : current.isEmailEnabled(),
                cmd.pushEnabled() != null ? cmd.pushEnabled() : current.isPushEnabled()
        );
    }
}
