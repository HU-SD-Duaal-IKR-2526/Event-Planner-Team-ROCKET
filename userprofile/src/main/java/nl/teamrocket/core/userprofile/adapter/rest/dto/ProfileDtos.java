package nl.teamrocket.core.userprofile.adapter.rest.dto;

import jakarta.validation.constraints.*;
import nl.teamrocket.core.userprofile.domain.model.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ProfileDtos {
    private ProfileDtos() {}

    // ── Requests ────────────────────────────────────────────────

    public record UpdateProfileRequest(
            @Size(min = 2, max = 64)  String displayName,
            @Size(max = 500)           String bio,
            List<String>               dietaryPreferences,
            String                     accessibilityNeeds,
            @Pattern(regexp = "^[a-z]{2}$", message = "ISO-639-1 required") String language,
            String                     timezone,
            NotificationChannel        notificationChannel,
            Boolean                    emailEnabled,
            Boolean                    pushEnabled
    ) {}

    public record SetAvatarRequest(
            @NotBlank String storageKey,
            @NotBlank String url,
            @NotBlank String contentType,
            @Positive long   sizeBytes
    ) {}

    // ── Responses ───────────────────────────────────────────────

    public record ProfileResponse(
            UUID         accountId,
            String       displayName,
            String       bio,
            List<String> dietaryPreferences,
            String       accessibilityNeeds,
            AvatarResponse       avatar,
            PreferencesResponse  preferences,
            Instant      createdAt,
            Instant      updatedAt
    ) {
        public static ProfileResponse from(UserProfile p) {
            return new ProfileResponse(
                    p.getAccountId(), p.getDisplayName(), p.getBio(),
                    p.getDietaryPreferences(), p.getAccessibilityNeeds(),
                    p.getAvatar() != null ? AvatarResponse.from(p.getAvatar()) : null,
                    PreferencesResponse.from(p.getPreferences()),
                    p.getCreatedAt(), p.getUpdatedAt());
        }
    }

    public record AvatarResponse(UUID id, String url, String contentType, long sizeBytes) {
        public static AvatarResponse from(Avatar a) {
            return new AvatarResponse(a.getId(), a.getUrl(), a.getContentType(), a.getSizeBytes());
        }
    }

    public record PreferencesResponse(String language, String timezone,
                                      NotificationChannel notificationChannel,
                                      boolean emailEnabled, boolean pushEnabled) {
        public static PreferencesResponse from(Preferences p) {
            return new PreferencesResponse(p.getLanguage(), p.getTimezone(),
                    p.getNotificationChannel(), p.isEmailEnabled(), p.isPushEnabled());
        }
    }

    public record NotificationPrefsResponse(boolean pushEnabled, boolean emailEnabled, String timezone) {}
    public record MessageResponse(String message) {}
}
