package nl.teamrocket.core.userprofile.adapter.mongo.mapper;

import nl.teamrocket.core.userprofile.adapter.mongo.document.UserProfileDocument;
import nl.teamrocket.core.userprofile.adapter.mongo.document.UserProfileDocument.*;
import nl.teamrocket.core.userprofile.domain.model.*;
import org.springframework.stereotype.Component;

@Component
public class ProfileDocumentMapper {

    public UserProfileDocument toDocument(UserProfile p) {
        return UserProfileDocument.builder()
                .id(p.getId())
                .accountId(p.getAccountId())
                .displayName(p.getDisplayName())
                .bio(p.getBio())
                .dietaryPreferences(p.getDietaryPreferences())
                .accessibilityNeeds(p.getAccessibilityNeeds())
                .avatar(toAvatarDoc(p.getAvatar()))
                .preferences(toPrefsDoc(p.getPreferences()))
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .version(p.getVersion())
                .build();
    }

    public UserProfile toDomain(UserProfileDocument d) {
        return UserProfile.reconstitute(
                d.getId(), d.getAccountId(), d.getDisplayName(), d.getBio(),
                toAvatar(d.getAvatar()), toPrefs(d.getPreferences()),
                d.getDietaryPreferences(), d.getAccessibilityNeeds(),
                d.getCreatedAt(), d.getUpdatedAt(), d.getVersion());
    }

    private AvatarSubDoc toAvatarDoc(Avatar a) {
        if (a == null) return null;
        return AvatarSubDoc.builder().id(a.getId()).url(a.getUrl())
                .contentType(a.getContentType()).sizeBytes(a.getSizeBytes()).build();
    }

    private Avatar toAvatar(AvatarSubDoc d) {
        if (d == null) return null;
        return new Avatar(d.getId(), d.getUrl(), d.getContentType(), d.getSizeBytes());
    }

    private PreferencesSubDoc toPrefsDoc(Preferences p) {
        if (p == null) return null;
        return PreferencesSubDoc.builder()
                .language(p.getLanguage()).timezone(p.getTimezone())
                .notificationChannel(p.getNotificationChannel())
                .emailEnabled(p.isEmailEnabled()).pushEnabled(p.isPushEnabled()).build();
    }

    private Preferences toPrefs(PreferencesSubDoc d) {
        if (d == null) return Preferences.defaultPreferences();
        return new Preferences(d.getLanguage(), d.getTimezone(), d.getNotificationChannel(),
                d.isEmailEnabled(), d.isPushEnabled());
    }
}
