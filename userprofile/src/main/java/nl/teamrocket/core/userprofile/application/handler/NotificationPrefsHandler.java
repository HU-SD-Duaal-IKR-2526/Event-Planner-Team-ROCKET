package nl.teamrocket.core.userprofile.application.handler;

import lombok.RequiredArgsConstructor;
import nl.teamrocket.core.userprofile.application.command.GetNotificationPrefsQuery;
import nl.teamrocket.core.userprofile.application.port.outbound.UserProfileRepository;
import nl.teamrocket.core.userprofile.domain.exception.ProfileNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Handles GET /users/{id}/notification-prefs.
 * Communication doc §3.4.4: Notification BC queries this synchronously (HTTP GET)
 * to decide whether to send push/email/in-app.
 */
@Service @RequiredArgsConstructor
public class NotificationPrefsHandler {
    private final UserProfileRepository repo;
    public NotificationPrefsResult handle(GetNotificationPrefsQuery query) {
        var profile = repo.findByAccountId(query.accountId())
                .orElseThrow(() -> new ProfileNotFoundException(query.accountId()));
        var prefs = profile.getPreferences();
        return new NotificationPrefsResult(prefs.isPushEnabled(), prefs.isEmailEnabled(), prefs.getTimezone());
    }
}
