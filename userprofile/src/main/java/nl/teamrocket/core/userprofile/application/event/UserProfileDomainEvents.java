package nl.teamrocket.core.userprofile.application.event;

import nl.teamrocket.core.userprofile.domain.model.NotificationChannel;
import nl.teamrocket.core.userprofile.domain.model.UserProfile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain events published by the User/Profile module to RabbitMQ.
 *
 * ProfileUpdated is the key Published Language contract consumed by Registration BC.
 * Communication doc §3.1.2 defines EXACTLY these 7 fields — do not rename without versioning:
 *
 *   userId               - UUID (= JWT sub)
 *   displayName          - String
 *   dietaryPreferences   - String[] e.g. ["vegan","gluten-free"]   ← required by Registration
 *   accessibilityNeeds   - String                                  ← required by Registration
 *   preferredLanguage    - String (ISO 639-1)
 *   updatedAt            - Instant (ISO-8601)
 *   version              - Long (monotone, for late-arrival filtering)
 */
public final class UserProfileDomainEvents {

    private UserProfileDomainEvents() {}

    /** profile.created.v1 — stub profile created after account registration */
    public record ProfileCreated(
            UUID    accountId,
            String  displayName,
            Instant createdAt,
            String  correlationId
    ) {
        public static ProfileCreated from(UserProfile p, String correlationId) {
            return new ProfileCreated(p.getAccountId(), p.getDisplayName(),
                    p.getCreatedAt(), correlationId);
        }
    }

    /**
     * profile.updated.v1
     *
     * Published Language contract — consumed by Registration BC for guest-list read model.
     * Communication doc §3.1.2 explicitly names ALL fields below.
     * Version enables late-arrival filtering: consumers reject events where
     * version < their current local version.
     */
    public record ProfileUpdated(
            UUID         userId,                 // = JWT sub = accountId
            String       displayName,
            List<String> dietaryPreferences,     // e.g. ["vegan","gluten-free"] — for Registration
            String       accessibilityNeeds,     // free text — for Registration guest list
            String       preferredLanguage,      // ISO 639-1
            Instant      updatedAt,
            long         version,                // monotone, for late-arrival dedup
            String       correlationId
    ) {
        public static ProfileUpdated from(UserProfile p, String correlationId) {
            return new ProfileUpdated(
                    p.getAccountId(),
                    p.getDisplayName(),
                    p.getDietaryPreferences(),
                    p.getAccessibilityNeeds(),
                    p.getPreferences().getLanguage(),
                    p.getUpdatedAt(),
                    p.getVersion() != null ? p.getVersion() : 0L,
                    correlationId
            );
        }
    }

    /** profile.avatar-updated.v1 — avatar set or removed */
    public record AvatarUpdated(
            UUID    accountId,
            String  avatarUrl,    // null when avatar is removed
            Instant updatedAt,
            String  correlationId
    ) {}
}
